package org.kergru.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(MockOAuth2Config.class)
@SpringBootTest
class LibraryFrontendApplicationTests {

  @Test
  void contextLoads() {
  }
}
