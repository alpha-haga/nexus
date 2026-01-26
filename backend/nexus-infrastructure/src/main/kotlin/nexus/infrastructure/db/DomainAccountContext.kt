package nexus.infrastructure.db

import nexus.infrastructure.db.exception.DomainAccountContextNotSetException

/**
 * DomainAccountContext
 *
 * 現在の処理がどの DomainAccount（業務ドメイン別 DB 接続アカウント）に向くべきかを保持する ThreadLocal コンテキスト
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
object DomainAccountContext {
    private val context = ThreadLocal<DomainAccount>()

    /**
     * 現在の DomainAccount を設定
     *
     * @param domainAccount 設定する DomainAccount（null 禁止）
     */
    fun set(domainAccount: DomainAccount) {
        context.set(domainAccount)
    }

    /**
     * 現在の DomainAccount を取得
     *
     * @return 現在の DomainAccount
     * @throws DomainAccountContextNotSetException DomainAccount が未設定の場合
     */
    fun get(): DomainAccount {
        return context.get() ?: throw DomainAccountContextNotSetException()
    }

    /**
     * 現在の DomainAccount を取得（未設定時は null）
     */
    fun getOrNull(): DomainAccount? = context.get()

    /**
     * DomainAccount が設定されているか確認
     */
    fun isSet(): Boolean = context.get() != null

    /**
     * 現在の DomainAccount をクリア（リーク防止）
     */
    fun clear() {
        context.remove()
    }
}