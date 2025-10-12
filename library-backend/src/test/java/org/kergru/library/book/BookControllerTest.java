package org.kergru.library.book;

import static org.hamcrest.Matchers.containsString;
import static org.kergru.library.JwtTestUtils.jwtWithRoles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.loans.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

  @AutoConfigureMockMvc
  @Testcontainers
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LoanService loanService;

  @Autowired
  private LoanRepository loanRepository;

  // Hack to create JwtDecoder bean by Spring Security
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> "http://localhost:8085/realms/library/protocol/openid-connect/certs");
  }

  @BeforeEach
  public void afterEach() {
    loanRepository.deleteAll(); //filled because of docker init
  }

  @Test
  @WithMockUser
  public void expectGetBookShouldReturnBook() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(get("/library/api/books/9780132350884")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Clean Code")))
        .andExpect(content().string(containsString("9780132350884")))
        .andExpect(jsonPath("$.loanStatus.available").value(true));
  }


  @Test
  @WithMockUser
  public void expectGetBorrowedBookShouldReturnBooksWithStatusBorrowed() throws Exception {
    //borrow book
    loanService.borrowBook("9780132350884", "demo_user_1");

    var jwt = jwtWithRoles("demo_user_1");
    mockMvc.perform(get("/library/api/books/9780132350884")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Clean Code")))
        .andExpect(content().string(containsString("9780132350884")))
        .andExpect(jsonPath("$.loanStatus.available").value(false));
  }

  @Test
  @WithMockUser
  public void expectSearchBooksShouldReturnBooks() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");
    mockMvc.perform(get("/library/api/books")
            .queryParam("searchString", "Clean Code")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Clean Code")))
        .andExpect(content().string(containsString("9780132350884")));
  }
}
