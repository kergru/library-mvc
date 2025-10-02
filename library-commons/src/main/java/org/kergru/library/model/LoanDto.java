package org.kergru.library.model;

import java.time.Instant;

public record LoanDto(
    BookDto book,
    Instant borrowedAt,
    Instant returnedAt
) { }
