// java
package org.kergru.library.service;

import java.util.List;
import java.util.Optional;
import org.kergru.library.client.LibraryBackendClient;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.stereotype.Service;

@Service
public class LibraryService {

  private final LibraryBackendClient backendClient;

  public LibraryService(LibraryBackendClient oauth2BackendClient) {
    this.backendClient = oauth2BackendClient;
  }

  /**
   * Retrieves books from the backend with pagination.
   */
  public PageResponseDto<BookDto> searchBooks(String searchString, int page, int size, String sortBy) {
    return backendClient.searchBooks(searchString,page, size, sortBy);
  }

  /**
   * Retrieves a single book by its ISBN from the backend.
   */
  public Optional<BookDto> getBookByIsbn(String isbn) {
    return backendClient.getBookByIsbn(isbn);
  }

  /**
   * Retrieves all users from the backend with pagination.
   */
  public PageResponseDto<UserDto> searchUsers(String searchString, int page, int size, String sortBy) {
    return backendClient.searchUsers(searchString,page, size, sortBy);
  }

  /**
   * Retrieves a single user by userName with his loans.
   */
  public Optional<UserWithLoans> getUserWithLoans(String userName) {
    return getUser(userName).map(user -> new UserWithLoans(user, getBorrowedBooksOfUser(userName)));
  }

  /**
   * Retrieves a single user by userName. Endpoint is only available for librarians or the user himself.
   */
  public Optional<UserDto> getUser(String userName) {
    return backendClient.getUser(userName);
  }

  /**
   * Retrieves borrowed books by user. Endpoint is only available for librarians or the user himself.
   */
  public List<LoanDto> getBorrowedBooksOfUser(String userName) {
    return backendClient.getBorrowedBooksOfUser(userName);
  }

  /**
   * Borrows a book to a user. Endpoint is only available for the user himself.
   */
  public LoanDto borrowBook(String isbn, String userName) {
    return backendClient.borrowBook(isbn, userName);
  }

  /**
   * Returns a book to library. Endpoint is only available for the user himself.
   */
  public void returnBook(Long loanId, String userName) {
    backendClient.returnBook(loanId, userName);
  }
}
