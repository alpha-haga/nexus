/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    const bffOrigin = process.env.BFF_ORIGIN ?? 'http://localhost:8080';
    return [
      {
        source: '/api/v1/:path*',
        destination: `${bffOrigin}/api/v1/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
