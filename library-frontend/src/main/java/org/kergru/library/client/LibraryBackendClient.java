package org.kergru.library.client;

import java.util.List;
import java.util.Optional;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class LibraryBackendClient {

  private final RestTemplate restTemplate;

  public LibraryBackendClient(RestTemplate oauth2RestTemplate) {
    this.restTemplate = oauth2RestTemplate;
  }

  public List<BookDto> getAllBooks() {
    return restTemplate.exchange(
        "/library/api/books",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<BookDto>>() {}
    ).getBody();
  }

  public Optional<BookDto> getBookByIsbn(String isbn) {
    try {
      BookDto book = restTemplate.getForObject(
          "/library/api/books/{isbn}", BookDto.class, isbn
      );
      return Optional.ofNullable(book);
    } catch (HttpClientErrorException.NotFound e) {
      // 404 vom Backend → Optional.empty
      return Optional.empty();
    }
  }

  public List<UserDto> getAllUsers() {
    return restTemplate.exchange(
        "/library/api/users",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<UserDto>>() {}
    ).getBody();
  }

  public Optional<UserDto> getUser(String userName) {
    try {
      UserDto user = restTemplate.getForObject(
          "/library/api/users/{userName}", UserDto.class, userName
      );
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      // 404 vom Backend → Optional.empty
      return Optional.empty();
    }
  }

  public List<LoanDto> getBorrowedBooksOfUser(String userName) {
    return restTemplate.exchange(
        "/library/api/users/{userName}/loans",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<LoanDto>>() {},
        userName
    ).getBody();
  }
}