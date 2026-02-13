'use client';

import Link from 'next/link';
import { signOut } from 'next-auth/react';
import React from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useTenantContext } from '@/contexts/TenantContext';

interface HeaderProps {
  onToggleSidebar: () => void;
  isSidebarOpen: boolean;
}

export function Header({ onToggleSidebar, isSidebarOpen }: HeaderProps) {
  const { user, availableCompanies, isLoading, error, isInitialized } = useAuth();
  const { selectedTenant, setTenant } = useTenantContext();

  const handleLogout = () => {
    signOut({ callbackUrl: '/login' });
  };

  const handleCompanyChange = (cmpCd: string) => {
    setTenant(cmpCd);
    // setTenant内でsaveTenantが呼ばれるため、ここでは不要
  };

  // 利用可能法人が1件で、selectedTenantがnullの場合に自動選択
  React.useEffect(() => {
    if (isInitialized && availableCompanies.length === 1 && !selectedTenant) {
      setTenant(availableCompanies[0].cmpCd);
    }
  }, [isInitialized, availableCompanies, selectedTenant, setTenant]);

  const selectedCompany = availableCompanies.find(c => c.cmpCd === selectedTenant);

  return (
    <header className="h-14 bg-white border-b border-gray-200 flex items-center px-4 shrink-0">
      <div className="flex items-center gap-2 w-56 shrink-0">
        <button
          type="button"
          onClick={onToggleSidebar}
          className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
          title="メニュー"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <Link href="/dashboard" className="flex items-center">
          <span className="text-xl font-bold text-primary-700">NEXUS</span>
        </Link>
      </div>
      <div className="flex-1 flex items-center gap-4">
        {/* 法人選択UI */}
        <div className="relative">
          {isLoading ? (
            <div className="animate-pulse h-10 w-40 bg-gray-200 rounded" />
          ) : error ? (
            <div className="text-red-500 text-sm">初期化エラー</div>
          ) : availableCompanies.length === 0 ? (
            <div className="text-gray-500 text-sm px-3 py-2">利用可能な法人がありません</div>
          ) : availableCompanies.length === 1 ? (
            <div className="px-3 py-2 text-sm font-medium text-gray-700">
              {selectedCompany?.companyNameShort ?? selectedCompany?.companyName ?? '法人未選択'}
            </div>
          ) : (
            <select
              value={selectedTenant ?? ''}
              onChange={(e) => handleCompanyChange(e.target.value)}
              className="block w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {availableCompanies.map((company) => (
                <option key={company.cmpCd} value={company.cmpCd}>
                  {company.companyNameShort ?? company.companyName}
                </option>
              ))}
            </select>
          )}
        </div>
      </div>

      {user && (
        <div className="flex items-center gap-3">
          <div className="text-right">
            <p className="text-sm font-medium text-gray-700">{user.username ?? user.email ?? 'ユーザー'}</p>
            <p className="text-xs text-gray-500">{user.email}</p>
          </div>
          <button
            onClick={handleLogout}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
            title="ログアウト"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>
      )}
    </header>
  );
}
