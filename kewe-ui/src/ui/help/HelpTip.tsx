import { useId } from 'react';
import { helpDefinitions, type HelpTerm } from './definitions';

export function HelpTip({ term }: { term: HelpTerm }) {
  const definition = helpDefinitions[term];
  const tooltipId = useId();

  return (
    <span className="help-tip-wrap">
      <button
        type="button"
        className="help-tip"
        aria-label={`Help: ${definition.label}`}
        aria-describedby={tooltipId}
      >
        ?
      </button>
      <span id={tooltipId} role="tooltip" className="help-tip-popover">
        <strong>{definition.label}</strong>
        <span>{definition.definition}</span>
      </span>
    </span>
  );
}
