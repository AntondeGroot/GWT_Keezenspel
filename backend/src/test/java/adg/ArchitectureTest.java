package adg;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * Static architecture rules over the production classes (tests excluded). These codify the
 * layering the code already follows so it cannot silently erode.
 */
@AnalyzeClasses(packages = "adg", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  /** Game logic and utilities must not reach up into the web layer. */
  @ArchTest
  static final ArchRule gameLogicIsIndependentOfTheWebLayer =
      noClasses()
          .that()
          .resideInAnyPackage("adg.keezen..", "adg.processing..", "adg.util..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("adg.services..");

  /** util is a leaf: it must not depend on any other adg feature package. */
  @ArchTest
  static final ArchRule utilIsALeaf =
      noClasses()
          .that()
          .resideInAPackage("adg.util..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("adg.keezen..", "adg.services..", "adg.processing..");

  /** Web components live in the services package, nowhere else. */
  @ArchTest
  static final ArchRule webComponentsLiveInServices =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .or()
          .areAnnotatedWith(Service.class)
          .should()
          .resideInAPackage("adg.services..");
}
