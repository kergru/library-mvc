package org.kergru.library.books.rest;

import org.kergru.library.books.service.BookService;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.PageResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/library/api")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  /**
   * Returns all books
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/books")
  public ResponseEntity<PageResponseDto<BookDto>> searchBooks(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "title") String sortBy
  ) {
    return ResponseEntity.ok(bookService.searchBooks(searchString, page, size, sortBy));
  }

  /**
   * Returns a single book by ISBN
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/books/{isbn}")
  public ResponseEntity<BookDto> getBook(@PathVariable String isbn) {
    return ResponseEntity.of(bookService.getBook(isbn));
  }
}
