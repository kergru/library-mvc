package org.kergru.library.web;

import java.util.Map;
import org.kergru.library.client.librarybackend.LibraryBackendClient.BookAlreadyBorrowedException;
import org.kergru.library.client.librarybackend.LibraryBackendClient.UserAlreadyExistsException;
import org.kergru.library.model.LoanDto;
import org.kergru.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/library/rest")
public class LibraryApiRestController {

  private final LibraryService libraryService;

  public LibraryApiRestController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @PostMapping("/me/borrowBook/{isbn}")
  public ResponseEntity<LoanDto> borrowBook(@PathVariable String isbn, @AuthenticationPrincipal OidcUser user) {

    try {
      return ResponseEntity.ok(libraryService.borrowBook(isbn, user.getPreferredUsername()));
    } catch (BookAlreadyBorrowedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PostMapping("/me/returnBook/{loanId}")
  public ResponseEntity<Void> returnBook(@PathVariable Long loanId, @AuthenticationPrincipal OidcUser user) {

    try {
      libraryService.returnBook(loanId, user.getPreferredUsername());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @PostMapping("/admin/users")
  public ResponseEntity<?> createUser(
      @RequestBody CreateUserRequest createUserRequest
  ) {
    try {
      return ResponseEntity.ok(
          libraryService.createUser(
              createUserRequest.username,
              createUserRequest.firstName,
              createUserRequest.lastName,
              createUserRequest.email,
              createUserRequest.password
          ));
    } catch (UserAlreadyExistsException e) {
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(Map.of("message", "User already exists", "hints", e.getHints()));
    }
  }

  public record CreateUserRequest(
      String username,
      String firstName,
      String lastName,
      String email,
      String password
  ) {}
}
