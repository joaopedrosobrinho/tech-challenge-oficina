package br.com.fiap.oficina.architecture;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
class ArquiteturaTest {
    @Test
    void dominioNaoDependeDeFrameworksOuCamadasExternas() {
        var classes = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("br.com.fiap.oficina");
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..infrastructure..", "..interfaces..", "jakarta.persistence..", "org.springframework..")
                .check(classes);
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..interfaces..")
                .check(classes);
    }
}
