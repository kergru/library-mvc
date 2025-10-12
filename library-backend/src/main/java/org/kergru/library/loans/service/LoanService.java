package org.kergru.library.loans.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.kergru.library.books.repository.BookEntity;
import org.kergru.library.books.repository.BookRepository;
import org.kergru.library.books.service.BookService;
import org.kergru.library.loans.repository.LoanEntity;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.model.LoanDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class LoanService {

  private final LoanRepository loanRepository;

  private final UserRepository userRepository;

  private final BookRepository bookRepository;

  public LoanService(
      LoanRepository loanRepository,
      UserRepository userRepository,
      BookRepository bookRepository
  ) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.bookRepository = bookRepository;
  }

  public List<LoanDto> findBorrowedByUser(String userName) {
    Optional<UserEntity> user = userRepository.findByUsername(userName);
    return user.map(u -> loanRepository.findByUserId(u.getId()).stream().map(this::toDto).toList()).orElse(List.of());
  }

  public LoanDto borrowBook(String isbn, String userName) {
    UserEntity user = userRepository.findByUsername(userName).orElseThrow();
    BookEntity book = bookRepository.findByIsbn(isbn).orElseThrow();

    if(isBorrowed(book)) {
      throw new IllegalStateException("Book is already borrowed");
    } else {
      LoanEntity loan = new LoanEntity();
      loan.setBook(book);
      loan.setUserId(user.getId());
      loan.setBorrowedAt(Instant.now());

      loanRepository.save(loan);
      return toDto(loan);
    }
  }

  public void returnBook(Long loanId, String userName) {
    LoanEntity loan = loanRepository.findById(loanId).orElseThrow();
    if(!loan.getUserId().equals(userRepository.findByUsername(userName).orElseThrow().getId())) {
      throw new IllegalStateException("User is not the owner of the loan");
    }
    loan.setReturnedAt(Instant.now());
    loanRepository.save(loan);
  }

  private boolean isBorrowed(BookEntity book) {
    System.out.println(loanRepository.findAll());
    return loanRepository.findByBookIdAndReturnedAtIsNull(book.getId()).isPresent();
  }

  private LoanDto toDto(LoanEntity e) {
    return new LoanDto(e.getId(), BookService.toDto(e.getBook()), e.getBorrowedAt(), e.getReturnedAt());
  }
}
