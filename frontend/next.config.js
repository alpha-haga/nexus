/** @type {import('next').NextConfig} */
const nextConfig = {
  // App Router を使用
  experimental: {
    // 必要に応じて実験的機能を有効化
  },

  // API プロキシ設定（開発時）
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: 'http://localhost:8080/api/v1/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
