# Portfolio & interview kit

Materials for putting Sentinel on a résumé and talking about it in interviews. Everything here is
grounded in what the project actually does — no invented users, metrics, or production claims.

## Project title

**Sentinel — an AI-agent security & observability console.**

## One-sentence description

A full-stack platform that runs simulated AI agents in a controlled project, intercepts every action
they attempt, and decides — with a deterministic, fully local engine — whether to allow, block, or
hold it for review, explaining every decision.

## Résumé bullets

- Built **Sentinel**, a full-stack AI-agent security platform (Java 21 / Spring Boot API + React/TypeScript console) that intercepts every action a simulated agent attempts and resolves it to **allow / block / require-approval** with a deterministic, fully local engine — **no external LLM, API keys, or network required**.
- Designed a modular **7-dimension risk-scoring engine** (Strategy pattern) where each scorer emits an explainable factor; the numeric score always reconciles with its contributing reasons, so every decision answers "why?".
- Implemented deterministic **prompt-injection detection** (pattern + obfuscation + Base64 decoding), **behavioral anomaly detection** using **Welford's online variance** over a sliding window, and **directed-graph traversal** to recognize reconnaissance → discovery → exfiltration attack chains across a run.
- Engineered a clean layered backend with a **configurable policy engine**, JPA event-store persistence (H2 / PostgreSQL), an OpenAPI-documented REST API, and **60 unit + integration tests**; wired **GitHub Actions CI** and a static **GitHub Pages** demo generated from real engine output.

## Technical skills demonstrated

`Java 21` · `Spring Boot` · `Spring Data JPA` · `REST API design` · `PostgreSQL` / `H2` ·
`JUnit 5` · `Mockito` · `AssertJ` · `MockMvc` · `React` · `TypeScript` · `Vite` ·
`Data structures & algorithms` (graphs, sliding window, online statistics, FSM) ·
`OOP & design patterns` (Strategy, Builder) · `Software architecture` · `Security fundamentals`
(least privilege, prompt injection, threat modeling) · `CI/CD` (GitHub Actions) · `Docker`

## GitHub "About" description

> Deterministic, local security & observability engine for AI agents — intercepts, scores, and explains every action. Java/Spring Boot + React/TypeScript.

## 30-second explanation

> AI coding agents can now read files and run commands on their own, which makes them vulnerable to
> prompt injection — hidden instructions in the data they read. Sentinel is the security layer that
> sits around an agent: it intercepts every action, checks it against the agent's permissions and a
> policy engine, scores the risk across seven dimensions, and decides whether to allow, block, or
> require approval — all with a deterministic local engine, and it explains every decision. The
> centerpiece demo shows a normal agent get hijacked by a poisoned file and then get stopped when it
> tries to steal secrets.

## 2-minute explanation

> The problem: agents that both read untrusted data and take actions are exposed to indirect prompt
> injection — OWASP's #1 LLM risk — and you can't fully fix it inside the model, so the industry is
> moving toward constraining agents from the outside with least privilege, human-in-the-loop, and
> telemetry. Sentinel is a working model of that outside layer.
>
> Architecturally it's a layered Spring Boot backend and a React console. Every action an agent
> attempts goes through a pipeline: classify the resource, check capabilities, scan ingested content
> for injection, analyze commands, detect behavioral anomalies, analyze the action sequence for
> escalation, evaluate policy, then score risk and resolve a decision. The risk engine is seven
> independent scorers combined into a weighted sum — each emits a factor with a signal, a weight, a
> point contribution, and a plain-English reason, so the score is fully explainable and the "why"
> always reconciles with the "how much."
>
> The interesting algorithms are real: Welford's online variance builds the anomaly baseline so I can
> compute a z-score on a streaming read-rate; the escalation detector treats the run's events as a
> directed graph and traverses the predecessor chain to catch recon→discovery→exfiltration chains
> that no single action reveals. It's deterministic and local by design — a monitoring layer that was
> itself an LLM would inherit the very injection weakness it's meant to catch — but the detection
> interfaces are built so an ML classifier could drop in later. It's covered by 60 tests, has CI, and
> deploys a working demo to GitHub Pages using data the backend itself generates.

## Likely interview questions & answers

**1. What was the hardest engineering problem?**
Making risk *explainable* without sacrificing composability. I didn't want one hard-coded formula, so
I used a Strategy-pattern set of scorers that each return a `RiskFactor` — normalized signal × weight
= point contribution, plus a reason. The engine sums the contributions and clamps to 100, and I test
that the factors' contributions reconcile with the total. Getting the weights so the six scenarios
produce decisive, sensible outcomes (a lone dangerous command is "review," but the same command after
recon+discovery is "block") took iteration — driven by the end-to-end scenario tests.

**2. Why deterministic instead of using an LLM to judge actions?**
Three reasons. Security: a monitoring layer that is itself an LLM inherits the prompt-injection
weakness it's supposed to catch. Explainability: rules produce a reconstructable "why." Practicality:
it runs locally with no keys and is fully testable and reproducible. I kept the detection behind
interfaces (`InjectionScanner`, `RiskScorer`) so an ML classifier could be added without touching the
pipeline.

**3. How does the anomaly detection actually work?**
A sliding window over recent actions, plus Welford's online algorithm to maintain a running
mean/variance of the read-count at each window position. The current window's read-count is expressed
as a z-score against that baseline; a spike above the threshold, or bulk enumeration crossing a count
threshold, raises the anomaly signal. I gate the z-score on a minimum read volume so tiny variance in
a quiet run can't manufacture a false anomaly — that was a real bug my tests caught.

**4. What happens when detection is wrong?**
By design, false positives fail safe: an over-triggered signal pushes toward "require approval,"
which is a human gate, not a hard block, except for absolute rules (missing capability, never-allowed
commands). Every decision is explainable, so a reviewer can see exactly which factor fired and
override it. I'm explicit in the docs that the pattern detectors can be evaded or over-trigger — they
are educational, not a production detector.

**5. How would you scale it, and what would you change for production?**
The evaluator is stateless (history is passed in), so it scales horizontally behind the API. I'd move
persistence to PostgreSQL with Flyway migrations (already have the profile), add streaming (SSE) for
live runs, and replace the event-store JSON columns with a proper time-series store if event volume
grew. For real use I'd wrap actual tool-using agents behind an adapter, run commands in a sandbox, and
add the ML injection classifier behind the existing interface.
