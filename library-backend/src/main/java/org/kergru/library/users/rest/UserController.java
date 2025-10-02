package org.kergru.library.users.rest;

import java.util.List;
import org.kergru.library.loans.service.LoansService;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.findAll());
  }

  /**
   * Returns user profile by userName,
   * only accessible by the librarian or the user himself
   */
  @GetMapping("/users/{userName}")
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['sub']")
  public ResponseEntity<UserDto> getUserByUserName(@PathVariable String userName) {
    Optional<UserDto> user = userService.findUserByUserName(userName);
    return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * All borrowed books by a user,
   * only accessible by the librarian or the user himself
   */
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['sub']")
  @GetMapping("/users/{userName}/loans")
  public ResponseEntity<List<LoanDto>> getBorrowedBooksByUser(@PathVariable String userName) {
    return ResponseEntity.ok(loansService.findBorrowedByUser(userName));
  }
}