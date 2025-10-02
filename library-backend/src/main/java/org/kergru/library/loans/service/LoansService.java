package org.kergru.library.loans.service;

import java.util.List;
import java.util.Optional;
import org.kergru.library.books.repository.BookEntity;
import org.kergru.library.loans.repository.LoanEntity;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class LoansService {

  private final LoanRepository loanRepository;

  private final UserRepository userRepository;

  public LoansService(LoanRepository loanRepository, UserRepository userRepository) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
  }

  public List<LoanDto> findBorrowedByUser(String userName) {
    Optional<UserEntity> user = userRepository.findByUsername(userName);
    return user.map(u -> loanRepository.findByUserId(u.getId()).stream().map(this::toDto).toList()).orElse(List.of());
  }

  private LoanDto toDto(LoanEntity e) {
    return new LoanDto(toDto(e.getBook()), e.getBorrowedAt(), e.getReturnedAt());
  }

  private BookDto toDto(BookEntity b) {
    return new BookDto(
        b.getIsbn(),
        b.getTitle(),
        b.getAuthor(),
        b.getPublishedAt(),
        b.getPublisher(),
        b.getLanguage(),
        b.getPages(),
        b.getDescription(),
        null
    );
  }
}
