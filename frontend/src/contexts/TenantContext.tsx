'use client';

import { createContext, useContext, useState, ReactNode } from 'react';

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

  const setTenant = (tenant: TenantId) => {
    setSelectedTenant(tenant);
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
