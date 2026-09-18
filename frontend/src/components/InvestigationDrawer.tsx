import type { SentinelEvent } from '../types';
import { bandColor, humanize } from '../lib/format';
import { DecisionBadge, RiskBadge, StateBadge } from './ui';

interface Props {
  event: SentinelEvent;
  events: SentinelEvent[];
  onClose: () => void;
  onSelect: (event: SentinelEvent) => void;
}

function contribColor(contribution: number): string {
  if (contribution >= 25) return 'var(--critical)';
  if (contribution >= 15) return 'var(--high)';
  if (contribution >= 8) return 'var(--medium)';
  return 'var(--low)';
}

export function InvestigationDrawer({ event, events, onClose, onSelect }: Props) {
  const previous = events.find((e) => e.id === event.previousEventId);
  const next = events.find((e) => e.previousEventId === event.id);
  const maxContribution = Math.max(1, ...event.riskAssessment.factors.map((f) => f.contribution));

  return (
    <>
      <div className="drawer-backdrop" onClick={onClose} />
      <aside className="drawer" role="dialog" aria-label="Event investigation">
        <div className="drawer-head">
          <div>
            <div className="eyebrow">Investigation · Event #{event.sequence}</div>
            <h3 style={{ fontSize: 18, marginTop: 4 }}>{event.actionType}</h3>
            <div className="row" style={{ marginTop: 8 }}>
              <DecisionBadge decision={event.decision} />
              <RiskBadge band={event.riskAssessment.band} score={event.riskAssessment.score} />
              {event.inducedByInjection && <span className="chip">induced by injection</span>}
            </div>
          </div>
          <button className="btn-ghost btn btn-sm" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <div className="drawer-body">
          <div className="field">
            <div className="field-k">What happened</div>
            <div className="field-v">{event.intent}</div>
          </div>

          <dl className="kv" style={{ marginBottom: 'var(--sp-4)' }}>
            <dt>Agent</dt>
            <dd>{event.agentName}</dd>
            <dt>Resource</dt>
            <dd>{event.resource}</dd>
            <dt>Sensitivity</dt>
            <dd>{event.resourceSensitivity}</dd>
            <dt>Requires</dt>
            <dd>{event.requiredCapabilities.join(', ') || '—'}</dd>
            <dt>Missing</dt>
            <dd style={{ color: event.missingCapabilities.length ? 'var(--critical)' : undefined }}>
              {event.missingCapabilities.join(', ') || 'none'}
            </dd>
            <dt>Executed</dt>
            <dd>{event.executed ? 'yes' : 'no (intercepted)'}</dd>
            <dt>Agent state</dt>
            <dd>
              <StateBadge state={event.agentStateAfter} />
            </dd>
          </dl>

          <div className="section-title">Risk breakdown</div>
          {event.riskAssessment.factors.length === 0 && (
            <p className="muted" style={{ fontSize: 13 }}>No risk factors contributed — this action is benign.</p>
          )}
          {event.riskAssessment.factors.map((factor) => (
            <div className="factor" key={factor.category + factor.name}>
              <div className="factor-top">
                <span className="cat">{humanize(factor.category)}</span>
                <span className="pts" style={{ color: contribColor(factor.contribution) }}>
                  +{factor.contribution}
                </span>
              </div>
              <div className="factor-bar">
                <span
                  style={{
                    width: `${(factor.contribution / maxContribution) * 100}%`,
                    background: contribColor(factor.contribution),
                  }}
                />
              </div>
              <div className="factor-why">{factor.explanation}</div>
            </div>
          ))}

          {event.policyDecision.matched && (
            <>
              <div className="section-title" style={{ marginTop: 'var(--sp-4)' }}>
                Policy
              </div>
              <div className="callout">
                <strong>{event.policyDecision.policyName}</strong> → {humanize(event.policyDecision.effect)}
                <div className="muted" style={{ marginTop: 4 }}>
                  {event.policyDecision.reason}
                </div>
              </div>
            </>
          )}

          {event.injectionFindings.length > 0 && (
            <>
              <div className="section-title" style={{ marginTop: 'var(--sp-4)' }}>
                Prompt-injection findings
              </div>
              <div className="finding-list">
                {event.injectionFindings.map((f, i) => (
                  <div className="finding" key={f.ruleId + i}>
                    <div className="row between">
                      <span className="mono" style={{ fontSize: 12 }}>
                        {humanize(f.category)}
                      </span>
                      <span className="badge critical">{f.severity}</span>
                    </div>
                    <div className="muted" style={{ fontSize: 12.5, marginTop: 4 }}>
                      {f.explanation}
                    </div>
                    <div className="snip">{f.matchedText}</div>
                  </div>
                ))}
              </div>
            </>
          )}

          {event.anomalyResult.score > 0 && (
            <>
              <div className="section-title" style={{ marginTop: 'var(--sp-4)' }}>
                Behavioral anomaly
              </div>
              <div className="callout" style={{ borderLeftColor: 'var(--high)' }}>
                {event.anomalyResult.reasons.map((r, i) => (
                  <div key={i}>{r}</div>
                ))}
                <div className="dim mono" style={{ fontSize: 11, marginTop: 6 }}>
                  window={event.anomalyResult.windowActionCount} · z={event.anomalyResult.zScore.toFixed(2)}
                </div>
              </div>
            </>
          )}

          <div className="section-title" style={{ marginTop: 'var(--sp-4)' }}>
            Why this happened
          </div>
          <ul className="reason-list">
            {event.reasons.map((r, i) => (
              <li key={i}>{r}</li>
            ))}
          </ul>

          {event.recommendations.length > 0 && (
            <>
              <div className="section-title" style={{ marginTop: 'var(--sp-4)' }}>
                What you could do
              </div>
              <div className="stack" style={{ gap: 8 }}>
                {event.recommendations.map((r, i) => (
                  <div className="callout rec" key={i}>
                    {r}
                  </div>
                ))}
              </div>
            </>
          )}

          <div className="section-title" style={{ marginTop: 'var(--sp-4)' }}>
            Sequence context
          </div>
          <div className="row wrap" style={{ gap: 8 }}>
            {previous ? (
              <button className="btn btn-sm" onClick={() => onSelect(previous)}>
                ← #{previous.sequence} {previous.actionType}
              </button>
            ) : (
              <span className="dim" style={{ fontSize: 12 }}>
                first action
              </span>
            )}
            {next ? (
              <button className="btn btn-sm" onClick={() => onSelect(next)}>
                #{next.sequence} {next.actionType} →
              </button>
            ) : (
              <span className="dim" style={{ fontSize: 12 }}>
                last action
              </span>
            )}
          </div>
          <div style={{ height: 24 }} />
          <div className="dim mono" style={{ fontSize: 11 }} title={bandColor(event.riskAssessment.band)}>
            event {event.id}
          </div>
        </div>
      </aside>
    </>
  );
}
