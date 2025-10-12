package org.kergru.library.users;

import static org.hamcrest.Matchers.containsString;
import static org.kergru.library.JwtTestUtils.jwtWithRoles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.loans.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

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

  @AfterEach
  public void afterEach() {
    loanRepository.deleteAll();
  }

  @Test
  @WithMockUser
  public void expectGetUserWithRoleLibrarianShouldReturnUser() throws Exception {
    var jwt = jwtWithRoles("librarian", "LIBRARIAN");

    mockMvc.perform(get("/library/api/users/demo_user_1")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")));
  }

  @Test
  @WithMockUser
  public void expectGetUnknownUserWithRoleLibrarianShouldReturnNotFound() throws Exception {
    var jwt = jwtWithRoles("librarian", "LIBRARIAN");

    mockMvc.perform(get("/library/api/users/UNKNOWN")
            .with(jwt))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  public void expectGetMyUserWithNotRoleLibrarianShouldReturnUser() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(get("/library/api/users/demo_user_1")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")));
  }

  @Test
  @WithMockUser
  public void expectGetForeignUserWithNotRoleLibrarianShouldReturnForbidden() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(get("/library/api/users/demo_user_2")
            .with(jwt))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  public void expectSearchUsersWithRoleLibrarianShouldReturnUsers() throws Exception {
    var jwt = jwtWithRoles("librarian", "LIBRARIAN");

    mockMvc.perform(get("/library/api/users")
            .queryParam("searchString", "demo_user_1")
            .with(jwt))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("demo_user_1")));
  }
  @Test
  @WithMockUser
  public void expectSearchUsersWithNotRoleLibrarianShouldReturnForbidden() throws Exception {
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(get("/library/api/users")
            .queryParam("searchString", "demo_user_1")
            .with(jwt))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  public void expectBorrowBookShouldReturnLoan() throws Exception {
    var isbn = "9781617294945";
    var jwt = jwtWithRoles("demo_user_1");

    mockMvc.perform(post("/library/api/users/demo_user_1/loans")
            .content(isbn)
            .contentType(MediaType.APPLICATION_JSON)
            .with(jwt)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.book.isbn").value(isbn));
  }

  @Test
  @WithMockUser
  public void expectBorrowAlreadyBorrowedBookShouldReturnConflict() throws Exception {
    //borrow book by another user
    var isbn = "9781617294945";
    loanService.borrowBook(isbn, "demo_user_2");

    //try to borrow book again
    var jwt = jwtWithRoles("demo_user_1");
    mockMvc.perform(post("/library/api/users/demo_user_1/loans")
            .content(isbn)
            .contentType(MediaType.APPLICATION_JSON)
            .with(jwt)
        )
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser
  public void expectReturnBookShouldReturnOk() throws Exception {
    var isbn = "9781617294945";
    var loan = loanService.borrowBook(isbn, "demo_user_1");

    var jwt = jwtWithRoles("demo_user_1");
    mockMvc.perform(delete("/library/api/users/demo_user_1/loans/" + loan.id())
            .with(jwt)
        )
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void expectReturnBookNotOwnerShouldReturnNotFound() throws Exception {
    var isbn = "9781617294945";
    //borrow book by another user
    var loan = loanService.borrowBook(isbn, "demo_user_2");

    var jwt = jwtWithRoles("demo_user_1");
    mockMvc.perform(delete("/library/api/users/demo_user_1/loans/" + loan.id())
            .with(jwt)
        )
        .andExpect(status().isNotFound());
  }
}

