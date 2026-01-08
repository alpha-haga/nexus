'use client';

import { signOut, useSession } from 'next-auth/react';

export function Header() {
  const { data: session } = useSession();

  const handleLogout = () => {
    signOut({ callbackUrl: '/login' });
  };

  return (
    <header className="bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <h1 className="text-xl font-semibold text-gray-900">NEXUS</h1>
          </div>
          {session && (
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600">{session.user.name}</span>
              <button onClick={handleLogout} className="btn-secondary text-sm">
                ログアウト
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
