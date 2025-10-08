package org.kergru.library.client;

import java.util.List;
import java.util.Optional;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class LibraryBackendClient {

  private final RestClient restClient;

  public LibraryBackendClient(RestClient oauth2RestClient) {
    this.restClient = oauth2RestClient;
  }

  public List<BookDto> getAllBooks() {
    return restClient.get()
        .uri("/library/api/books")
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

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

  public List<UserDto> getAllUsers() {
    return restClient.get()
        .uri("/library/api/users")
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

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

  public List<LoanDto> getBorrowedBooksOfUser(String userName) {
    return restClient.get()
        .uri("/library/api/users/{userName}/loans", userName)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}