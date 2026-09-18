package com.sentinel.detection;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Inspects a shell command for dangerous constructs. This is the single source of truth for
 * "how dangerous is this command", shared by the command-danger risk scorer and the escalation
 * detector.
 *
 * <p>Individual pattern weights are combined with a <em>probabilistic OR</em>
 * ({@code 1 - Π(1 - wᵢ)}) rather than a plain sum. That keeps the result bounded in [0, 1] while
 * still letting multiple independent red flags compound — a command that both pipes a download
 * into a shell <em>and</em> clears history is scored higher than either alone, but never exceeds 1.
 */
@Component
public class CommandAnalyzer {

    private record Rule(Pattern pattern, double weight, String reason, boolean exfil, boolean escalation) {
        static Rule of(String regex, double weight, String reason, boolean exfil, boolean escalation) {
            return new Rule(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), weight, reason, exfil, escalation);
        }
    }

    private static final List<Rule> RULES = List.of(
            Rule.of("\\brm\\s+-[a-z]*[rf][a-z]*\\s+(/|~|\\$home)", 0.95,
                    "Recursively deletes a root or home path (rm -rf on a critical location).", false, true),
            Rule.of("\\brm\\s+-[a-z]*[rf]", 0.8,
                    "Recursive force delete (rm -rf).", false, false),
            Rule.of(":\\(\\)\\s*\\{\\s*:\\s*\\|\\s*:\\s*&\\s*\\}\\s*;\\s*:", 0.95,
                    "Fork bomb — exhausts process resources.", false, true),
            Rule.of("(curl|wget|fetch)\\b[^|]*\\|\\s*(sudo\\s+)?(sh|bash|zsh)\\b", 0.92,
                    "Pipes downloaded content straight into a shell (remote code execution).", true, true),
            Rule.of("base64\\s+-d[^|]*\\|\\s*(sh|bash|zsh)", 0.9,
                    "Decodes Base64 and executes it — a common obfuscated-payload technique.", false, true),
            Rule.of("/dev/tcp/", 0.9,
                    "Uses the bash /dev/tcp pseudo-device — a reverse-shell channel.", true, true),
            Rule.of("\\b(nc|ncat|netcat)\\b", 0.7,
                    "netcat — frequently used for reverse shells or data exfiltration.", true, false),
            Rule.of("\\bchmod\\s+([ug]*\\+s|-R\\s+0?777|0?777)\\b", 0.7,
                    "Sets world-writable or setuid permissions (chmod 777 / +s).", false, true),
            Rule.of("\\b(sudo|doas|su)\\b", 0.7,
                    "Elevates privileges (sudo/doas/su).", false, true),
            Rule.of(">>?\\s*\\S*(authorized_keys|\\.bashrc|\\.zshrc|\\.profile|/etc/passwd|sshd_config)", 0.75,
                    "Writes to a login/persistence file (authorized_keys, shell rc, /etc/passwd).", false, true),
            Rule.of("\\bcrontab\\b|/etc/cron", 0.5,
                    "Installs a scheduled job (persistence).", false, true),
            Rule.of("history\\s+-c|unset\\s+histfile|\\.bash_history", 0.6,
                    "Clears shell history (anti-forensics / hides activity).", false, true),
            Rule.of("(curl|wget)\\b[^\\n]*(-d|--data|-f\\b|--upload-file|-t\\b)", 0.55,
                    "Uploads data with curl/wget — potential exfiltration.", true, false),
            Rule.of("\\b(scp|rsync)\\b[^\\n]*@", 0.5,
                    "Copies files to a remote host (scp/rsync) — potential exfiltration.", true, false),
            Rule.of("\\beval\\b", 0.5,
                    "Uses eval — executes dynamically constructed code.", false, false),
            Rule.of("\\b(dd)\\b[^\\n]*of=/dev/|\\bmkfs\\b|>\\s*/dev/sd", 0.85,
                    "Writes directly to a block device (data destruction).", false, true),
            Rule.of("\\b(shutdown|reboot|halt|poweroff)\\b", 0.6,
                    "Shuts down or reboots the host.", false, false)
    );

    public CommandAnalysis analyze(String command) {
        if (command == null || command.isBlank()) {
            return CommandAnalysis.safe();
        }
        List<String> reasons = new ArrayList<>();
        boolean exfil = false;
        boolean escalation = false;
        double survivalProduct = 1.0; // Π(1 - wᵢ) over matched rules

        for (Rule rule : RULES) {
            if (rule.pattern().matcher(command).find()) {
                reasons.add(rule.reason());
                survivalProduct *= (1.0 - rule.weight());
                exfil = exfil || rule.exfil();
                escalation = escalation || rule.escalation();
            }
        }
        double danger = 1.0 - survivalProduct; // probabilistic OR
        return new CommandAnalysis(danger, reasons, exfil, escalation);
    }
}
