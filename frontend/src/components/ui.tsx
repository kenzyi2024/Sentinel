import type { AgentState, Decision, RiskBand } from '../types';
import { bandClass, colorFor, decisionClass, decisionLabel } from '../lib/format';

export function Shield({ size = 18 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 2.5 4.5 5.5v6c0 4.6 3.2 8.4 7.5 10 4.3-1.6 7.5-5.4 7.5-10v-6L12 2.5Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
      <path d="m8.6 12 2.4 2.4 4.4-4.6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function DecisionBadge({ decision }: { decision: Decision }) {
  const cls = decisionClass(decision);
  return (
    <span className={`badge ${cls}`}>
      <span className="tick" style={{ background: 'currentColor' }} />
      {decisionLabel(decision)}
    </span>
  );
}

export function RiskBadge({ band, score }: { band: RiskBand; score?: number }) {
  return (
    <span className={`badge ${bandClass(band)}`}>
      {score !== undefined ? `${score} · ` : ''}
      {band}
    </span>
  );
}

const STATE_COLOR: Record<AgentState, string> = {
  INITIALIZING: 'var(--fg-dim)',
  ACTIVE: 'var(--low)',
  COMPROMISED: 'var(--high)',
  QUARANTINED: 'var(--medium)',
  TERMINATED: 'var(--critical)',
};

export function StateBadge({ state }: { state: AgentState }) {
  return (
    <span className="tag-state" style={{ color: STATE_COLOR[state] }}>
      ● {state}
    </span>
  );
}

export function RiskGauge({ score, band }: { score: number; band: RiskBand }) {
  const color = colorFor(bandClass(band));
  return (
    <div className="gauge-wrap">
      <svg width="200" height="120" viewBox="0 0 200 120" role="img" aria-label={`Risk ${score} of 100, ${band}`}>
        <path
          d="M16 108 A84 84 0 0 1 184 108"
          fill="none"
          stroke="var(--bg-3)"
          strokeWidth="12"
          strokeLinecap="round"
          pathLength={100}
        />
        <path
          d="M16 108 A84 84 0 0 1 184 108"
          fill="none"
          stroke={color}
          strokeWidth="12"
          strokeLinecap="round"
          pathLength={100}
          strokeDasharray={`${score} 100`}
        />
      </svg>
      <div className="gauge-score" style={{ color }}>
        {score}
      </div>
      <div className="gauge-label" style={{ color }}>
        {band} RISK
      </div>
    </div>
  );
}
