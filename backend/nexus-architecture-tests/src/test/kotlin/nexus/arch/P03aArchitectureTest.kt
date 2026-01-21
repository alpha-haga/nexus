package nexus.arch

import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaType
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

/**
 * P0-3a: 境界ルール固定（最小で効く）
 *
 * 前提:
 * - package は "nexus...."
 * - domain は "..domain.." を持たない（nexus.<domain>.. を domain集合として扱う）
 * - @Transactional/@Service は P0-3a では許可（段階導入）
 * - 禁止するのは JPA/JDBC/SQL系のみ
 */
class P03aArchitectureTest {

    private val classes = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("nexus")

    /**
     * domain集合（P0-3a定義）
     * - settings.gradle.kts の include と対応させて列挙する
     */
    private val domainPackages = arrayOf(
        "nexus.group..",
        "nexus.identity..",
        "nexus.household..",
        "nexus.gojo..",
        "nexus.funeral..",
        "nexus.bridal..",
        "nexus.point..",
        "nexus.agent..",
        "nexus.payment..",
        "nexus.accounting..",
        "nexus.reporting..",
    )

    private val appPackages = arrayOf(
        "nexus.bff..",
        "nexus.api..",
        "nexus.batch..",
    )

    private val infraPackages = arrayOf(
        "nexus.infrastructure..",
    )

    /**
     * L1-1: domain は JPA/JDBC/SQL系に依存してはならない
     * - org.springframework.transaction.. / stereotype.. は P0-3aでは許可
     */
    @Test
    fun `L1-1 domain must not depend on JPA or JDBC or SQL packages (except entity)`() {
        noClasses()
            .that().resideInAnyPackage(*domainPackages)
            .and().resideOutsideOfPackages(
                "nexus.*.entity..",
                "nexus.*.*.entity..",
                "nexus.*.*.*.entity..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.persistence..",
                "org.springframework.data.jpa..",
                "org.springframework.jdbc..",
                "javax.sql..",
            )
            .because("domain(非entity) を実装技術（JPA/JDBC/SQL）から独立させ、将来の差し替え自由度を確保するため")
            .check(classes)
    }

    /**
     * L1-2: domain -> infrastructure/app 依存禁止（domain集合で判定）
     */
    @Test
    fun `L1-2 domain must not depend on infrastructure or app`() {
        noClasses()
            .that().resideInAnyPackage(*domainPackages)
            .should().dependOnClassesThat().resideInAnyPackage(
                *infraPackages,
                *appPackages,
            )
            .because("依存方向を app->domain->core / infrastructure->domain+core に固定し、循環依存を設計で潰すため")
            .check(classes)
    }

    /**
     * L1-3: bff/api controller の public メソッドは nexus.*.entity.* を返却型に含めない（ジェネリクス含む）
     *
     * 取りこぼし防止のため、return type の erasure だけでなく、
     * ジェネリクスに関与する raw types も走査して判定する。
     */
    @Test
    fun `L1-3 controllers must not return domain entities`() {
        methods()
            .that().arePublic()
            .and().areDeclaredInClassesThat().resideInAnyPackage(
                "nexus.bff.controller..",
                "nexus.api.controller..",
            )
            .should(notReturnJpaEntityInSignature())
            .because("Controller は domain entity を境界外へ露出させない（壊れない土台のため）")
            .check(classes)
    }

    /**
     * L2-1（任意）: repository と reader/queryservice の相互参照禁止
     *
     * ※P0-3a では強くしすぎない。
     * 既存のパッケージ設計が揺れている場合に備え、
     * - repository パッケージがある場合に効く
     * - reader/query/readmodel 等がまだ無い場合は自然にスルーされる
     * という粒度で入れる。
     */
    @Test
    fun `L2-1 repository and reader must not depend on each other (if present)`() {
        // repository -> reader/query
        noClasses()
            .that().resideInAnyPackage("nexus.*.*.repository..", "nexus.*.repository..")
            .and().resideOutsideOfPackages("nexus.group.repository..") // ★暫定除外
            .should().dependOnClassesThat().resideInAnyPackage(
                "nexus.*.*.reader..",
                "nexus.*.reader..",
                "nexus.*.*.query..",
                "nexus.*.query..",
            )
            .because("Write抽象とRead抽象の責務混濁を防ぎ、巨大化と循環の温床を潰すため")
            .check(classes)

        // reader/query -> repository
        noClasses()
            .that().resideInAnyPackage(
                "nexus.*.*.reader..",
                "nexus.*.reader..",
                "nexus.*.*.query..",
                "nexus.*.query..",
            )
            .should().dependOnClassesThat().resideInAnyPackage("nexus.*.*.repository..", "nexus.*.repository..")
            .because("Read側がWrite抽象へ寄りかかると責務が崩れるため")
            .check(classes)
    }
    
    /**
     * L2-2（任意）: readmodel -> entity 参照禁止（入れられれば）
     */
    @Test
    fun `L2-2 readmodel must not depend on entity (if present)`() {
        noClasses()
            .that().resideInAnyPackage("nexus.*.*.readmodel..", "nexus.*.readmodel..")
            .should().dependOnClassesThat().resideInAnyPackage("nexus.*.*.entity..", "nexus.*.entity..")
            .because("ReadModel が Entity の薄いラッパになるのを防ぎ、境界を保つため")
            .allowEmptyShould(true)
            .check(classes)
    }

    private fun notReturnJpaEntityInSignature(): ArchCondition<JavaMethod> {
        return object : ArchCondition<JavaMethod>("not return JPA @Entity type (including generics)") {
            override fun check(method: JavaMethod, events: ConditionEvents) {
                val returnType: JavaType = method.returnType
    
                val involved = returnType.allInvolvedRawTypes
                val hasJpaEntity = involved.any { it.hasEntityAnnotation() }
    
                if (hasJpaEntity) {
                    val msg = "Controller method returns/contains JPA @Entity: ${method.fullName} -> ${returnType.name}"
                    events.add(SimpleConditionEvent.violated(method, msg))
                }
            }
        }
    }
    
    private fun com.tngtech.archunit.core.domain.JavaClass.hasEntityAnnotation(): Boolean {
        return this.annotations.any { it.rawType.fullName == "jakarta.persistence.Entity" }
    }
    private fun isEntityPackage(pkg: String): Boolean {
        // 取りこぼし防止のため、保守的に "nexus." + ".entity" を含むかで判定する
        // （例: nexus.gojo.contract.entity / nexus.identity.person.entity 等）
        return pkg.startsWith("nexus.") && pkg.contains(".entity")
    }
}
