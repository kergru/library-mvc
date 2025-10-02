package org.kergru.library.books.rest;

import org.kergru.library.model.BookDto;
import org.kergru.library.books.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


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
  public ResponseEntity<List<BookDto>> getAllBooks() {
    return ResponseEntity.ok(bookService.findAll());
  }

  /**
   * Returns a single book by ISBN
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/books/{isbn}")
  public ResponseEntity<BookDto> getBook(@PathVariable String isbn) {
    return ResponseEntity.of(bookService.findByIsbn(isbn));
  }
}