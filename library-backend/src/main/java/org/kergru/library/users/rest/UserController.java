package org.kergru.library.users.rest;

import java.util.List;
import java.util.Optional;
import org.kergru.library.loans.service.LoansService;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/library/api")
public class UserController {

  private final UserService userService;

  private final LoansService loansService;

  public UserController(UserService userService, LoansService loansService) {
    this.userService = userService;
    this.loansService = loansService;
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('LIBRARIAN')")
  public ResponseEntity<PageResponseDto<UserDto>> getAllUsers(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "firstName") String sortBy
  ) {
    return ResponseEntity.ok(userService.searchUsers(searchString, page, size, sortBy));
  }

  /**
   * Returns user profile by userName, only accessible by the librarian or the user himself
   */
  @GetMapping("/users/{userName}")
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['preferred_username']")
  public ResponseEntity<UserDto> getUser(@PathVariable String userName) {
    Optional<UserDto> user = userService.getUser(userName);
    return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * All borrowed books by a user, only accessible by the librarian or the user himself
   */
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['preferred_username']")
  @GetMapping("/users/{userName}/loans")
  public ResponseEntity<List<LoanDto>> getBorrowedBooksByUser(@PathVariable String userName) {
    return ResponseEntity.ok(loansService.findBorrowedByUser(userName));
  }
}