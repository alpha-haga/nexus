package nexus.core.id

import java.util.UUID

/**
 * NEXUS システム共通ID定義
 *
 * 全てのIDはValue Objectとして不変
 * UUIDベースで法人横断のユニーク性を保証
 */

/** 法人ID - FEDERATION内で一意 */
@JvmInline
value class CorporationId(val value: String) {
    init {
        require(value.isNotBlank()) { "CorporationId must not be blank" }
    }
    companion object {
        fun generate(): CorporationId = CorporationId(UUID.randomUUID().toString())
    }
}

/** 人物ID - システム全体で一意 */
@JvmInline
value class PersonId(val value: String) {
    init {
        require(value.isNotBlank()) { "PersonId must not be blank" }
    }
    companion object {
        fun generate(): PersonId = PersonId(UUID.randomUUID().toString())
    }
}

/** 世帯ID */
@JvmInline
value class HouseholdId(val value: String) {
    init {
        require(value.isNotBlank()) { "HouseholdId must not be blank" }
    }
    companion object {
        fun generate(): HouseholdId = HouseholdId(UUID.randomUUID().toString())
    }
}

/** 互助会契約ID */
@JvmInline
value class GojoContractId(val value: String) {
    init {
        require(value.isNotBlank()) { "GojoContractId must not be blank" }
    }
    companion object {
        fun generate(): GojoContractId = GojoContractId(UUID.randomUUID().toString())
    }
}

/** 葬祭案件ID */
@JvmInline
value class FuneralCaseId(val value: String) {
    init {
        require(value.isNotBlank()) { "FuneralCaseId must not be blank" }
    }
    companion object {
        fun generate(): FuneralCaseId = FuneralCaseId(UUID.randomUUID().toString())
    }
}

/** 冠婚案件ID */
@JvmInline
value class BridalCaseId(val value: String) {
    init {
        require(value.isNotBlank()) { "BridalCaseId must not be blank" }
    }
    companion object {
        fun generate(): BridalCaseId = BridalCaseId(UUID.randomUUID().toString())
    }
}

/** ポイントアカウントID */
@JvmInline
value class PointAccountId(val value: String) {
    init {
        require(value.isNotBlank()) { "PointAccountId must not be blank" }
    }
    companion object {
        fun generate(): PointAccountId = PointAccountId(UUID.randomUUID().toString())
    }
}
