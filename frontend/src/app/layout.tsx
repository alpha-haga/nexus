import type { Metadata } from 'next';
import { Providers } from '@/modules/core/components/Providers';
import '@/modules/core/styles/globals.css';

export const metadata: Metadata = {
  title: 'NEXUS - 互助会・葬祭・冠婚・ポイント統合システム',
  description: '複数法人を横断するFEDERATION型アーキテクチャの統合システム',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ja">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
