package org.kergru.library.books.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

  Optional<BookEntity> findByIsbn(String isbn);

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
        l.borrowedAt as borrowed_at,
        l.userId as borrower_id
    FROM BookEntity b
    LEFT JOIN LoanEntity l ON b.id = l.book.id AND l.returnedAt IS NULL
    WHERE b.isbn = :isbn
  """)
  Optional<BookWithLoanProjection> findByIsbnWithLoan(@Param("isbn") String isbn);

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
    WHERE 
        (:#{#searchString} IS NULL OR 
         b.title LIKE %:#{#searchString}% OR 
         b.author LIKE %:#{#searchString}% OR 
         b.isbn LIKE %:#{#searchString}%)
  """)
  Page<BookWithLoanProjection> searchBooksPaged(
      @Param("searchString") String searchString,
      Pageable pageable
  );
}
