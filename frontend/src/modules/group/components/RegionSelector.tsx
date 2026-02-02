// frontend/src/modules/group/components/RegionSelector.tsx（新規作成）

'use client';

import type { Region } from '@/types';

interface RegionSelectorProps {
  value: Region | null;
  onChange: (region: Region | null) => void;
  disabled?: boolean;
}

export function RegionSelector({ value, onChange, disabled = false }: RegionSelectorProps) {
  const regions: { value: NonNullable<Region>; label: string }[] = [
    { value: 'INTEGRATION', label: '統合DB' },
    { value: 'SAITAMA', label: '埼玉' },
    { value: 'FUKUSHIMA', label: '福島' },
    { value: 'TOCHIGI', label: '栃木' },
  ];

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        Region <span className="text-red-500">*</span>
      </label>
      <select
        value={value || ''}
        onChange={(e) => onChange(e.target.value ? (e.target.value as Region) : null)}
        disabled={disabled}
        className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
      >
        <option value="">選択してください</option>
        {regions.map((region) => (
          <option key={region.value} value={region.value}>
            {region.label}
          </option>
        ))}
      </select>
    </div>
  );
}
