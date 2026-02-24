import { useState } from 'react';
import { helpDefinitions, type HelpTerm } from './definitions';

export function HelpTip({ term }: { term: HelpTerm }) {
  const [open, setOpen] = useState(false);
  const definition = helpDefinitions[term];

  return (
    <span className="help-tip-wrap">
      <button
        type="button"
        className="help-tip"
        aria-label={`Help: ${definition.label}`}
        onFocus={() => setOpen(true)}
        onBlur={() => setOpen(false)}
        onClick={() => setOpen((value) => !value)}
      >
        ?
      </button>
      {open && (
        <span role="tooltip" className="help-tip-popover">
          <strong>{definition.label}</strong>
          <span>{definition.definition}</span>
        </span>
      )}
    </span>
  );
}
