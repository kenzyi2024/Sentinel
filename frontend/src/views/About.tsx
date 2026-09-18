import type { EngineMeta } from '../types';
import { REPO_URL } from '../config';

interface Props {
  meta: EngineMeta | null;
}

const STACK = ['Java 21', 'Spring Boot', 'Spring Data JPA', 'H2 / PostgreSQL', 'JUnit 5', 'React', 'TypeScript', 'Vite'];

export function About({ meta }: Props) {
  return (
    <div className="prose">
      <div className="eyebrow">About</div>
      <h2 style={{ fontSize: 24, marginTop: 6 }}>Why Sentinel exists</h2>
      <p>
        AI coding agents increasingly act on their own — reading files, running commands, calling tools. The defining
        risk is <strong>indirect prompt injection</strong>: an agent ingests data (a file, a ticket, a web page) that
        secretly contains instructions, and starts following them instead of its real task. OWASP ranks prompt
        injection the #1 risk for LLM applications, and real zero-click exfiltration incidents have already shipped.
        The emerging defense is not a smarter model — it is least privilege, human-in-the-loop review, and telemetry
        around what the agent tries to do.
      </p>

      <h2>How it works</h2>
      <p>
        Every action an agent attempts is intercepted and pushed through a deterministic pipeline: classify the
        resource → check capabilities → scan ingested content for injection → analyze commands → detect behavioral
        anomalies → analyze the action against the run's history for escalation → evaluate policy → score risk →
        resolve a decision → record an explainable event.
      </p>

      <h2>Why deterministic, not an LLM</h2>
      <p>
        The analysis engine is intentionally rule-based and runs entirely locally — no API keys, no external calls,
        works offline. That is a deliberate security position, not a limitation: a monitoring layer that is itself an
        LLM would inherit the very prompt-injection weakness it is meant to catch. Each decision is reconstructable
        from structured data, so you can always answer "why was this blocked?" The detection interfaces are built for
        extension, so a machine-learning classifier could be added behind them later.
      </p>

      <h2>Threat model</h2>
      <ul>
        <li>Indirect prompt injection hidden in project files and tickets</li>
        <li>Attempts to read secrets without the capability to do so</li>
        <li>Dangerous or destructive shell commands</li>
        <li>Privilege escalation and security-control tampering</li>
        <li>Reconnaissance → discovery → exfiltration kill chains</li>
        <li>Behavioral drift — an agent acting far outside its baseline</li>
      </ul>

      {meta && (
        <>
          <h2>Engine</h2>
          <div className="report-grid" style={{ marginTop: 'var(--sp-3)' }}>
            <div className="stat-tile">
              <div className="stat-v">{meta.riskScorers}</div>
              <div className="stat-k">Risk scorers</div>
            </div>
            <div className="stat-tile">
              <div className="stat-v">{meta.scenarios}</div>
              <div className="stat-k">Scenarios</div>
            </div>
            <div className="stat-tile">
              <div className="stat-v">{meta.usesExternalModel ? 'Yes' : 'No'}</div>
              <div className="stat-k">External model</div>
            </div>
            <div className="stat-tile">
              <div className="stat-v">{meta.deterministic ? 'Yes' : 'No'}</div>
              <div className="stat-k">Deterministic</div>
            </div>
          </div>
        </>
      )}

      <h2>Built with</h2>
      <div className="tag-list">
        {STACK.map((t) => (
          <span className="chip" key={t}>
            {t}
          </span>
        ))}
      </div>

      <p style={{ marginTop: 'var(--sp-5)' }}>
        <a href={REPO_URL} target="_blank" rel="noreferrer">
          View the source and full engineering docs on GitHub ↗
        </a>
      </p>
    </div>
  );
}
