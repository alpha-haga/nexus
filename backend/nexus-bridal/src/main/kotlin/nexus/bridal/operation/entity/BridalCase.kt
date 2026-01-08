package nexus.bridal.operation.entity

import jakarta.persistence.*
import nexus.core.id.BridalCaseId
import nexus.core.id.CorporationId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 冠婚案件エンティティ
 *
 * 業務モジュール - identity/group への直接更新禁止
 * gojo への更新禁止（参照のみ）
 */
@Entity
@Table(name = "bridal_cases")
class BridalCase(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "corporation_id", nullable = false)
    val corporationId: String,

    /** 新郎 PersonId */
    @Column(name = "groom_person_id", nullable = false)
    val groomPersonId: String,

    /** 新婦 PersonId */
    @Column(name = "bride_person_id", nullable = false)
    val bridePersonId: String,

    /** 使用する互助会契約ID（参照のみ） */
    @Column(name = "gojo_contract_id")
    var gojoContractId: String? = null,

    @Column(name = "plan_code")
    var planCode: String? = null,

    @Column(name = "plan_name")
    var planName: String? = null,

    /** 挙式日 */
    @Column(name = "ceremony_date")
    var ceremonyDate: LocalDate? = null,

    /** 会場 */
    @Column(name = "venue")
    var venue: String? = null,

    /** 招待人数 */
    @Column(name = "guest_count")
    var guestCount: Int = 0,

    /** 総額 */
    @Column(name = "total_amount")
    var totalAmount: Long = 0,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: BridalStatus = BridalStatus.INQUIRY,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    @Column(name = "version")
    var version: Long = 0
) {
    fun toCaseId(): BridalCaseId = BridalCaseId(id)
    fun toCorporationId(): CorporationId = CorporationId(corporationId)
    fun groomPersonId(): PersonId = PersonId(groomPersonId)
    fun bridePersonId(): PersonId = PersonId(bridePersonId)
    fun gojoContractId(): GojoContractId? = gojoContractId?.let { GojoContractId(it) }

    companion object {
        fun create(
            caseId: BridalCaseId,
            corporationId: CorporationId,
            groomPersonId: PersonId,
            bridePersonId: PersonId
        ): BridalCase {
            return BridalCase(
                id = caseId.value,
                corporationId = corporationId.value,
                groomPersonId = groomPersonId.value,
                bridePersonId = bridePersonId.value
            )
        }
    }
}

enum class BridalStatus {
    INQUIRY,        // 問い合わせ
    CONSULTATION,   // 相談中
    CONTRACTED,     // 契約済み
    PREPARATION,    // 準備中
    COMPLETED,      // 施行完了
    CANCELLED       // キャンセル
}
