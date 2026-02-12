package nexus.core.context

import nexus.core.exception.TenantContextNotSetException

/**
 * TenantContext
 *
 * 現在の処理がどの Tenant（法人）に向くべきかを保持する ThreadLocal コンテキスト
 *
 * 責務:
 * - app 層（bff/api/batch）が set/clear する
 * - domain/application 層が読む
 *
 * 実装方式:
 * - ThreadLocal を採用（RegionContext と同様）
 * - app 層は必ず finally で clear する（リーク防止）
 *
 * 未設定時の挙動:
 * - get() が未設定なら例外を投げる（fail fast、事故防止）
 *
 * 注意:
 * - RegionContext と共存可能（併存フェーズ）
 * - 将来的に RegionContext を置き換える予定
 */
object TenantContext {
    private val context = ThreadLocal<String>()

    /**
     * 現在の Tenant を設定
     *
     * @param tenantId 設定する Tenant ID（null 禁止、空文字禁止）
     */
    fun set(tenantId: String) {
        require(tenantId.isNotBlank()) { "TenantId must not be blank" }
        context.set(tenantId)
    }

    /**
     * 現在の Tenant を取得
     *
     * @return 現在の Tenant ID
     * @throws TenantContextNotSetException Tenant が未設定の場合
     */
    fun get(): String {
        return context.get() ?: throw TenantContextNotSetException()
    }

    /**
     * 現在の Tenant を取得（未設定時は null）
     */
    fun getOrNull(): String? = context.get()

    /**
     * Tenant が設定されているか確認
     */
    fun isSet(): Boolean = context.get() != null

    /**
     * 現在の Tenant をクリア（リーク防止）
     */
    fun clear() {
        context.remove()
    }
}
