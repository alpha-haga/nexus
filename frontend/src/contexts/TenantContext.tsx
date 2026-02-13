'use client';

import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { getSavedTenant, saveTenant } from '@/modules/core/utils/tenantStorage';

export type TenantId = string | null;

interface TenantContextType {
  selectedTenant: TenantId;
  setTenant: (tenant: TenantId) => void;
}

const TenantContext = createContext<TenantContextType | undefined>(undefined);

interface TenantContextProviderProps {
  children: ReactNode;
}

export function TenantContextProvider({ children }: TenantContextProviderProps) {
  const [selectedTenant, setSelectedTenant] = useState<TenantId>(null);

  // 初期化時にsessionStorageから復元
  useEffect(() => {
    const saved = getSavedTenant();
    if (saved) {
      setSelectedTenant(saved);
    }
  }, []);

  const setTenant = (tenant: TenantId) => {
    setSelectedTenant(tenant);
    saveTenant(tenant);
  };

  return (
    <TenantContext.Provider value={{ selectedTenant, setTenant }}>
      {children}
    </TenantContext.Provider>
  );
}

export function useTenantContext() {
  const context = useContext(TenantContext);
  if (context === undefined) {
    throw new Error('useTenantContext must be used within a TenantContextProvider');
  }
  return context;
}
