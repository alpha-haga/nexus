'use client';

import { SessionProvider } from 'next-auth/react';
import { ReactNode } from 'react';
import { TenantContextProvider } from '@/contexts/TenantContext';

interface ProvidersProps {
  children: ReactNode;
}

export function Providers({ children }: ProvidersProps) {
  return (
    <TenantContextProvider>
      <SessionProvider>{children}</SessionProvider>
    </TenantContextProvider>
  );
}
