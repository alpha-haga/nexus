/**
 * gojo モジュール
 *
 * 互助会業務機能
 *
 * ルール:
 * - 他のモジュールへの直接 import は禁止
 * - identity / group への直接更新は禁止
 * - @/services 経由でAPIを呼び出す
 * - @/modules/core のみ import 可能
 *
 * 依存:
 * - @/modules/core
 */

// コンポーネントをエクスポート
// export { ContractForm } from './components/ContractForm';
// export { ContractList } from './components/ContractList';
// export { PaymentHistory } from './components/PaymentHistory';
export { GojoContractsList } from './components/GojoContractsList';