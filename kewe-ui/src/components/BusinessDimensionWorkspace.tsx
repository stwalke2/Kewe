import { useMemo, useState } from 'react';

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
}

function formatDate(value?: string): string {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString();
}

export function BusinessDimensionWorkspace({
  title,
  definition,
  code,
  name,
  effectiveDate,
  includedHierarchies,
  isType,
}: BusinessDimensionWorkspaceProps) {
  const [tab, setTab] = useState<PrimaryTab>('dimension');
  const [settingsTab, setSettingsTab] = useState<SettingsTab>('general');

  const hierarchyLabel = useMemo(() => {
    if (!includedHierarchies || includedHierarchies.length === 0) {
      return 'None assigned';
    }
    return includedHierarchies.join(', ');
  }, [includedHierarchies]);

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
          <div className="form-grid dimension-home-grid">
            <label>
              Definition
              <input value={definition ?? ''} readOnly placeholder="No definition provided." />
            </label>
            <label>
              Code
              <input value={code} readOnly />
            </label>
            <label>
              Name
              <input value={name} readOnly />
            </label>
            <label>
              Effective Date
              <input value={formatDate(effectiveDate)} readOnly />
            </label>
            {!isType && (
              <label className="dimension-home-full-width">
                Included in Hierarchies
                <input value={hierarchyLabel} readOnly />
              </label>
            )}
          </div>
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
