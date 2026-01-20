package nexus.api.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ApiArchitectureTest {

    private fun importApiClasses(): JavaClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("nexus.api")

    /**
     * Rule B:
     * nexus-api は業務ドメインを参照してはならない。
     * 業務ドメインは必ず nexus-bff 経由。
     */
    @Test
    fun `nexus-api should not depend on business domains`() {
        val classes = importApiClasses()

        val businessDomains = arrayOf(
            "nexus.gojo..",
            "nexus.funeral..",
            "nexus.bridal..",
            "nexus.point..",
            "nexus.agent..",
            "nexus.payment..",
            "nexus.accounting..",
            "nexus.reporting.."
        )

        val rule = noClasses()
            .that()
            .resideInAnyPackage("nexus.api..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(*businessDomains)

        rule.check(classes)
    }
}