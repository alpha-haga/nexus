'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import type { AuthContext, BootstrapResponse } from '@/types/auth';

const AuthContext = createContext<AuthContext | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { data: session, status } = useSession();
  
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

      // Tenantの決定は tenantStorage の復元を唯一の正とする
      // TenantContextProviderが初期化時に復元するため、ここでは何もしない

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
