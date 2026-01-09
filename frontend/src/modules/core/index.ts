/**
 * core モジュール
 *
 * 共通コンポーネント・ユーティリティ
 *
 * ルール:
 * - 他のモジュールから直接 import 可能
 * - 業務モジュールへの依存は禁止
 */

// 共通コンポーネントをエクスポート
export { AppLayout } from './components/AppLayout';
export { Header } from './components/Header';
export { Sidebar } from './components/Sidebar';
export { Providers } from './components/Providers';

// 共通ユーティリティをエクスポート
// export { formatDate } from './utils/date';
// export { formatCurrency } from './utils/currency';
