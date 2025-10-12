package org.kergru.library.users.rest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.kergru.library.loans.service.LoanService;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/library/api")
public class UserController {

  private final UserService userService;

  private final LoanService loanService;

  public UserController(UserService userService, LoanService loanService) {
    this.userService = userService;
    this.loanService = loanService;
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('LIBRARIAN')")
  public ResponseEntity<PageResponseDto<UserDto>> getAllUsers(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "firstname") String sortBy
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
    return ResponseEntity.ok(loanService.findBorrowedByUser(userName));
  }

  /**
   * Borrows a book to a user. Endpoint is only available for the user himself.
   * If the book is already borrowed, a 409 Conflict is returned.
   */
  @PreAuthorize("#userName == authentication.principal.claims['preferred_username']")
  @PostMapping("/users/{userName}/loans")
  public ResponseEntity<LoanDto> borrowBook(@PathVariable String userName, @RequestBody String isbn) {

    try {
      var loanDto = loanService.borrowBook(isbn, userName);
      return ResponseEntity.ok(loanDto);
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  /**
   * Returns a book to library by setting returnedAt date in loan. Endpoint is only available for the user himself.
   * If no loan with id found or loan not borrowed by user, a 404 NotFound is returned.
   */
  @PreAuthorize("#userName == authentication.principal.claims['preferred_username']")
  @DeleteMapping("/users/{userName}/loans/{loanId}")
  public ResponseEntity<Void> returnBook(@PathVariable String userName, @PathVariable long loanId) {

    try {
      loanService.returnBook(loanId, userName);
      return ResponseEntity.ok().build();
    } catch (NoSuchElementException | IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }
}
