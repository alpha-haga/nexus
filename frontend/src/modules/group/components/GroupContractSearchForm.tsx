'use client';

import { useState, useEffect, FormEvent } from 'react';
import { groupService } from '@/services/group';
import type { GroupContractSearchCondition, Company } from '@/types';
import { CompanySelector } from './CompanySelector';

interface GroupContractSearchFormProps {
  onSearch: (condition: GroupContractSearchCondition) => void;
  value: GroupContractSearchCondition;
  onChange: (next: GroupContractSearchCondition) => void;
  loading?: boolean;
  disabled?: boolean;
  variant?: 'basic' | 'full';
  showActions?: boolean;
}

/**
 * trim 対象は string フィールドのみ。
 * この検索条件では「配列は cmpCds だけ」なので、そこだけ除外する。
 */
type StringConditionKeys = Exclude<keyof GroupContractSearchCondition, 'cmpCds'>;

const TRIM_KEYS: readonly StringConditionKeys[] = [
  'contractReceiptYmdFrom',
  'contractReceiptYmdTo',
  'contractNo',
  'contractorName',
  'staffName',
  'telNo',
  'bosyuCd',
  'courseCd',
  'courseName',
  'contractStatusKbn',
] as const;

const trimStringField = (obj: GroupContractSearchCondition, key: StringConditionKeys) => {
  const v = obj[key];
  if (typeof v === 'string') {
    const t = v.trim();
    obj[key] = t === '' ? undefined : t;
  }
};

export function GroupContractSearchForm({
  onSearch,
  value,
  onChange,
  loading = false,
  disabled = false,
  variant = 'full',
  showActions = true,
}: GroupContractSearchFormProps) {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [companiesLoading, setCompaniesLoading] = useState(false);
  const [companiesError, setCompaniesError] = useState<string | null>(null);

  // 法人一覧を取得
  useEffect(() => {
    setCompaniesLoading(true);
    setCompaniesError(null);
    groupService
      .getCompanies()
      .then((data) => {
        setCompanies(data);
        setCompaniesLoading(false);
      })
      .catch((err: unknown) => {
        let errorMessage = '法人一覧の取得に失敗しました';
      
        if (err && typeof err === 'object') {
          // ApiError 等、Error を継承していないが message を持つケース
          if ('message' in err && typeof (err as any).message === 'string') {
            errorMessage = (err as any).message;
          } else if (err instanceof Error) {
            errorMessage = err.message;
          }
        }
      
        setCompaniesError(errorMessage);
        setCompaniesLoading(false);
      });
  }, []);

  // selectedCmpCdsをvalueから取得
  const selectedCmpCds = value.cmpCds ? new Set(value.cmpCds) : null;

  const handleCmpCdsChange = (newCmpCds: Set<string> | null) => {
    onChange({
      ...value,
      cmpCds: newCmpCds === null ? undefined : Array.from(newCmpCds),
    });
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (disabled) return;

    const trimmedCondition: GroupContractSearchCondition = { ...value };

    // string フィールドのみ trim（cmpCds は対象外）
    for (const key of TRIM_KEYS) {
      trimStringField(trimmedCondition, key);
    }

    // cmpCds は配列なので trim しない
    // 空配列を送らない方針なら、ここで undefined に正規化
    if (Array.isArray(trimmedCondition.cmpCds) && trimmedCondition.cmpCds.length === 0) {
      trimmedCondition.cmpCds = undefined;
    }

    onSearch(trimmedCondition);
  };

  const handleReset = () => {
    onChange({});
    // Reset は検索を実行しない（初期検索禁止・明示的検索のみ）
  };

  const isBasic = variant === 'basic';

  return (
    <form onSubmit={handleSubmit} className={variant === 'basic' ? 'space-y-4' : 'card p-4 space-y-4'}>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {/* 法人 */}
        <div>
          <CompanySelector
            companies={companies}
            selectedCmpCds={selectedCmpCds}
            onChange={handleCmpCdsChange}
            loading={companiesLoading}
            error={companiesError}
            disabled={disabled}
          />
        </div>

        {/* 契約番号 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            契約番号
          </label>
          <input
            type="text"
            value={value.contractNo || ''}
            onChange={(e) => onChange({ ...value, contractNo: e.target.value || undefined })}
            className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
            maxLength={20}
            disabled={disabled}
          />
        </div>

        {/* 契約者氏名 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            契約者氏名
          </label>
          <input
            type="text"
            placeholder="山田 / ヤマダ など"
            value={value.contractorName || ''}
            onChange={(e) =>
              onChange({ ...value, contractorName: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
            maxLength={50}
            disabled={disabled}
          />
        </div>

        {/* 電話番号 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            電話番号
          </label>
          <input
            type="text"
            value={value.telNo || ''}
            onChange={(e) => onChange({ ...value, telNo: e.target.value || undefined })}
            className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
            maxLength={20}
            disabled={disabled}
          />
        </div>

        {/* 以下は full のときのみ表示 */}
        {!isBasic && (
          <>
            {/* 契約状態区分 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                契約状態区分
              </label>
              <input
                type="text"
                value={value.contractStatusKbn || ''}
                onChange={(e) =>
                  onChange({ ...value, contractStatusKbn: e.target.value || undefined })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={10}
                disabled={disabled}
              />
            </div>

            {/* 契約受付日（開始） */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                契約受付日（開始）
              </label>
              <input
                type="text"
                placeholder="YYYYMMDD"
                value={value.contractReceiptYmdFrom || ''}
                onChange={(e) =>
                  onChange({ ...value, contractReceiptYmdFrom: e.target.value || undefined })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={8}
                disabled={disabled}
              />
            </div>

            {/* 契約受付日（終了） */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                契約受付日（終了）
              </label>
              <input
                type="text"
                placeholder="YYYYMMDD"
                value={value.contractReceiptYmdTo || ''}
                onChange={(e) =>
                  onChange({ ...value, contractReceiptYmdTo: e.target.value || undefined })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={8}
                disabled={disabled}
              />
            </div>

            {/* 募集コード */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                募集コード
              </label>
              <input
                type="text"
                value={value.bosyuCd || ''}
                onChange={(e) => onChange({ ...value, bosyuCd: e.target.value || undefined })}
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={20}
                disabled={disabled}
              />
            </div>

            {/* 担当者氏名 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                担当者氏名
              </label>
              <input
                type="text"
                placeholder="佐藤 / サトウ など"
                value={value.staffName || ''}
                onChange={(e) => onChange({ ...value, staffName: e.target.value || undefined })}
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={50}
                disabled={disabled}
              />
            </div>

            {/* コースコード */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                コースコード
              </label>
              <input
                type="text"
                value={value.courseCd || ''}
                onChange={(e) => onChange({ ...value, courseCd: e.target.value || undefined })}
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={20}
                disabled={disabled}
              />
            </div>

            {/* コース名 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                コース名
              </label>
              <input
                type="text"
                placeholder="例）○○コース"
                value={value.courseName || ''}
                onChange={(e) => onChange({ ...value, courseName: e.target.value || undefined })}
                className="w-full px-3 py-2 border border-gray-300 rounded disabled:opacity-50 disabled:bg-gray-100"
                maxLength={50}
                disabled={disabled}
              />
            </div>
          </>
        )}
      </div>

      {showActions && (
        <div className="flex gap-2 pt-2">
          <button
            type="submit"
            disabled={loading || disabled}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? '検索中...' : '検索'}
          </button>
          <button
            type="button"
            onClick={handleReset}
            disabled={loading || disabled}
            className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            リセット
          </button>
        </div>
      )}
    </form>
  );
}
