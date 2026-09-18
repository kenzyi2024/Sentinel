package com.sentinel.detection;

import com.sentinel.domain.InjectionFinding;
import com.sentinel.domain.enums.InjectionCategory;
import com.sentinel.domain.enums.Severity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic, rule-based prompt-injection detector.
 *
 * <p>The scan runs in three layers, which together approximate the techniques a defender cares
 * about without any model inference:
 * <ol>
 *   <li><b>Direct pattern matching</b> against an ordered library of {@link InjectionRule}s
 *       (instruction override, role manipulation, context switching, secret access, exfiltration,
 *       security-control disabling, embedded imperatives).</li>
 *   <li><b>Obfuscation detection</b>: zero-width characters, letters spaced apart, long hex, and
 *       Base64 blobs — including <em>decoding</em> Base64 to look for instructions hidden inside.</li>
 *   <li><b>De-obfuscated re-scan</b>: the text is normalized (de-spaced, de-leetspeak, stripped of
 *       zero-width characters) and the rule library is re-run, so {@code i g n o r e} and
 *       {@code 1gn0re} are caught even though the raw pattern match missed them.</li>
 * </ol>
 *
 * <p>This is explicitly an educational / security-analysis engine, not a production LLM-grade
 * detector; see {@link InjectionScanner} for the extension seam.
 */
@Component
public class PatternInjectionScanner implements InjectionScanner {

    /** Cap the scanned length so a pathological input cannot dominate a run. */
    private static final int MAX_SCAN_LENGTH = 20_000;
    private static final int SNIPPET_RADIUS = 60;

