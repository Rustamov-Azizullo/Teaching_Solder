import { can } from './permissions';

describe('can', () => {
  it.each([
    ['GROUP_LEADER', 'soldierWrite', false],
    ['TMIBB', 'dashboardOtm', true],
    ['TMIBB', 'dashboardVocational', false],
    ['SYSTEM_ADMIN', 'admin', true],
    ['HKTB', 'admin', false],
    ['HKTB', 'dictionaryWrite', true],
    ['PSYCHOLOGIST', 'questionnaireWrite', true],
    ['UNIT_OPERATOR', 'questionnaireWrite', false],
    ['MEGA_SUPER_ADMIN', 'admin', true],
    ['MEGA_SUPER_ADMIN', 'soldierWrite', true],
    ['SUPER_ADMIN', 'dashboardOtm', true],
    ['ADMIN', 'assignmentDecide', true],
    ['USER', 'employment', true],
  ] as const)('%s / %s -> %s', (role, capability, expected) => {
    expect(can(role, capability)).toBe(expected);
  });

  it('denies every capability when the role is undefined', () => {
    expect(can(undefined, 'admin')).toBe(false);
    expect(can(undefined, 'soldierRead')).toBe(false);
  });
});
