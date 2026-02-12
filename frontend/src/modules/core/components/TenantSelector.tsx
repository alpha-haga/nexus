'use client';

import { useState, useEffect } from 'react';
import { useTenantContext } from '@/contexts/TenantContext';
import { groupService } from '@/services/group';
import type { Company, ApiError } from '@/types';

/**
 * TenantSelector
 * 
 * 法人（Tenant）選択コンポーネント
 * Phase3: 既存APIから法人一覧を取得して表示
 */
export function TenantSelector() {
  const { selectedTenant, setTenant } = useTenantContext();
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  // 法人一覧を取得
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    groupService
      .getCompanies()
      .then((data) => {
        if (cancelled) return;
        setCompanies(data);
        setLoading(false);
      })
      .catch((err: ApiError) => {
        if (cancelled) return;
        setError(err);
        setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        法人 <span className="text-red-500">*</span>
      </label>
      {loading && (
        <div className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-50 text-gray-500">
          読み込み中...
        </div>
      )}
      {error && (
        <div className="w-full px-3 py-2 border border-red-300 rounded bg-red-50 text-red-700">
          エラー: {error.status} {error.message}
        </div>
      )}
      {!loading && !error && companies.length === 0 && (
        <div className="w-full px-3 py-2 border border-amber-300 rounded bg-amber-50 text-amber-700">
          法人が0件です（利用者に問い合わせてください）
        </div>
      )}
      {!loading && !error && companies.length > 0 && (
        <select
          value={selectedTenant || ''}
          onChange={(e) => setTenant(e.target.value || null)}
          className="w-full px-3 py-2 border border-gray-300 rounded"
        >
          <option value="">選択してください</option>
          {companies.map((company) => (
            <option key={company.cmpCd} value={company.cmpCd}>
              {company.cmpShortNm}
            </option>
          ))}
        </select>
      )}
    </div>
  );
}
