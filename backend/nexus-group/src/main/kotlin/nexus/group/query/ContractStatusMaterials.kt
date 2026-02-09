package nexus.group.query

/**
 * 契約状態材料の共通インターフェース
 * 
 * GroupContractSearchDto と GroupContractDetailDto の両方で使用可能
 * 契約状態表示文字列組み立てに必要な材料フィールドを定義
 */
interface ContractStatusMaterials {
    val contractStatusKbn: String?
    val contractStatusName: String?
    val dmdStopReasonKbn: String?
    val dmdStopReasonName: String?
    val cancelReasonKbn: String?
    val cancelReasonName: String?
    val zashuReasonKbn: String?
    val zashuReasonName: String?
    val anspApproveKbn: String?
    val anspApproveName: String?
    val torikeshiReasonKbn: String?
    val torikeshiReasonName: String?
    val ecApproveKbn: String?
    val ecApproveName: String?
    val cancelStatusKbn: String?
    val cancelStatusName: String?
}
