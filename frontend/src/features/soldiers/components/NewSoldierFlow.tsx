import { Steps } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth';
import { soldierLabels } from '../labels';
import type { SourceLookup } from '../types';
import { PinflLookup } from './PinflLookup';
import { SoldierForm } from './SoldierForm';

type Draft = { pinfl: string; lookup?: SourceLookup };

/** Yangi askar: 1) JShShIR va manba tizimga so'rov, 2) yetishmagan ma'lumotlarni to'ldirish. */
export function NewSoldierFlow() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [draft, setDraft] = useState<Draft | null>(null);

  return (
    <>
      <Steps
        current={draft ? 1 : 0}
        style={{ maxWidth: 520, marginBottom: 24 }}
        items={[{ title: soldierLabels.lookup.title }, { title: soldierLabels.createTitle }]}
      />
      {draft ? (
        <SoldierForm
          pinfl={draft.pinfl}
          lookup={draft.lookup}
          defaultUnitId={user?.militaryUnitId ?? undefined}
          onSaved={(saved) => navigate(`/soldiers/${saved.id}`)}
          onCancel={() => setDraft(null)}
        />
      ) : (
        <PinflLookup
          onFound={(pinfl, lookup) => setDraft({ pinfl, lookup })}
          onManual={(pinfl) => setDraft({ pinfl })}
        />
      )}
    </>
  );
}
