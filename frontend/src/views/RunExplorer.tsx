import { useEffect, useState } from 'react';
import type { RunDetail, ScenarioInfo, SentinelEvent } from '../types';
import { EventStream } from '../components/EventStream';
import { ExecutionGraph } from '../components/ExecutionGraph';
import { InvestigationDrawer } from '../components/InvestigationDrawer';
import { RiskGauge, StateBadge } from '../components/ui';

interface Props {
  scenarios: ScenarioInfo[];
  activeRun: RunDetail | null;
  running: boolean;
  onRun: (scenarioId: string) => void;
}

export function RunExplorer({ scenarios, activeRun, running, onRun }: Props) {
  const [selected, setSelected] = useState<SentinelEvent | null>(null);

  // Reset the selected event whenever a new run loads.
  useEffect(() => {
    setSelected(null);
  }, [activeRun?.summary.id]);

  return (
    <div className="explorer">
      <div className="stack">
        <div className="panel">
          <div className="panel-head">
            <h3>Scenarios</h3>
          </div>
          <div style={{ padding: 8 }}>
            {scenarios.map((s) => (
              <button
                key={s.id}
                className={`list-item ${activeRun?.summary.scenarioId === s.id ? 'active' : ''}`}
                onClick={() => onRun(s.id)}
              >
                <div className="li-title">{s.name}</div>
                <div className="li-sub">
                  {s.archetype} · {s.actionCount} actions
                </div>
              </button>
            ))}
          </div>
        </div>

        {activeRun && <RunMeta run={activeRun} />}
      </div>

      <div className="stack">
        {running && (
          <div className="panel">
            <div className="empty">
              <span className="spin" style={{ display: 'inline-block' }}>◠</span> Running simulation…
            </div>
          </div>
        )}

        {!running && !activeRun && (
          <div className="panel">
            <div className="empty">
              Select a scenario on the left to run it, then click any event to investigate the decision.
            </div>
          </div>
        )}

        {!running && activeRun && (
          <>
            <div className="panel">
              <div className="panel-head">
                <h3>Execution graph</h3>
                <span className="dim mono" style={{ fontSize: 11 }}>
                  {activeRun.events.length} events
                </span>
              </div>
              <ExecutionGraph events={activeRun.events} selectedId={selected?.id} onSelect={setSelected} />
            </div>

            <div className="panel">
              <div className="panel-head">
                <h3>Event stream</h3>
                <span className="dim mono" style={{ fontSize: 11 }}>
                  click a row to investigate
                </span>
              </div>
              <div style={{ overflowX: 'auto' }}>
                <EventStream events={activeRun.events} selectedId={selected?.id} onSelect={setSelected} />
              </div>
            </div>
          </>
        )}
      </div>

      <div className="stack">
        {activeRun && <RiskRail run={activeRun} />}
      </div>

      {selected && activeRun && (
        <InvestigationDrawer
          event={selected}
          events={activeRun.events}
          onClose={() => setSelected(null)}
          onSelect={setSelected}
        />
      )}
    </div>
  );
}

function statusColor(status: string): string {
  switch (status) {
    case 'COMPLETED':
      return 'var(--low)';
    case 'TERMINATED':
      return 'var(--critical)';
    case 'RUNNING':
      return 'var(--accent)';
    default:
      return 'var(--fg-muted)';
  }
}

function RunMeta({ run }: { run: RunDetail }) {
  const s = run.summary;
  return (
    <div className="panel">
      <div className="panel-head">
        <h3>Run</h3>
        <span className="tag-state" style={{ color: statusColor(s.status) }}>
          ● {s.status}
        </span>
      </div>
      <div className="panel-body" style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div>
          <div className="eyebrow">Scenario</div>
          <div>{s.scenarioName}</div>
        </div>
        <div>
          <div className="eyebrow">Agent</div>
          <div>
            {s.agentName} <span className="dim">· {s.archetype}</span>
          </div>
        </div>
        <div>
          <div className="eyebrow">Final agent state</div>
          <StateBadge state={run.report.finalAgentState} />
        </div>
      </div>
    </div>
  );
}

function RiskRail({ run }: { run: RunDetail }) {
  const r = run.report;
  const total = Math.max(1, r.allowed + r.requiresApproval + r.blocked);
  return (
    <>
      <div className="panel">
        <div className="panel-head">
          <h3>Risk</h3>
        </div>
        <div className="panel-body">
          <RiskGauge score={r.maxRiskScore} band={r.overallRiskBand} />
          <div className="meter" title="Decision distribution">
            <span style={{ width: `${(r.allowed / total) * 100}%`, background: 'var(--low)' }} />
            <span style={{ width: `${(r.requiresApproval / total) * 100}%`, background: 'var(--medium)' }} />
            <span style={{ width: `${(r.blocked / total) * 100}%`, background: 'var(--critical)' }} />
          </div>
          <div className="stat-row">
            <span className="k">Actions</span>
            <span className="v">{r.totalActions}</span>
          </div>
          <div className="stat-row">
            <span className="k" style={{ color: 'var(--low)' }}>
              Allowed
            </span>
            <span className="v">{r.allowed}</span>
          </div>
          <div className="stat-row">
            <span className="k" style={{ color: 'var(--medium)' }}>
              Review
            </span>
            <span className="v">{r.requiresApproval}</span>
          </div>
          <div className="stat-row">
            <span className="k" style={{ color: 'var(--critical)' }}>
              Blocked
            </span>
            <span className="v">{r.blocked}</span>
          </div>
          <div className="stat-row">
            <span className="k">Injection findings</span>
            <span className="v">{r.injectionFindings}</span>
          </div>
          <div className="stat-row">
            <span className="k">Anomalies</span>
            <span className="v">{r.anomaliesDetected}</span>
          </div>
        </div>
      </div>

      <div className="panel">
        <div className="panel-head">
          <h3>Security report</h3>
        </div>
        <div className="panel-body">
          <div className="section-title">Detected attack patterns</div>
          {r.detectedAttackPatterns.length === 0 ? (
            <p className="dim" style={{ fontSize: 12.5 }}>
              None detected.
            </p>
          ) : (
            <ul className="reason-list" style={{ marginBottom: 'var(--sp-4)' }}>
              {r.detectedAttackPatterns.map((p, i) => (
                <li key={i} style={{ fontSize: 12.5 }}>
                  {p}
                </li>
              ))}
            </ul>
          )}
          <div className="section-title">Recommendations</div>
          <div className="stack" style={{ gap: 8 }}>
            {r.recommendedPolicyChanges.map((rec, i) => (
              <div className="callout rec" key={i} style={{ fontSize: 12.5 }}>
                {rec}
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}
