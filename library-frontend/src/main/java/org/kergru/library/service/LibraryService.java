// java
package org.kergru.library.service;

import java.util.List;
import java.util.Optional;
import org.kergru.library.client.LibraryBackendClient;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.stereotype.Service;

@Service
public class LibraryService {

  private final LibraryBackendClient backendClient;

  public LibraryService(LibraryBackendClient oauth2BackendClient) {
    this.backendClient = oauth2BackendClient;
  }

  /**
   * Retrieves all books from the backend. Using the token relay pattern.
   */
  public List<BookDto> getAllBooks() {
    return backendClient.getAllBooks();
  }

  /**
   * Retrieves a single book by its ISBN from the backend. Using the token relay pattern.
   */
  public Optional<BookDto> getBookByIsbn(String isbn) {
    return backendClient.getBookByIsbn(isbn);
  }

  /**
   * Retrieves all users. Using the token relay pattern.
   */
  public List<UserDto> getAllUsers() {
    return backendClient.getAllUsers();
  }

  /**
   * Retrieves a single user by userName with his loans. Using the token relay pattern.
   */
  public Optional<UserWithLoans> getUserWithLoans(String userName) {
    return getUser(userName).map(user -> new UserWithLoans(user, getBorrowedBooksOfUser(userName)));
  }

  /**
   * Retrieves a single user by userName. Using the token relay pattern. Endpoint is only available for librarians or the user himself.
   */
  public Optional<UserDto> getUser(String userName) {
    return backendClient.getUser(userName);
  }

  /**
   * Retrieves borrowed books by user Using the token relay pattern. Endpoint is only available for librarians or the user himself.
   */
  public List<LoanDto> getBorrowedBooksOfUser(String userName) {
    return backendClient.getBorrowedBooksOfUser(userName);
  }
}