import { SearchOutlined } from '@ant-design/icons';
import { Alert, Button, Input, Space } from 'antd';
import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getErrorMessage } from '@/lib/apiClient';
import { PINFL_PATTERN } from '@/utils/validators';
import { soldierLabels } from '../labels';
import { useSourceLookup } from '../hooks/useSoldiers';
import type { SourceLookup } from '../types';

const SERVICE_UNAVAILABLE = 503;

type PinflLookupProps = {
  onFound: (pinfl: string, lookup: SourceLookup) => void;
  onManual: (pinfl: string) => void;
};

/** Yangi askar formasining 1-qadami: JShShIR kiritiladi va manba tizimga so'rov yuboriladi. */
export function PinflLookup({ onFound, onManual }: PinflLookupProps) {
  const navigate = useNavigate();
  const [pinfl, setPinfl] = useState('');
  const [notice, setNotice] = useState<{ type: 'info' | 'warning' | 'error'; text: string } | null>(null);
  const [existingId, setExistingId] = useState<number | null>(null);
  const { mutateAsync, isPending } = useSourceLookup();
  const isValid = PINFL_PATTERN.test(pinfl);

  const handleLookup = async () => {
    setNotice(null);
    setExistingId(null);
    try {
      const lookup = await mutateAsync(pinfl);
      if (lookup.existingSoldierId) {
        setExistingId(lookup.existingSoldierId);
        setNotice({ type: 'warning', text: soldierLabels.lookup.existing });
        return;
      }
      if (lookup.found) {
        onFound(pinfl, lookup);
        return;
      }
      setNotice({ type: 'info', text: soldierLabels.lookup.notFound });
    } catch (error) {
      const isUnavailable = axios.isAxiosError(error) && error.response?.status === SERVICE_UNAVAILABLE;
      setNotice({ type: isUnavailable ? 'warning' : 'error', text: isUnavailable ? soldierLabels.lookup.unavailable : getErrorMessage(error) });
    }
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%', maxWidth: 520 }}>
      <Input
        size="large"
        value={pinfl}
        maxLength={14}
        inputMode="numeric"
        placeholder={soldierLabels.lookup.placeholder}
        aria-label={soldierLabels.fields.pinfl}
        status={pinfl && !isValid ? 'error' : undefined}
        onChange={(event) => setPinfl(event.target.value.replace(/\D/g, ''))}
        onPressEnter={() => isValid && handleLookup()}
      />
      {pinfl && !isValid && <span style={{ color: '#cf1322' }}>{soldierLabels.lookup.invalidPinfl}</span>}
      <Space wrap>
        <Button type="primary" icon={<SearchOutlined />} disabled={!isValid} loading={isPending} onClick={handleLookup}>
          {soldierLabels.lookup.button}
        </Button>
        <Button disabled={!isValid} onClick={() => onManual(pinfl)}>{soldierLabels.lookup.manual}</Button>
      </Space>
      {notice && (
        <Alert
          type={notice.type}
          showIcon
          message={notice.text}
          action={
            existingId ? (
              <Button size="small" onClick={() => navigate(`/soldiers/${existingId}`)}>{soldierLabels.lookup.openExisting}</Button>
            ) : notice.type !== 'error' ? (
              <Button size="small" onClick={() => onManual(pinfl)}>{soldierLabels.lookup.manual}</Button>
            ) : undefined
          }
        />
      )}
    </Space>
  );
}
