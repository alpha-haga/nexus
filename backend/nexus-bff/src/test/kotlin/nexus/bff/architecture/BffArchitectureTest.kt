package nexus.bff.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class BffArchitectureTest {

    private fun importBffClasses(): JavaClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("nexus.bff")

    /**
     * Rule A:
     * BFF Controller は domain entity を直接参照してはならない。
     *
     * - entity 鈍化の強制
     * - ReadModel / DTO / QueryFacade 経由のみ許可
     */
    @Test
    fun `controllers should not depend on entity`() {
        val classes = importBffClasses()

        val rule = noClasses()
            .that()
            .resideInAnyPackage("nexus.bff.controller..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..entity..")

        rule.check(classes)
    }
}