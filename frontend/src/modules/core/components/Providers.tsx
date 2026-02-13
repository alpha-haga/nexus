'use client';

import { SessionProvider } from 'next-auth/react';
import { ReactNode } from 'react';
import { AuthProvider } from '@/contexts/AuthContext';
import { TenantContextProvider } from '@/contexts/TenantContext';

interface ProvidersProps {
  children: ReactNode;
}

export function Providers({ children }: ProvidersProps) {
  return (
    <SessionProvider>
      <TenantContextProvider>
        <AuthProvider>{children}</AuthProvider>
      </TenantContextProvider>
    </SessionProvider>
  );
}
