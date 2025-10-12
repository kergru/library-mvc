package org.kergru.library.client;

import java.util.List;
import java.util.Optional;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * OAuth2 client for the library backend.
 * Uses the token relay pattern which is set up in OAuth2RestClientConfig by adding OAuth2ClientHttpRequestInterceptor.
 *
 * @see OAuth2RestClientConfig
 */
@Service
public class LibraryBackendClient {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final RestClient restClient;

  public LibraryBackendClient(RestClient oauth2RestClient) {
    this.restClient = oauth2RestClient;
  }

  /**
   * Searches books from backend using pagination.
   */
  public PageResponseDto<BookDto> searchBooks(String searchString, int page, int size, String sortBy) {
    return restClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/api/books")
              .queryParam("page", page)
              .queryParam("size", size)
              .queryParam("sort", sortBy);
          if (searchString != null && !searchString.isEmpty()) {
            builder.queryParam("searchString", searchString);
          }
          return builder.build();
        })
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves book by isbn from backend.
   */
  public Optional<BookDto> getBookByIsbn(String isbn) {
    try {
      BookDto book = restClient.get()
          .uri("/library/api/books/{isbn}", isbn)
          .retrieve()
          .body(BookDto.class);
      return Optional.ofNullable(book);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  /**
   * Searches users from backend using pagination.
   */
  public PageResponseDto<UserDto> searchUsers(String searchString, int page, int size, String sortBy) {
    return restClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/api/users")
              .queryParam("page", page)
              .queryParam("size", size)
              .queryParam("sort", sortBy);
          if (searchString != null && !searchString.isEmpty()) {
            builder.queryParam("searchString", searchString);
          }
          return builder.build();
        })
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves user by username from backend.
   */
  public Optional<UserDto> getUser(String userName) {
    try {
      UserDto user = restClient.get()
          .uri("/library/api/users/{userName}", userName)
          .retrieve()
          .body(UserDto.class);
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves borrowed books of user from backend.
   */
  public List<LoanDto> getBorrowedBooksOfUser(String userName) {
    return restClient.get()
        .uri("/library/api/users/{userName}/loans", userName)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  /**
   * Borrows a book to a user. Endpoint is only available for the user himself.
   */
  public LoanDto borrowBook(String isbn, String userName) {
    try {
      return restClient.post()
          .uri("/library/api/users/{userName}/loans", userName)
          .body(isbn)
          .retrieve()
          .body(LoanDto.class);
    } catch (HttpClientErrorException.Conflict e) {
      logger.error("Failed to borrow book, book is already borrowed", e);
      throw new BookAlreadyBorrowedException(isbn);
    }
  }

  public void returnBook(Long loanId, String userName) {
    restClient.delete()
        .uri("/library/api/users/{userName}/loans/{loanId}", userName, loanId)
        .retrieve()
        .toBodilessEntity();
  }

  public static class BookAlreadyBorrowedException extends RuntimeException {
    public BookAlreadyBorrowedException(String isbn) {
      super("Book with isbn " + isbn + " is already borrowed");
    }
  }
}
