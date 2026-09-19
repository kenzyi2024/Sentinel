// Enum values mirrored from the backend, so the Scenario Builder works in both live and demo mode
// without depending on a backend round-trip.
import type { AgentArchetype } from '../types';

export const CAPABILITIES = [
  'READ_PROJECT',
  'WRITE_PROJECT',
  'DELETE_FILE',
  'INSTALL_DEPENDENCY',
  'MODIFY_CONFIG',
  'EXECUTE_COMMAND',
  'NETWORK_ACCESS',
  'READ_SECRETS',
] as const;

export const ACTION_TYPES = [
  'LIST_DIRECTORY',
  'READ_FILE',
  'SEARCH_FILES',
  'READ_ENV',
  'WRITE_FILE',
  'DELETE_FILE',
  'MODIFY_CONFIG',
  'INSTALL_DEPENDENCY',
  'EXECUTE_COMMAND',
  'SPAWN_PROCESS',
  'NETWORK_REQUEST',
] as const;

export const ARCHETYPES: AgentArchetype[] = ['DEVELOPER', 'RESEARCH', 'MALICIOUS', 'COMPROMISED'];

/** Default capability grant per archetype (mirrors AgentFactory), used to prefill the builder. */
export const DEFAULT_CAPABILITIES: Record<AgentArchetype, string[]> = {
  DEVELOPER: ['READ_PROJECT', 'WRITE_PROJECT', 'EXECUTE_COMMAND', 'INSTALL_DEPENDENCY'],
  RESEARCH: ['READ_PROJECT'],
  MALICIOUS: ['READ_PROJECT', 'WRITE_PROJECT', 'EXECUTE_COMMAND', 'NETWORK_ACCESS', 'MODIFY_CONFIG', 'DELETE_FILE'],
  COMPROMISED: ['READ_PROJECT', 'WRITE_PROJECT', 'EXECUTE_COMMAND', 'NETWORK_ACCESS'],
};

export function actionUsesCommand(type: string): boolean {
  return type === 'EXECUTE_COMMAND' || type === 'SPAWN_PROCESS';
}

export function actionUsesContent(type: string): boolean {
  return (
    type === 'READ_FILE' ||
    type === 'WRITE_FILE' ||
    type === 'MODIFY_CONFIG' ||
    type === 'NETWORK_REQUEST' ||
    type === 'READ_ENV'
  );
}
