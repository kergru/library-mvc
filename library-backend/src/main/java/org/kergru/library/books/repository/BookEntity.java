package org.kergru.library.books.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class BookEntity {

  @Id
  private Long id;

  @Column(nullable = false)
  private String isbn;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false)
  private Integer publishedAt;

  @Column(nullable = false)
  private String publisher;

  @Column(nullable = false)
  private String language;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Integer pages;

  protected BookEntity() { /* JPA */ }

  protected BookEntity(Long id, String isbn, String title, String author, Integer year, String publisher, String language, Integer pages) {
    this.id = id;
    this.isbn = isbn;
    this.title = title;
    this.author = author;
    this.publishedAt = year;
    this.publisher = publisher;
    this.language = language;
    this.description = description;
    this.pages = pages;
  }

  public Long getId() {
    return id;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public Integer getPublishedAt() {
    return publishedAt;
  }

  public String getPublisher() {
    return publisher;
  }

  public String getLanguage() {
    return language;
  }

  public String getDescription() {
    return description;
  }

  public Integer getPages() {
    return pages;
  }
}