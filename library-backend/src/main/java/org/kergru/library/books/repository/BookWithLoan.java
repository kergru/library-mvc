package org.kergru.library.books.repository;

import java.time.Instant;

public interface BookWithLoan {

  String getIsbn();

  String getTitle();

  String getAuthor();

  Integer getPublishedAt();

  String getPublisher();

  String getLanguage();

  String getDescription();

  Integer getPages();

  Long getLoanId(); // null if not borrowed

  Instant getBorrowedAt(); // null if not borrowed

  Long getBorrowerId(); // null if not borrowed
}
