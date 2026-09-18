# Threat model

Sentinel models the security layer that sits between an autonomous AI agent and the system it
operates on. It assumes the agent is **capable and tool-using** but **not trustworthy** — either
because it can be hijacked by injected instructions, or because it is adversarial from the start.

## What Sentinel defends against

| Threat | How it shows up | Sentinel's response |
|--------|-----------------|---------------------|
| **Indirect prompt injection** | Instructions hidden in ingested files/tickets (`docs/onboarding.md`, `issues/TICKET-482.md`) | `PatternInjectionScanner` flags override / role / exfiltration / control-disable patterns (incl. spaced-out and Base64-encoded); the agent transitions to `COMPROMISED` |
| **Secret access without clearance** | Reading `.env`, `secrets/`, keys | `ResourceClassifier` marks the resource SECRET; `PermissionModel` requires `READ_SECRETS`; missing capability → hard **block** |
| **Dangerous shell commands** | `rm -rf /`, `curl … | sh`, reverse shells, fork bombs | `CommandAnalyzer` scores danger; danger ≥ 0.9 is **never allowed** |
| **Privilege escalation / tampering** | `sudo`, `chmod 777`, disabling logging, editing config | Command/escalation detection + config-change approval policy |
| **Data exfiltration** | Outbound requests carrying secrets | Egress requires approval; combined with prior discovery it reaches CRITICAL → **block** |
| **Multi-step attack chains** | recon → discovery → exfiltration across many actions | `EscalationDetector` traverses the event graph to recognize the sequence no single action reveals |
| **Behavioral drift** | An agent reading 30 files when it usually reads 5 | `AnomalyDetector` (sliding window + Welford z-score) flags the spike |
| **Excess privilege** | An agent granted capabilities it never uses | The report recommends revoking unused capabilities |

## Trust boundaries

- **Untrusted:** all content an agent ingests (file contents, command output, tickets). This is data,
  never instructions — the core lesson of indirect prompt injection.
- **Enforced:** capabilities (least privilege) and policies are the authority boundary. An action is
  permitted only if the agent holds every required capability *and* policy/risk allow it.
- **Human-in-the-loop:** risky-but-legitimate actions (network egress, config changes, HIGH risk)
  are held for approval rather than allowed or blocked outright.

## Decision precedence

`DecisionResolver` applies, in strict order: missing capability → **block**; never-allowed command
→ **block**; explicit policy DENY → **block**; CRITICAL risk → **block**; policy REQUIRE_APPROVAL or
HIGH risk → **review**; otherwise **allow** (with warnings for WARN policies / MEDIUM risk). The risk
gate can veto a permissive policy, but an explicit DENY and a missing capability are absolute.

## Out of scope / non-goals

- Real LLM agents (agents here are scripted simulations; no action is actually executed).
- A production-grade detector — the pattern engine is educational and can be evaded or over-trigger.
- Authentication, authorization of *human* users, multi-tenancy, and rate limiting.
- Network/host hardening of the Sentinel service itself.

See [ARCHITECTURE.md](ARCHITECTURE.md) for how these defenses are implemented.
