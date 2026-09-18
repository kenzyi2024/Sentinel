import { Fragment } from 'react';
import type { SentinelEvent } from '../types';
import { decisionClass } from '../lib/format';

interface Props {
  events: SentinelEvent[];
  selectedId?: string;
  onSelect: (event: SentinelEvent) => void;
}

/**
 * The run's actions as a linked chain of nodes, colored by decision. An exclamation marks an event
 * that carries a prompt-injection finding. Clicking a node opens its investigation.
 */
export function ExecutionGraph({ events, selectedId, onSelect }: Props) {
  return (
    <div className="exec-graph">
      {events.map((event, i) => (
        <Fragment key={event.id}>
          {i > 0 && <span className="edge" />}
          <button
            type="button"
            className={`gnode ${decisionClass(event.decision)} ${event.id === selectedId ? 'sel' : ''}`}
            title={`#${event.sequence} ${event.actionType} → ${event.decision} (risk ${event.riskAssessment.score})`}
            onClick={() => onSelect(event)}
          >
            {event.injectionFindings.length > 0 ? '!' : <span className="num">{event.sequence}</span>}
          </button>
        </Fragment>
      ))}
    </div>
  );
}
