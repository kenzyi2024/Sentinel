import type { Mode, Policy, PolicyEffect } from '../types';
import { effectLabel } from '../lib/format';

interface Props {
  policies: Policy[];
  mode: Mode;
}

function effectClass(effect: PolicyEffect): string {
  switch (effect) {
    case 'DENY':
      return 'critical';
    case 'REQUIRE_APPROVAL':
      return 'high';
    case 'WARN':
      return 'medium';
    case 'ALLOW':
      return 'low';
    default:
      return 'neutral';
  }
}

function scopeOf(p: Policy): string {
  const parts: string[] = [];
  parts.push(p.archetypes.length ? p.archetypes.join(' / ') : 'any agent');
  parts.push(p.actionTypes.length ? p.actionTypes.join(' / ') : 'any action');
  if (p.resourcePattern) parts.push(`path ${p.resourcePattern}`);
  if (p.minSensitivity) parts.push(`≥ ${p.minSensitivity}`);
  if (p.requiredCapability) parts.push(`needs ${p.requiredCapability}`);
  return parts.join(' · ');
}

export function Policies({ policies, mode }: Props) {
  return (
    <div>
      <div className="eyebrow">Policy engine</div>
      <h2 style={{ fontSize: 22, marginTop: 6, marginBottom: 8 }}>Active policies</h2>
      <p className="muted" style={{ maxWidth: 680, marginBottom: 'var(--sp-4)' }}>
        Policies are declarative data, evaluated on every action. When several match, the strongest effect wins. They
        are stored in the database and {mode === 'live' ? 'editable' : 'fully editable'} through the REST API
        (<code>/api/policies</code>) — no code change or restart required.
      </p>

      <div className="panel">
        <table className="ptable">
          <thead>
            <tr>
              <th style={{ width: 40 }}>Pri</th>
              <th>Policy</th>
              <th>Scope</th>
              <th>Effect</th>
              <th>Reason</th>
            </tr>
          </thead>
          <tbody>
            {policies.map((p) => (
              <tr key={p.id}>
                <td className="mono dim">{p.priority}</td>
                <td className="pname">{p.name}</td>
                <td className="mono dim" style={{ fontSize: 11.5 }}>
                  {scopeOf(p)}
                </td>
                <td>
                  <span className={`badge ${effectClass(p.effect)}`}>{effectLabel(p.effect)}</span>
                </td>
                <td className="preason">{p.reason}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
