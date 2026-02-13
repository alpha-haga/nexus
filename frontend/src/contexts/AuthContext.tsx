'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import type { AuthContext, BootstrapResponse } from '@/types/auth';
import { useTenantContext } from '@/contexts/TenantContext';
import { getSavedTenant } from '@/modules/core/utils/tenantStorage';

const AuthContext = createContext<AuthContext | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { data: session, status } = useSession();
  const { selectedTenant, setTenant } = useTenantContext();
  
  // 状態
  const [user, setUser] = useState<AuthContext['user']>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [availableCompanies, setAvailableCompanies] = useState<AuthContext['availableCompanies']>([]);
  const [hasIntegrationAccess, setHasIntegrationAccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isInitialized, setIsInitialized] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  // ログイン後に bootstrap API を呼び出し
  useEffect(() => {
    if (status === 'authenticated' && session?.accessToken) {
      bootstrap();
    } else if (status === 'unauthenticated') {
      setIsLoading(false);
    }
  }, [status, session]);

  const bootstrap = async () => {
    try {
      setIsLoading(true);
      setError(null);

      const response = await fetch('/api/v1/auth/bootstrap', {
        headers: {
          Authorization: `Bearer ${session?.accessToken}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Bootstrap failed: ${response.status}`);
      }

      const data: BootstrapResponse = await response.json();

      // 状態を一括設定
      setUser(data.user);
      setRoles(data.roles);
      setAvailableCompanies(data.availableCompanies);
      setHasIntegrationAccess(data.hasIntegrationAccess);

      // Default tenant の決定と設定
      const saved = getSavedTenant();
      const savedExists = saved && data.availableCompanies.some(c => c.cmpCd === saved);

      // tenantStorage を唯一の正とする（クロージャ問題回避）
      const currentTenant = getSavedTenant();

      // 既に保存済み tenant がある場合は上書きしない
      if (!currentTenant) {
        const toSelect =
          (savedExists ? saved : null) ??
          (data.availableCompanies[0]?.cmpCd ?? null);

        if (toSelect) {
          setTenant(toSelect); // setTenant 内で storage も更新される
        }
      }

      setIsInitialized(true);
    } catch (err) {
      console.error('Bootstrap failed:', err);
      setError(err instanceof Error ? err : new Error('Unknown error'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        roles,
        availableCompanies,
        hasIntegrationAccess,
        isLoading,
        isInitialized,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
