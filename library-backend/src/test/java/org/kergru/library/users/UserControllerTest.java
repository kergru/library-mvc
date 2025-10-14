package org.kergru.library.users;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.kergru.library.JwtTestUtils.jwtWithRoles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.loans.service.LoanService;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

  private static final String NEW_USER_USERNAME = "johndoe";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LoanService loanService;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private ObjectMapper objectMapper;

  // Hack to create JwtDecoder bean by Spring Security
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> "http://localhost:8085/realms/library/protocol/openid-connect/certs");
  }

  @AfterEach
  public void afterEach() {
    loanRepository.deleteAll();
    //userRepository.deleteByUsername(NEW_USER_USERNAME);
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

  @Test
  @WithMockUser
  @Transactional
  public void expectCreateUserShouldReturnUser() throws Exception {
    try {
      var jwt = jwtWithRoles("librarian", "LIBRARIAN");
      var userDto = new UserDto(NEW_USER_USERNAME, "John", "Doe", "john.doe@example.com");

      mockMvc.perform(post("/library/api/users")
              .content(objectMapper.writeValueAsString(userDto))
              .contentType(MediaType.APPLICATION_JSON)
              .with(jwt)
          )
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.userName").value("johndoe"))
          .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    } finally {
      userRepository.deleteByUsername(NEW_USER_USERNAME);
    }
  }

  @Test
  @WithMockUser
  public void expectCreateUserWithAlreadyExistingUsernameShouldReturnConflict() throws Exception {
    var jwt = jwtWithRoles("librarian", "LIBRARIAN");
    // demo_user_1 already exists thru initial data
    var userDto = new UserDto("demo_user_1", "John", "Doe", "john.doe@example.com");

    mockMvc.perform(post("/library/api/users")
            .content(objectMapper.writeValueAsString(userDto))
            .contentType(MediaType.APPLICATION_JSON)
            .with(jwt)
        )
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("User already exists"))
        .andExpect(jsonPath("$.detail").value("username"));
  }

  @Test
  @WithMockUser
  @Transactional
  public void expectDeleteUserShouldReturn201() throws Exception {
    try {
      // create user first
      UserEntity userEntity = new UserEntity(null, NEW_USER_USERNAME, "John", "Doe", "john.doe@example.com");
      userRepository.save(userEntity);

      var jwt = jwtWithRoles("librarian", "LIBRARIAN");
      mockMvc.perform(delete("/library/api/users/" + NEW_USER_USERNAME)
              .with(jwt)
          )
          .andExpect(status().isNoContent());

      // check that user is deleted
      Optional<UserEntity> userEntityOptional = userRepository.findByUsername(NEW_USER_USERNAME);
      assertFalse(userEntityOptional.isPresent());
    } finally {
      userRepository.deleteByUsername(NEW_USER_USERNAME);
    }
  }
}

