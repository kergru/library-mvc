package org.kergru.library.books.service;

import java.util.Optional;
import java.util.stream.Collectors;
import org.kergru.library.books.repository.BookRepository;
import org.kergru.library.books.repository.BookWithLoanProjection;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanStatusDto;
import org.kergru.library.model.PageResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public PageResponseDto<BookDto> searchBooks(String searchString, int page, int size, String sortBy) {

    var bookPage = bookRepository.searchBooksPaged(
        (StringUtils.hasText(searchString) ? searchString : null),
        PageRequest.of(page, size, Sort.by(sortBy)));
    return new PageResponseDto<>(
        bookPage.getContent().stream().map(BookService::toDto).collect(Collectors.toList()),
        bookPage.getNumber(),
        bookPage.getSize(),
        bookPage.getTotalPages(),
        bookPage.getTotalElements(),
        bookPage.isFirst(),
        bookPage.isLast(),
        bookPage.getNumberOfElements(),
        bookPage.isEmpty()
    );
  }

  public Optional<BookDto> getBook(String isbn) {
    return bookRepository.findByIsbnWithLoan(isbn).map(BookService::toDto);
  }

  public static BookDto toDto(BookWithLoanProjection b) {
    return new BookDto(
        b.getIsbn(),
        b.getTitle(),
        b.getAuthor(),
        b.getPublishedAt(),
        b.getPublisher(),
        b.getLanguage(),
        b.getPages(),
        b.getDescription(),
        new LoanStatusDto(
            b.getLoanId() != null,
            b.getLoanId() != null ? b.getBorrowerId() : null,
            b.getLoanId() != null ? b.getBorrowedAt() : null
        )
    );
  }
}

