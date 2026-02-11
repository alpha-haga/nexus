package nexus.group.query

/**
 * 契約詳細画面の TODO カード群に対応するサブリソース QueryService 群。
 *
 * P2-6：API 分離単位を固定するために interface のみ定義する。
 * 実装（JDBC/JPA）は P2-7 以降で infrastructure 層に追加する。
 */

/** 契約内容（追加情報） */
interface GroupContractContractContentsQueryService {
    fun getContractContents(cmpCd: String, contractNo: String): GroupContractContractContentsDto?
}

/** 担当者情報 */
interface GroupContractStaffQueryService {
    fun getStaffs(cmpCd: String, contractNo: String): GroupContractStaffDto?
}

/** 口座情報 */
interface GroupContractBankAccountQueryService {
    fun getBankAccount(cmpCd: String, contractNo: String): GroupContractBankAccountDto?
}

/** 入金情報 */
interface GroupContractReceiptQueryService {
    fun getReceipts(cmpCd: String, contractNo: String): GroupContractReceiptDto
}

/** 入金情報（将来予定/旧名称: payment トップドメインとの混同を避けるため receipt を採用） */
interface GroupContractPaymentQueryService {
    fun getPayments(cmpCd: String, contractNo: String): GroupContractPaymentDto?
}

/** 対応履歴 */
interface GroupContractActivityHistoryQueryService {
    fun getActivityHistory(cmpCd: String, contractNo: String): GroupContractActivityHistoryDto?
}
