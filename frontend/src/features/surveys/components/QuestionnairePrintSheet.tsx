import { Descriptions, Typography } from 'antd';
import type { Soldier } from '@/features/soldiers';
import { formatDate } from '@/utils/format';
import { surveyLabels } from '../labels';
import type { Questionnaire } from '../types';

type QuestionnairePrintSheetProps = { soldier: Soldier; questionnaire: Questionnaire; districtName: string };

/** Qog'oz anketa ko'rinishi: faqat chop etishda ko'rinadi (styles/print.css). */
export function QuestionnairePrintSheet({ soldier, questionnaire, districtName }: QuestionnairePrintSheetProps) {
  const names = (items: { name: string }[]) => (items.length ? items.map((item) => item.name).join('; ') : '—');
  return (
    <div className="print-only" style={{ fontSize: 13 }}>
      <Typography.Title level={5} style={{ textAlign: 'center' }}>{surveyLabels.print.heading}</Typography.Title>
      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label={surveyLabels.header.date}>{formatDate(questionnaire.filledDate)}</Descriptions.Item>
        <Descriptions.Item label={surveyLabels.header.district}>{districtName}</Descriptions.Item>
        <Descriptions.Item label={surveyLabels.header.unit}>{soldier.militaryUnitName}</Descriptions.Item>
        <Descriptions.Item label="F.I.Sh.">{soldier.fullName}</Descriptions.Item>
        <Descriptions.Item label="JShShIR">{soldier.pinfl}</Descriptions.Item>
        <Descriptions.Item label="Tug'ilgan sana">{formatDate(soldier.birthDate)}</Descriptions.Item>
        <Descriptions.Item label="Pasport">{soldier.passport}</Descriptions.Item>
        <Descriptions.Item label="Telefon">{soldier.phone} ({soldier.phoneKinshipName})</Descriptions.Item>
        <Descriptions.Item label={surveyLabels.q14}>
          {questionnaire.interestDirection?.name ?? '—'}
          {questionnaire.interestOtherText ? `: ${questionnaire.interestOtherText}` : ''}
        </Descriptions.Item>
        <Descriptions.Item label={surveyLabels.q15}>
          {names(questionnaire.futurePlans)}
          {questionnaire.planOtherText ? `: ${questionnaire.planOtherText}` : ''}
        </Descriptions.Item>
        {questionnaire.universityChoices.length > 0 && (
          <>
            <Descriptions.Item label={surveyLabels.q16}>
              {questionnaire.universityChoices.map((choice) => (
                <div key={choice.priority}>{choice.priority}. {choice.university} — {choice.studyDirection}</div>
              ))}
            </Descriptions.Item>
            <Descriptions.Item label={surveyLabels.q17}>{names(questionnaire.specialtySubjects)}</Descriptions.Item>
            <Descriptions.Item label={surveyLabels.q18}>{names(questionnaire.mandatorySubjects)}</Descriptions.Item>
          </>
        )}
      </Descriptions>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 48 }}>
        <span>{surveyLabels.print.soldierSignature}: ____________________</span>
        <span>{surveyLabels.print.psychologistSignature} ({questionnaire.psychologistName}): ____________________</span>
      </div>
    </div>
  );
}
