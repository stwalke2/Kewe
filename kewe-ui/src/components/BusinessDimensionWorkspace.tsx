import { useEffect, useMemo, useState } from 'react';

type PrimaryTab = 'dimension' | 'settings' | 'roles' | 'security' | 'history';
type SettingsTab = 'general' | 'defaults-allowable' | 'charging' | 'visibility';

interface BusinessDimensionWorkspaceProps {
  title: string;
  definition?: string;
  code: string;
  name: string;
  effectiveDate?: string;
  includedHierarchies?: string[];
  isType: boolean;
  onSave?: (draft: { definition?: string; code: string; name: string; includedHierarchies?: string[] }) => Promise<void>;
}

function formatDate(value?: string): string {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toISOString().slice(0, 10);
}

export function BusinessDimensionWorkspace({
  title,
  definition,
  code,
  name,
  effectiveDate,
  includedHierarchies,
  isType,
  onSave,
}: BusinessDimensionWorkspaceProps) {
  const [tab, setTab] = useState<PrimaryTab>('dimension');
  const [settingsTab, setSettingsTab] = useState<SettingsTab>('general');
  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [draft, setDraft] = useState({
    definition: definition ?? '',
    code,
    name,
    effectiveDate: formatDate(effectiveDate),
    includedHierarchies: (includedHierarchies ?? []).join(', '),
  });

  useEffect(() => {
    setDraft({
      definition: definition ?? '',
      code,
      name,
      effectiveDate: formatDate(effectiveDate),
      includedHierarchies: (includedHierarchies ?? []).join(', '),
    });
    setEditMode(false);
  }, [code, definition, effectiveDate, includedHierarchies, name]);

  const hierarchyLabel = useMemo(() => {
    if (!includedHierarchies || includedHierarchies.length === 0) {
      return 'None assigned';
    }
    return includedHierarchies.join(', ');
  }, [includedHierarchies]);

  const handleSave = async () => {
    if (!onSave) return;
    setSaving(true);
    setMessage(null);
    try {
      await onSave({
        definition: draft.definition || undefined,
        code: draft.code,
        name: draft.name,
        includedHierarchies: draft.includedHierarchies.split(',').map((value) => value.trim()).filter(Boolean),
      });
      setEditMode(false);
      setMessage('Saved successfully.');
    } catch {
      setMessage('Unable to save changes.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="page-section">
      <div className="card">
        <div className="segmented-control tab-row">
          <button type="button" className={`segment${tab === 'dimension' ? ' active' : ''}`} onClick={() => setTab('dimension')}>
            {title}
          </button>
          <button type="button" className={`segment${tab === 'settings' ? ' active' : ''}`} onClick={() => setTab('settings')}>
            Settings
          </button>
          <button type="button" className={`segment${tab === 'roles' ? ' active' : ''}`} onClick={() => setTab('roles')}>
            Roles
          </button>
          <button type="button" className={`segment${tab === 'security' ? ' active' : ''}`} onClick={() => setTab('security')}>
            Security
          </button>
          <button type="button" className={`segment${tab === 'history' ? ' active' : ''}`} onClick={() => setTab('history')}>
            Change History
          </button>
        </div>

        {tab === 'dimension' && (
          <>
            <div className="form-grid dimension-home-grid">
              <label>
                Definition
                <input value={draft.definition} readOnly={!editMode} onChange={(event) => setDraft({ ...draft, definition: event.target.value })} placeholder="No definition provided." />
              </label>
              <label>
                Code
                <input value={draft.code} readOnly />
              </label>
              <label>
                Name
                <input value={draft.name} readOnly={!editMode} onChange={(event) => setDraft({ ...draft, name: event.target.value })} />
              </label>
              <label>
                Effective Date
                <input value={draft.effectiveDate} readOnly />
              </label>
              {!isType && (
                <label className="dimension-home-full-width">
                  Included in Hierarchies
                  <input
                    value={editMode ? draft.includedHierarchies : hierarchyLabel}
                    readOnly={!editMode}
                    onChange={(event) => setDraft({ ...draft, includedHierarchies: event.target.value })}
                  />
                </label>
              )}
            </div>
            {onSave && (
              <div className="actions-row">
                {!editMode && <button className="btn btn-primary" onClick={() => setEditMode(true)}>Edit</button>}
                {editMode && <>
                  <button className="btn btn-primary" disabled={saving} onClick={() => void handleSave()}>{saving ? 'Saving…' : 'Save'}</button>
                  <button className="btn btn-secondary" disabled={saving} onClick={() => setEditMode(false)}>Cancel</button>
                </>}
              </div>
            )}
            {message && <p className="message success">{message}</p>}
          </>
        )}

        {tab === 'settings' && (
          <div className="settings-panel-placeholder">
            <div className="segmented-control tab-row">
              <button type="button" className={`segment${settingsTab === 'general' ? ' active' : ''}`} onClick={() => setSettingsTab('general')}>
                General
              </button>
              <button
                type="button"
                className={`segment${settingsTab === 'defaults-allowable' ? ' active' : ''}`}
                onClick={() => setSettingsTab('defaults-allowable')}
              >
                Defaults and Allowable Dimensions
              </button>
              <button type="button" className={`segment${settingsTab === 'charging' ? ' active' : ''}`} onClick={() => setSettingsTab('charging')}>
                Charging
              </button>
              <button type="button" className={`segment${settingsTab === 'visibility' ? ' active' : ''}`} onClick={() => setSettingsTab('visibility')}>
                Visibility
              </button>
            </div>
            <p className="subtle">{settingsTab.replace('-', ' ')} options will be added in a later step.</p>
          </div>
        )}

        {tab !== 'dimension' && tab !== 'settings' && (
          <div className="settings-panel-placeholder">
            <p className="subtle">No options configured yet for this tab.</p>
          </div>
        )}
      </div>
    </section>
  );
}
