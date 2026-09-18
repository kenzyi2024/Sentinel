import type { SentinelEvent } from '../types';
import { bandColor, shortTime } from '../lib/format';
import { DecisionBadge } from './ui';

interface Props {
  events: SentinelEvent[];
  selectedId?: string;
  onSelect: (event: SentinelEvent) => void;
}

export function EventStream({ events, selectedId, onSelect }: Props) {
  return (
    <table className="stream">
      <thead>
        <tr>
          <th className="seq">#</th>
          <th>Time</th>
          <th>Action</th>
          <th>Resource</th>
          <th>Decision</th>
          <th className="risk-cell">Risk</th>
        </tr>
      </thead>
      <tbody>
        {events.map((event) => (
          <tr
            key={event.id}
            className={event.id === selectedId ? 'selected' : ''}
            onClick={() => onSelect(event)}
          >
            <td className="seq">{event.sequence}</td>
            <td className="dim">{shortTime(event.timestamp)}</td>
            <td>{event.actionType}</td>
            <td className="resource">
              {event.resource}
              {event.injectionFindings.length > 0 && <span className="flag">⚠ INJ</span>}
            </td>
            <td>
              <DecisionBadge decision={event.decision} />
            </td>
            <td className="risk-cell" style={{ color: bandColor(event.riskAssessment.band) }}>
              {event.riskAssessment.score}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
