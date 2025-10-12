package org.kergru.library.loans.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.kergru.library.books.repository.BookEntity;

@Entity
@Table(name = "loans")
public class LoanEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private BookEntity book;

  @Column(nullable = false)
  private Instant borrowedAt;

  @Column
  private Instant returnedAt; // null => aktuell ausgeliehen

  public LoanEntity() {}

  public LoanEntity(Long id, Long userId, BookEntity book, Instant borrowedAt, Instant returnedAt) {
    this.id = id;
    this.userId = userId;
    this.book = book;
    this.borrowedAt = borrowedAt;
    this.returnedAt = returnedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public BookEntity getBook() {
    return book;
  }

  public Instant getBorrowedAt() {
    return borrowedAt;
  }

  public Instant getReturnedAt() {
    return returnedAt;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setBook(BookEntity book) {
    this.book = book;
  }

  public void setBorrowedAt(Instant borrowedAt) {
    this.borrowedAt = borrowedAt;
  }

  public void setReturnedAt(Instant returnedAt) {
    this.returnedAt = returnedAt;
  }
}