    private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B-\\u200D\\uFEFF\\u2060]");
    private static final Pattern SPACED_LETTERS = Pattern.compile("(?i)\\b(?:[a-z]\\s){3,}[a-z]\\b");
    private static final Pattern LONG_HEX = Pattern.compile("(?i)\\b(?:[0-9a-f]{2}[\\s:]?){12,}\\b");
    private static final Pattern BASE64_BLOB = Pattern.compile("\\b[A-Za-z0-9+/]{24,}={0,2}");

    private static final List<InjectionRule> RULES = buildRules();

    @Override
    public List<InjectionFinding> scan(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String scanned = text.length() > MAX_SCAN_LENGTH ? text.substring(0, MAX_SCAN_LENGTH) : text;

        List<InjectionFinding> findings = new ArrayList<>();
        Set<String> matchedRuleIds = new LinkedHashSet<>();

        // Layer 1: direct pattern matching (positions map to the original text).
        for (InjectionRule rule : RULES) {
            Matcher matcher = rule.pattern().matcher(scanned);
            if (matcher.find()) {
                matchedRuleIds.add(rule.id());
                findings.add(new InjectionFinding(
                        rule.id(), rule.category(), rule.severity(),
                        snippet(scanned, matcher.start(), matcher.end()),
                        matcher.start(), rule.description()));
            }
        }

        // Layer 2: obfuscation signals (and Base64 decoding).
        detectObfuscation(scanned, findings, matchedRuleIds);

        // Layer 3: de-obfuscated re-scan to reveal spaced-out / leetspeak instructions.
        String normalized = deobfuscate(scanned);
        if (!normalized.equals(scanned.toLowerCase(Locale.ROOT))) {
            for (InjectionRule rule : RULES) {
                if (matchedRuleIds.contains(rule.id())) {
                    continue;
                }
                if (rule.pattern().matcher(normalized).find()) {
                    matchedRuleIds.add(rule.id());
                    findings.add(new InjectionFinding(
                            rule.id(), rule.category(), rule.severity(),
                            "(revealed after de-obfuscation)", -1,
                            rule.description() + " — hidden via obfuscation (spacing/leetspeak/zero-width)."));
                }
            }
        }

        findings.sort((a, b) -> Integer.compare(a.position(), b.position()));
        return findings;
    }

    private void detectObfuscation(String text, List<InjectionFinding> findings, Set<String> matchedRuleIds) {
        if (ZERO_WIDTH.matcher(text).find()) {
            findings.add(new InjectionFinding("obfuscation.zero-width", InjectionCategory.OBFUSCATION,
                    Severity.MEDIUM, "(zero-width characters present)", 0,
                    "Text contains invisible zero-width characters, a common way to hide instructions."));
        }
        Matcher spaced = SPACED_LETTERS.matcher(text);
        if (spaced.find()) {
            findings.add(new InjectionFinding("obfuscation.spaced-letters", InjectionCategory.OBFUSCATION,
                    Severity.MEDIUM, snippet(text, spaced.start(), spaced.end()), spaced.start(),
                    "Letters spaced apart to evade keyword matching (e.g. 'i g n o r e')."));
        }
        Matcher hex = LONG_HEX.matcher(text);
        if (hex.find()) {
            findings.add(new InjectionFinding("obfuscation.long-hex", InjectionCategory.OBFUSCATION,
                    Severity.LOW, snippet(text, hex.start(), hex.end()), hex.start(),
                    "A long hex-encoded blob that may hide an encoded instruction or payload."));
        }
        Matcher b64 = BASE64_BLOB.matcher(text);
        while (b64.find()) {
            String decoded = tryDecodeBase64(b64.group());
            if (decoded == null) {
                continue;
            }
            findings.add(new InjectionFinding("obfuscation.base64", InjectionCategory.OBFUSCATION,
                    Severity.MEDIUM, snippet(text, b64.start(), b64.end()), b64.start(),
                    "A Base64 blob decodes to readable text — a channel for hidden instructions."));
            // If the decoded payload itself contains an injection, surface that too.
            String decodedLower = decoded.toLowerCase(Locale.ROOT);
            for (InjectionRule rule : RULES) {
                if (matchedRuleIds.contains(rule.id())) {
                    continue;
                }
                if (rule.pattern().matcher(decodedLower).find()) {
                    matchedRuleIds.add(rule.id());
                    findings.add(new InjectionFinding(rule.id(), rule.category(),
                            Severity.CRITICAL, "(decoded from Base64: \"" + truncate(decoded, 60) + "\")",
                            b64.start(), rule.description() + " — concealed inside a Base64 blob."));
                }
            }
        }
    }

    private static String tryDecodeBase64(String blob) {
        if (blob.length() % 4 != 0) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(blob);
            String text = new String(decoded, StandardCharsets.UTF_8);
            long printable = text.chars().filter(c -> c >= 0x20 && c < 0x7F).count();
            // Only treat it as hidden text when it decodes to mostly-printable ASCII.
            if (text.length() >= 6 && printable >= text.length() * 0.85) {
                return text;
            }
        } catch (IllegalArgumentException ignored) {
            // not valid Base64
        }
        return null;
    }

    /** Normalizes text to defeat common obfuscation before re-running the rule library. */
    static String deobfuscate(String text) {
        String result = ZERO_WIDTH.matcher(text).replaceAll("");
        result = result.toLowerCase(Locale.ROOT);
        // Collapse single letters separated by spaces: "i g n o r e" -> "ignore".
        StringBuilder collapsed = new StringBuilder(result.length());
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            boolean isSpacer = (c == ' ')
                    && i > 0 && Character.isLetterOrDigit(chars[i - 1])
                    && i + 1 < chars.length && Character.isLetterOrDigit(chars[i + 1])
                    && (i + 2 >= chars.length || !Character.isLetterOrDigit(chars[i + 2]))
                    && (i - 2 < 0 || !Character.isLetterOrDigit(chars[i - 2]));
            if (!isSpacer) {
                collapsed.append(c);
            }
        }
        result = collapsed.toString();
        // Reverse common leetspeak substitutions.
        result = result.replace('0', 'o').replace('1', 'i').replace('3', 'e')
                .replace('4', 'a').replace('5', 's').replace('@', 'a').replace('$', 's');
        return result;
    }

    private static String snippet(String text, int start, int end) {
        int from = Math.max(0, start - SNIPPET_RADIUS);
        int to = Math.min(text.length(), end + SNIPPET_RADIUS);
        String core = text.substring(from, to).replaceAll("\\s+", " ").trim();
        String prefix = from > 0 ? "…" : "";
        String suffix = to < text.length() ? "…" : "";
        return prefix + core + suffix;
    }

    private static String truncate(String text, int max) {
        String single = text.replaceAll("\\s+", " ").trim();
        return single.length() <= max ? single : single.substring(0, max) + "…";
    }

    private static List<InjectionRule> buildRules() {
        List<InjectionRule> rules = new ArrayList<>();

        // --- Instruction override ---
        rules.add(InjectionRule.of("override.ignore-previous", InjectionCategory.INSTRUCTION_OVERRIDE,
                Severity.CRITICAL,
                "ignore\\s+(all\\s+|any\\s+|the\\s+)?(previous|prior|above|preceding|earlier)\\s+(instructions?|prompts?|messages?|directions?|context|rules?)",
                "Attempts to make the agent ignore its previous instructions."));
        rules.add(InjectionRule.of("override.disregard", InjectionCategory.INSTRUCTION_OVERRIDE,
                Severity.CRITICAL,
                "disregard\\s+(all\\s+|the\\s+|any\\s+)?(previous|prior|above|earlier|system)",
                "Attempts to make the agent disregard prior or system instructions."));
        rules.add(InjectionRule.of("override.forget", InjectionCategory.INSTRUCTION_OVERRIDE,
                Severity.HIGH,
                "forget\\s+(everything|all|the\\s+above|(your\\s+)?previous|prior)",
                "Attempts to make the agent forget its established context."));
        rules.add(InjectionRule.of("override.new-instructions", InjectionCategory.INSTRUCTION_OVERRIDE,
                Severity.HIGH,
                "(new|updated|real|actual|revised)\\s+(instructions?|task|directive|system\\s+prompt)\\s*[:\\-]",
                "Presents replacement instructions that try to supersede the real ones."));

        // --- Role manipulation ---
        rules.add(InjectionRule.of("role.you-are-now", InjectionCategory.ROLE_MANIPULATION,
                Severity.HIGH,
                "you\\s+are\\s+now\\s+(a|an|the)?\\s*(dan|admin|root|developer|unrestricted|jailbroken|god)",
                "Attempts to reassign the agent's role to a privileged or unrestricted persona."));
        rules.add(InjectionRule.of("role.enable-mode", InjectionCategory.ROLE_MANIPULATION,
                Severity.HIGH,
                "(enable|activate|enter|switch\\s+to)\\s+(developer|god|admin|debug|dan|jailbreak)\\s+mode",
                "Attempts to enable a privileged or 'jailbreak' mode."));
        rules.add(InjectionRule.of("role.act-as", InjectionCategory.ROLE_MANIPULATION,
                Severity.MEDIUM,
                "(act\\s+as|pretend\\s+(to\\s+be|you\\s+are)|behave\\s+like)\\s+(an?\\s+)?(admin|root|developer|unrestricted|different)",
                "Attempts to have the agent role-play as a more privileged actor."));

        // --- Context switching / fake system markers ---
        rules.add(InjectionRule.of("context.system-marker", InjectionCategory.CONTEXT_SWITCH,
                Severity.HIGH,
                "(#{2,}\\s*system|\\[system\\]|<\\|?system\\|?>|<\\|im_start\\|>|\\[/?inst\\])",
                "Injects fake system/role delimiters to switch the model's context."));
        rules.add(InjectionRule.of("context.system-prompt", InjectionCategory.CONTEXT_SWITCH,
                Severity.MEDIUM,
                "(reveal|show|print|repeat|what\\s+(is|are))\\s+(your\\s+)?(the\\s+)?(system\\s+prompt|initial\\s+instructions|hidden\\s+instructions)",
                "Attempts to extract the agent's system prompt or hidden instructions."));

        // --- Secret access ---
        rules.add(InjectionRule.of("secret.read-credentials", InjectionCategory.SECRET_ACCESS,
                Severity.HIGH,
                "(read|print|show|reveal|cat|dump|display|output|leak|copy)\\s+(the\\s+|all\\s+|your\\s+)?[^\\n]{0,30}(\\.env|secret|secrets|credential|password|api[_\\s-]?key|access[_\\s-]?key|private\\s+key|token)",
                "Attempts to read or expose secrets, credentials, or keys."));

        // --- Data exfiltration ---
        rules.add(InjectionRule.of("exfil.send-to-url", InjectionCategory.DATA_EXFILTRATION,
                Severity.CRITICAL,
                "(send|post|upload|exfiltrate|transmit|forward|email|leak|deliver)\\s+[^\\n]{0,60}(to\\s+|->|→|at\\s+)?[^\\n]{0,20}(https?://|ftp://|webhook|pastebin|@[a-z0-9.-]+\\.[a-z]{2,})",
                "Attempts to send data to an external destination."));
        rules.add(InjectionRule.of("exfil.post-env", InjectionCategory.DATA_EXFILTRATION,
                Severity.CRITICAL,
                "(curl|wget|fetch|http)\\s[^\\n]{0,80}(\\.env|env|secret|token|credential)",
                "Attempts to transmit environment/secret material over the network."));

        // --- Security control disabling ---
        rules.add(InjectionRule.of("control.disable-security", InjectionCategory.SECURITY_CONTROL_DISABLE,
                Severity.HIGH,
                "(disable|turn\\s+off|bypass|skip|deactivate|deactivate|ignore)\\s+(the\\s+|all\\s+|any\\s+)?(security|logging|monitor(ing)?|audit(ing)?|guard(rails)?|safety|filter|policy|policies|sentinel|detection|scanner)",
                "Attempts to disable or bypass security or monitoring controls."));
        rules.add(InjectionRule.of("control.do-not-report", InjectionCategory.SECURITY_CONTROL_DISABLE,
                Severity.HIGH,
                "(do\\s+not|don'?t|never)\\s+(log|record|report|alert|notify|tell|mention|reveal)\\s+[^\\n]{0,30}(this|user|anyone|admin|action|that)",
                "Instructs the agent to act covertly and avoid detection."));

        // --- Embedded imperatives addressed to the model ---
        rules.add(InjectionRule.of("imperative.addressed-to-ai", InjectionCategory.IMPERATIVE_INJECTION,
                Severity.MEDIUM,
                "\\b(assistant|ai|model|agent|llm|chatbot|copilot|system)\\b[,:]?\\s+(must|should|please|now|immediately)\\s+",
                "Data that directly issues commands to the AI/agent rather than being inert."));
        rules.add(InjectionRule.of("imperative.attention-ai", InjectionCategory.IMPERATIVE_INJECTION,
                Severity.MEDIUM,
                "(important|attention|note\\s+to|instructions?\\s+(for|to))\\s*[:\\-]?\\s*(the\\s+)?(ai|assistant|model|agent|llm)",
                "A block of text that tries to address and instruct the AI directly."));

        return rules;
    }
}
