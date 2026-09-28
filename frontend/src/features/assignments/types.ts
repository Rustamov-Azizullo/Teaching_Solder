export type AssignmentStatus = 'PROPOSED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
export type Direction = 'VOCATIONAL' | 'OTM_PREP';

export type Assignment = {
  id: number;
  militaryUnitId: number;
  militaryUnitName: string;
  institutionId: number;
  institutionName: string;
  direction: Direction;
  status: AssignmentStatus;
  basisDocument: string | null;
  validFrom: string | null;
  validTo: string | null;
  contractNo: string | null;
  contractDate: string | null;
  jointPlan: string | null;
  proposalNote: string | null;
  decisionNote: string | null;
  proposedBy: string;
  decidedBy: string | null;
  createdAt: string;
};

export type ProposalRequest = { militaryUnitId: number; institutionId: number; direction: Direction; note?: string };
export type DecisionRequest = { approve: boolean; basisDocument?: string; validFrom?: string; validTo?: string; note?: string };
export type ContractRequest = { contractNo?: string; contractDate?: string; jointPlan?: string };
