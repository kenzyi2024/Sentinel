import { useCallback, useEffect, useState } from 'react';
import type { EngineMeta, Mode, Policy, RunDetail, ScenarioInfo } from './types';
import type { SentinelApi } from './api/client';
import { createApi } from './api/client';
import { Shield } from './components/ui';
import { Landing } from './views/Landing';
import { RunExplorer } from './views/RunExplorer';
import { ScenarioLab } from './views/ScenarioLab';
import { Policies } from './views/Policies';
import { About } from './views/About';
import { APP_NAME } from './config';

type View = 'landing' | 'explorer' | 'scenarios' | 'policies' | 'about';

const NAV: { id: View; label: string }[] = [
  { id: 'explorer', label: 'Run Explorer' },
  { id: 'scenarios', label: 'Scenario Lab' },
  { id: 'policies', label: 'Policies' },
  { id: 'about', label: 'About' },
];

export default function App() {
  const [api, setApi] = useState<SentinelApi | null>(null);
  const [mode, setMode] = useState<Mode>('demo');
  const [view, setView] = useState<View>('landing');
  const [scenarios, setScenarios] = useState<ScenarioInfo[]>([]);
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [meta, setMeta] = useState<EngineMeta | null>(null);
  const [activeRun, setActiveRun] = useState<RunDetail | null>(null);
  const [running, setRunning] = useState(false);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let cancelled = false;
    createApi().then(async (client) => {
      if (cancelled) return;
      setApi(client);
      setMode(client.mode);
      const [sc, po, me] = await Promise.all([client.scenarios(), client.policies(), client.meta()]);
      if (cancelled) return;
      setScenarios(sc);
      setPolicies(po);
      setMeta(me);
      setReady(true);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const runScenario = useCallback(
    async (scenarioId: string) => {
      if (!api) return;
      setView('explorer');
      setRunning(true);
      try {
        setActiveRun(await api.createRun(scenarioId));
      } finally {
        setRunning(false);
      }
    },
    [api],
  );

  return (
    <div className="app">
      <header className="topbar">
        <button className="brand" onClick={() => setView('landing')}>
          <Shield />
          <span>
            {APP_NAME.toLowerCase().slice(0, 3)}
            <b>{APP_NAME.toLowerCase().slice(3)}</b>
          </span>
        </button>
        <nav className="nav">
          {NAV.map((n) => (
            <button key={n.id} className={view === n.id ? 'active' : ''} onClick={() => setView(n.id)}>
              {n.label}
            </button>
          ))}
        </nav>
        <span className="topbar-spacer" />
        <span className={`mode-pill ${mode}`} title={mode === 'live' ? 'Connected to local backend' : 'Running on bundled demo data'}>
          <span className="dot" />
          {mode === 'live' ? 'LIVE BACKEND' : 'DEMO DATA'}
        </span>
      </header>

      <main className="page">
        {!ready && <div className="empty">Loading engine…</div>}
        {ready && view === 'landing' && (
          <Landing onRunDemo={() => runScenario('compromised-agent')} onBrowse={() => setView('scenarios')} meta={meta} />
        )}
        {ready && view === 'explorer' && (
          <RunExplorer scenarios={scenarios} activeRun={activeRun} running={running} onRun={runScenario} />
        )}
        {ready && view === 'scenarios' && <ScenarioLab scenarios={scenarios} running={running} onRun={runScenario} />}
        {ready && view === 'policies' && <Policies policies={policies} mode={mode} />}
        {ready && view === 'about' && <About meta={meta} />}
      </main>

      <footer className="footer">
        {APP_NAME} · deterministic, local AI-agent security engine ·{' '}
        {mode === 'demo' ? 'running on bundled demo data' : 'connected to local backend'}
      </footer>
    </div>
  );
}
