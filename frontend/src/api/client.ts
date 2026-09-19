import type {
  Agent,
  CustomScenarioInput,
  EngineMeta,
  Mode,
  Policy,
  RunDetail,
  RunSummary,
  ScenarioInfo,
} from '../types';
import { demoAgents, demoPolicies, demoRunList, demoRunsByScenario, demoScenarios } from '../demo/loader';

const API_BASE = (import.meta.env.VITE_API_BASE as string | undefined) ?? 'http://localhost:8080/api';
const HEALTH_TIMEOUT_MS = 1200;

export interface SentinelApi {
  readonly mode: Mode;
  scenarios(): Promise<ScenarioInfo[]>;
  agents(): Promise<Agent[]>;
  policies(): Promise<Policy[]>;
  runs(): Promise<RunSummary[]>;
  run(id: string): Promise<RunDetail>;
  createRun(scenarioId: string): Promise<RunDetail>;
  createCustomRun(input: CustomScenarioInput): Promise<RunDetail>;
  meta(): Promise<EngineMeta | null>;
}

/** Probe the backend; fall back to bundled demo data if it is unreachable. */
export async function createApi(): Promise<SentinelApi> {
  const reachable = await backendReachable();
  return reachable ? new LiveApi() : new DemoApi();
}

async function backendReachable(): Promise<boolean> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), HEALTH_TIMEOUT_MS);
  try {
    const res = await fetch(`${API_BASE}/health`, { signal: controller.signal });
    return res.ok;
  } catch {
    return false;
  } finally {
    clearTimeout(timer);
  }
}

class LiveApi implements SentinelApi {
  readonly mode: Mode = 'live';

  scenarios() {
    return this.get<ScenarioInfo[]>('/scenarios');
  }
  agents() {
    return this.get<Agent[]>('/agents');
  }
  policies() {
    return this.get<Policy[]>('/policies');
  }
  runs() {
    return this.get<RunSummary[]>('/runs');
  }
  run(id: string) {
    return this.get<RunDetail>(`/runs/${id}`);
  }
  createRun(scenarioId: string) {
    return this.post<RunDetail>('/runs', { scenarioId });
  }
  createCustomRun(input: CustomScenarioInput) {
    return this.post<RunDetail>('/runs/custom', input);
  }
  meta() {
    return this.get<EngineMeta>('/meta').catch(() => null);
  }

  private async get<T>(path: string): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`);
    if (!res.ok) throw new Error(`GET ${path} → ${res.status}`);
    return res.json() as Promise<T>;
  }

  private async post<T>(path: string, body: unknown): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`POST ${path} → ${res.status}`);
    return res.json() as Promise<T>;
  }
}

/** Serves the bundled fixtures. `createRun` returns the scenario's precomputed run. */
class DemoApi implements SentinelApi {
  readonly mode: Mode = 'demo';

  async scenarios() {
    return demoScenarios;
  }
  async agents() {
    return demoAgents;
  }
  async policies() {
    return demoPolicies;
  }
  async runs() {
    return demoRunList.map((r) => r.summary);
  }
  async run(id: string) {
    const found = demoRunList.find((r) => r.summary.id === id);
    if (!found) throw new Error(`Run not found: ${id}`);
    return found;
  }
  async createRun(scenarioId: string) {
    const found = demoRunsByScenario[scenarioId];
    if (!found) throw new Error(`No demo run for scenario: ${scenarioId}`);
    return found;
  }
  async createCustomRun(): Promise<RunDetail> {
    // The engine runs on the backend; custom sequences can't be evaluated from bundled fixtures.
    throw new Error('Custom scenarios require the local backend (live mode). Start it and reload.');
  }
  async meta() {
    return {
      name: 'Sentinel',
      version: '0.1.0',
      deterministic: true,
      usesExternalModel: false,
      riskScorers: 7,
      scenarios: demoScenarios.length,
      totalRuns: demoRunList.length,
      totalEvents: demoRunList.reduce((n, r) => n + r.events.length, 0),
      capabilities: [],
      actionTypes: [],
    } satisfies EngineMeta;
  }
}
