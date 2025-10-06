package org.kergru.library.books.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

  @Query("""
           SELECT
            b.id as id,
            b.isbn as isbn,
            b.title as title,
            b.author as author,
            b.publishedAt as publishedAt,
            b.publisher as publisher,
            b.language as language,
            b.description as description,
            b.pages as pages,
            l.id as loanId,
            l.borrowedAt as borrowedAt,
            l.userId as borrowerId
        FROM BookEntity b
        LEFT JOIN LoanEntity l ON b.id = l.book.id AND l.returnedAt IS NULL
        WHERE b.isbn = :isbn
      \s""")
  Optional<BookWithLoan> findByIsbnWithLoan(String isbn);

  @Query("""
      SELECT
          b.id as id,
          b.isbn as isbn,
          b.title as title,
          b.author as author,
          b.publishedAt as publishedAt,
          b.publisher as publisher,
          b.language as language,
          b.description as description,
          b.pages as pages,
          l.id as loanId,
          l.borrowedAt as borrowedAt,
          l.userId as borrowerId
      FROM BookEntity b
      LEFT JOIN LoanEntity l ON b.id = l.book.id AND l.returnedAt IS NULL
      """)
  List<BookWithLoan> findAllWithLoanStatus();
}

