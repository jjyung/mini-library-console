package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.example.library.generated.dto.BookDetailResponseDTO;
import com.example.library.generated.dto.LoanSummaryResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PostBookBorrowsDataResponseDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostBookBorrowsDataResponseDTO {

  private BookDetailResponseDTO book;

  private LoanSummaryResponseDTO loan;

  public PostBookBorrowsDataResponseDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostBookBorrowsDataResponseDTO(BookDetailResponseDTO book, LoanSummaryResponseDTO loan) {
    this.book = book;
    this.loan = loan;
  }

  public PostBookBorrowsDataResponseDTO book(BookDetailResponseDTO book) {
    this.book = book;
    return this;
  }

  /**
   * Get book
   * @return book
   */
  @NotNull @Valid 
  @Schema(name = "book", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("book")
  public BookDetailResponseDTO getBook() {
    return book;
  }

  @JsonProperty("book")
  public void setBook(BookDetailResponseDTO book) {
    this.book = book;
  }

  public PostBookBorrowsDataResponseDTO loan(LoanSummaryResponseDTO loan) {
    this.loan = loan;
    return this;
  }

  /**
   * Get loan
   * @return loan
   */
  @NotNull @Valid 
  @Schema(name = "loan", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("loan")
  public LoanSummaryResponseDTO getLoan() {
    return loan;
  }

  @JsonProperty("loan")
  public void setLoan(LoanSummaryResponseDTO loan) {
    this.loan = loan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostBookBorrowsDataResponseDTO postBookBorrowsDataResponseDTO = (PostBookBorrowsDataResponseDTO) o;
    return Objects.equals(this.book, postBookBorrowsDataResponseDTO.book) &&
        Objects.equals(this.loan, postBookBorrowsDataResponseDTO.loan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(book, loan);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PostBookBorrowsDataResponseDTO {\n");
    sb.append("    book: ").append(toIndentedString(book)).append("\n");
    sb.append("    loan: ").append(toIndentedString(loan)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

