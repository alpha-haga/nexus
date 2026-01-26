package nexus.infrastructure.db

import nexus.infrastructure.db.exception.CorporationContextNotSetException

/**
 * CorporationContext
 *
 * 現在の処理がどの Corporation（法人）に向くべきかを保持する ThreadLocal コンテキスト
 *
 * 責務:
 * - app 層（bff/api/batch）が set/clear する
 * - infrastructure 層（DataSource 層）が読む
 *
 * 実装方式:
 * - ThreadLocal を採用
 * - app 層は必ず finally で clear する（リーク防止）
 *
 * 未設定時の挙動:
 * - get() が未設定なら例外を投げる（fail fast、事故防止）
 */
object CorporationContext {
    private val context = ThreadLocal<String>()

    /**
     * 現在の Corporation を設定
     *
     * @param corporation 設定する Corporation（null 禁止、空文字禁止）
     */
    fun set(corporation: String) {
        require(corporation.isNotBlank()) { "Corporation must not be blank" }
        context.set(corporation)
    }

    /**
     * 現在の Corporation を取得
     *
     * @return 現在の Corporation
     * @throws CorporationContextNotSetException Corporation が未設定の場合
     */
    fun get(): String {
        return context.get() ?: throw CorporationContextNotSetException()
    }

    /**
     * 現在の Corporation を取得（未設定時は null）
     */
    fun getOrNull(): String? = context.get()

    /**
     * Corporation が設定されているか確認
     */
    fun isSet(): Boolean = context.get() != null

    /**
     * 現在の Corporation をクリア（リーク防止）
     */
    fun clear() {
        context.remove()
    }
}