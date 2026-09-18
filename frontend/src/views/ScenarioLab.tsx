import type { ScenarioInfo } from '../types';

interface Props {
  scenarios: ScenarioInfo[];
  running: boolean;
  onRun: (scenarioId: string) => void;
}

export function ScenarioLab({ scenarios, running, onRun }: Props) {
  return (
    <div>
      <div className="between" style={{ marginBottom: 'var(--sp-4)' }}>
        <div>
          <div className="eyebrow">Scenario Lab</div>
          <h2 style={{ fontSize: 22, marginTop: 6 }}>Launch a simulation</h2>
        </div>
      </div>
      <p className="muted" style={{ maxWidth: 620, marginBottom: 'var(--sp-5)' }}>
        Each scenario scripts an agent against a small project — some files carry hidden instructions. Run one to
        watch Sentinel evaluate every action in real time.
      </p>

      <div className="grid-cards">
        {scenarios.map((s) => (
          <div className="panel scenario-card" key={s.id}>
            <div>
              <div className="meta-row" style={{ marginBottom: 8 }}>
                <span className="chip">{s.archetype}</span>
                {s.grantedCapabilities.slice(0, 3).map((c) => (
                  <span className="chip" key={c}>
                    {c}
                  </span>
                ))}
                {s.grantedCapabilities.length > 3 && <span className="chip">+{s.grantedCapabilities.length - 3}</span>}
              </div>
              <h3>{s.name}</h3>
            </div>
            <p className="desc">{s.description}</p>
            <div>
              <div className="eyebrow">Threat model</div>
              <div className="muted" style={{ fontSize: 12.5, marginTop: 2 }}>
                {s.threatModel}
              </div>
            </div>
            <div>
              <div className="eyebrow">Expected outcome</div>
              <div className="muted" style={{ fontSize: 12.5, marginTop: 2 }}>
                {s.expectedBehavior}
              </div>
            </div>
            <button className="btn btn-primary" disabled={running} onClick={() => onRun(s.id)}>
              ▶ Run scenario · {s.actionCount} actions
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
