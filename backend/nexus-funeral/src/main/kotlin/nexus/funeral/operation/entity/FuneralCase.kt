package nexus.funeral.operation.entity

import jakarta.persistence.*
import nexus.core.id.CorporationId
import nexus.core.id.FuneralCaseId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 葬祭案件エンティティ
 *
 * 業務モジュール - identity/group への直接更新禁止
 * gojo への更新禁止（参照のみ）
 */
@Entity
@Table(name = "funeral_cases")
class FuneralCase(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "corporation_id", nullable = false)
    val corporationId: String,

    /** 喪主 PersonId */
    @Column(name = "chief_mourner_person_id", nullable = false)
    val chiefMournerPersonId: String,

    /** 故人 PersonId */
    @Column(name = "deceased_person_id")
    var deceasedPersonId: String? = null,

    /** 使用する互助会契約ID（参照のみ） */
    @Column(name = "gojo_contract_id")
    var gojoContractId: String? = null,

    @Column(name = "plan_code")
    var planCode: String? = null,

    @Column(name = "plan_name")
    var planName: String? = null,

    /** 葬儀日 */
    @Column(name = "ceremony_date")
    var ceremonyDate: LocalDate? = null,

    /** 会場 */
    @Column(name = "venue")
    var venue: String? = null,

    /** 総額 */
    @Column(name = "total_amount")
    var totalAmount: Long = 0,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: FuneralStatus = FuneralStatus.INQUIRY,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    @Column(name = "version")
    var version: Long = 0
) {
    fun toCaseId(): FuneralCaseId = FuneralCaseId(id)
    fun toCorporationId(): CorporationId = CorporationId(corporationId)
    fun chiefMournerPersonId(): PersonId = PersonId(chiefMournerPersonId)
    fun deceasedPersonId(): PersonId? = deceasedPersonId?.let { PersonId(it) }
    fun gojoContractId(): GojoContractId? = gojoContractId?.let { GojoContractId(it) }

    companion object {
        fun create(
            caseId: FuneralCaseId,
            corporationId: CorporationId,
            chiefMournerPersonId: PersonId
        ): FuneralCase {
            return FuneralCase(
                id = caseId.value,
                corporationId = corporationId.value,
                chiefMournerPersonId = chiefMournerPersonId.value
            )
        }
    }
}

enum class FuneralStatus {
    INQUIRY,        // 問い合わせ
    CONSULTATION,   // 相談中
    CONTRACTED,     // 契約済み
    PREPARATION,    // 準備中
    COMPLETED,      // 施行完了
    CANCELLED       // キャンセル
}
