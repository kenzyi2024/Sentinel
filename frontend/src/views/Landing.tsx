import type { EngineMeta } from '../types';
import { REPO_URL } from '../config';

interface Props {
  onRunDemo: () => void;
  onBrowse: () => void;
  meta: EngineMeta | null;
}

const PIPELINE = ['Agent', 'Action', 'Permission', 'Policy', 'Risk', 'Decision', 'Telemetry', 'Investigation'];

const FEATURES: { title: string; body: string }[] = [
  {
    title: 'Deterministic risk engine',
    body: 'Seven independent, weighted scorers combine into one explainable score — every point traces back to a reason. No model, no black box.',
  },
  {
    title: 'Prompt-injection detection',
    body: 'Pattern, obfuscation, and Base64-decoding analysis flags hidden instructions in ingested files — the core threat behind agent hijacking.',
  },
  {
    title: 'Behavioral anomaly detection',
    body: "Welford's online variance builds a live baseline; bulk enumeration and sensitive-access-after-recon light up as they happen.",
  },
  {
    title: 'Capability & policy model',
    body: 'Least-privilege capabilities plus a configurable policy engine decide allow / block / require-approval — editable without touching code.',
  },
  {
    title: 'Escalation graph analysis',
    body: 'Actions link into a directed graph; traversal recognizes the reconnaissance → discovery → exfiltration kill chain across a whole run.',
  },
  {
    title: 'Explainable by construction',
    body: 'Click any event to see what was required, what was missing, which rules fired, what came before, and exactly why it was blocked.',
  },
];

export function Landing({ onRunDemo, onBrowse, meta }: Props) {
  return (
    <div>
      <section className="hero">
        <div className="eyebrow">AI-agent security · observability · local-first</div>
        <h1 style={{ marginTop: 12 }}>
          Watch AI agents work.
          <br />
          <span className="accent">Catch what they try to do.</span>
        </h1>
        <p className="lead">
          Sentinel runs simulated AI agents inside a controlled project, intercepts every action they attempt, and
          decides — with an explainable, fully local engine — whether to allow it, block it, or send it for review.
        </p>
        <div className="hero-cta">
          <button className="btn btn-primary" onClick={onRunDemo}>
            ▶ Run the compromised-agent demo
          </button>
          <button className="btn" onClick={onBrowse}>
            Browse scenarios
          </button>
          <a className="btn btn-ghost" href={REPO_URL} target="_blank" rel="noreferrer">
            Source on GitHub ↗
          </a>
        </div>
      </section>

      <div className="pipeline">
        {PIPELINE.map((node, i) => (
          <span key={node} style={{ display: 'contents' }}>
            {i > 0 && <span className="arrow">→</span>}
            <span className={`node ${node === 'Risk' || node === 'Decision' ? 'hot' : ''}`}>{node}</span>
          </span>
        ))}
      </div>

      <div className="feature-grid">
        {FEATURES.map((f) => (
          <div className="feature panel" key={f.title}>
            <div className="fi">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="3" width="18" height="18" rx="4" stroke="currentColor" strokeWidth="1.6" />
                <path d="m8 12 2.5 2.5L16 9" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </div>
            <h4>{f.title}</h4>
            <p>{f.body}</p>
          </div>
        ))}
      </div>

      {meta && (
        <p className="dim" style={{ textAlign: 'center', marginTop: 'var(--sp-6)', fontFamily: 'var(--font-mono)', fontSize: 12 }}>
          {meta.riskScorers} risk scorers · {meta.scenarios} scenarios · deterministic ·{' '}
          {meta.usesExternalModel ? 'uses external model' : 'no external model'}
        </p>
      )}
    </div>
  );
}
