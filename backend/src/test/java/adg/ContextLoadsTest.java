package adg;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import adg.services.CardsApiDelegateImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Boots the full Spring context (no web server) via the real Application bootstrap and
 * asserts a bean wires. Unit tests construct beans by hand and ArchUnit is static, so
 * neither exercises the container — without this, broken wiring (a missing bean, an
 * ambiguous or mis-annotated constructor) would only fail when the app actually starts,
 * not in the build. web(NONE) keeps it fast (no port bound).
 */
class ContextLoadsTest {

  @Test
  void contextLoadsAndCoreBeansWire() {
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run()) {
      assertNotNull(context.getBean(CardsApiDelegateImpl.class));
    }
  }
}
