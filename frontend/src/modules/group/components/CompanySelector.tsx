'use client';

import { useState, useRef, useEffect } from 'react';
import type { Company } from '@/types';

interface CompanySelectorProps {
  companies: Company[];
  selectedCmpCds: Set<string> | null;
  onChange: (selectedCmpCds: Set<string> | null) => void;
  loading?: boolean;
  error?: string | null;
  disabled?: boolean;
}

export function CompanySelector({
  companies,
  selectedCmpCds,
  onChange,
  loading = false,
  error = null,
  disabled = false,
}: CompanySelectorProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // クリックアウトサイドで閉じる
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const isAllSelected = selectedCmpCds === null;
  const selectedCount = isAllSelected ? companies.length : selectedCmpCds.size;

  const handleToggle = (cmpCd: string) => {
    if (disabled) return;

    if (isAllSelected) {
      // 全選択状態から部分選択へ: クリックした会社だけ選択する
      onChange(new Set([cmpCd]));
    } else {
      const newSet = new Set(selectedCmpCds);
      if (newSet.has(cmpCd)) {
        newSet.delete(cmpCd);
        // 0件になったら全選択に戻す
        onChange(newSet.size > 0 ? newSet : null);
      } else {
        newSet.add(cmpCd);
        // 全件選択になったら全選択状態に
        if (newSet.size === companies.length) {
          onChange(null);
        } else {
          onChange(newSet);
        }
      }
    }
  };

  const handleSelectAll = () => {
    if (disabled) return;
    onChange(null);
  };

  const displayText = isAllSelected
    ? `全法人（${companies.length}件）`
    : `選択中（${selectedCount}件）`;

  return (
    <div className="relative" ref={dropdownRef}>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        法人
      </label>
      <button
        type="button"
        onClick={() => !disabled && setIsOpen(!isOpen)}
        disabled={disabled || loading}
        className="w-full px-3 py-2 border border-gray-300 rounded text-left bg-white disabled:opacity-50 disabled:bg-gray-100 flex items-center justify-between"
      >
        <span className={loading ? 'text-gray-400' : ''}>
          {loading ? '読み込み中...' : error ? 'エラー' : displayText}
        </span>
        <svg
          className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {error && (
        <p className="mt-1 text-sm text-red-600">{error}</p>
      )}

      {isOpen && !loading && !error && companies.length > 0 && (
        <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded shadow-lg max-h-60 overflow-y-auto">
          <div className="p-2 border-b border-gray-200">
            <button
              type="button"
              onClick={handleSelectAll}
              className="text-sm text-blue-600 hover:text-blue-800"
            >
              {isAllSelected ? '✓ 全選択' : '全選択に戻す'}
            </button>
          </div>
          <div className="p-2">
            {companies.map((company) => {
              const isSelected = isAllSelected || selectedCmpCds?.has(company.cmpCd) || false;
              return (
                <label
                  key={company.cmpCd}
                  className="flex items-center p-2 hover:bg-gray-50 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    checked={isSelected}
                    onChange={() => handleToggle(company.cmpCd)}
                    className="mr-2"
                  />
                  <span className="text-sm">
                    {company.cmpShortNm} ({company.cmpCd})
                  </span>
                </label>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
