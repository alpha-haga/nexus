package nexus.bff.dto.gojo

/**
 * BFF 返却用の契約ステータス
 * - domain entity の enum に依存しない
 */
enum class ContractStatusDto {
    ACTIVE,
    MATURED,
    USED,
    CANCELLED,
    SUSPENDED
}
