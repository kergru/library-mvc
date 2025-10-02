package org.kergru.library.web.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BackendMockConfig {

  private static WireMockServer backendMock;

  public static void start() throws IOException {
    backendMock = new WireMockServer(WireMockConfiguration.options().port(8081));
    backendMock.start();

    // Beispiel-Stubs
    stubBooks();
    stubBook();
    stubUser();
    stubLoans();
  }

  private static void stubBooks() throws IOException {
    backendMock.stubFor(get(urlEqualTo("/library/api/books"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(readFile("stubs/books-list.json"))));
  }

  private static void stubBook() throws IOException {
    backendMock.stubFor(get(urlEqualTo("/library/api/books/12345"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(readFile("stubs/book-details.json"))));
  }

  private static void stubUser() throws IOException {
    backendMock.stubFor(get(urlEqualTo("/library/api/users/demo_user_1"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(readFile("stubs/user-details.json"))));
  }

  private static void stubLoans() throws IOException {
    backendMock.stubFor(get(urlEqualTo("/library/api/users/demo_user_1/loans"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(readFile("stubs/loans-list.json"))));
  }

  private static String readFile(String path) throws IOException {
    return Files.readString(Paths.get("src/test/resources/" + path));
  }

  public static void stop() {
    if (backendMock != null) backendMock.stop();
  }
}
