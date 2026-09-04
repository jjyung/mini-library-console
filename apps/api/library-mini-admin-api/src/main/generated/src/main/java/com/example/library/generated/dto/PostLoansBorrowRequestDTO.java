package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PostLoansBorrowRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-04T17:25:23.268957+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostLoansBorrowRequestDTO {

  private String readerId;

  private String isbn;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private @Nullable LocalDate dueDate;

  public PostLoansBorrowRequestDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostLoansBorrowRequestDTO(String readerId, String isbn) {
    this.readerId = readerId;
    this.isbn = isbn;
  }

  public PostLoansBorrowRequestDTO readerId(String readerId) {
    this.readerId = readerId;
    return this;
  }

  /**
   * 測試用借閱人識別值；使用 synthetic value
   * @return readerId
   */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "readerId", description = "測試用借閱人識別值；使用 synthetic value", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("readerId")
  public String getReaderId() {
    return readerId;
  }

  @JsonProperty("readerId")
  public void setReaderId(String readerId) {
    this.readerId = readerId;
  }

  public PostLoansBorrowRequestDTO isbn(String isbn) {
    this.isbn = isbn;
    return this;
  }

  /**
   * 書籍 ISBN
   * @return isbn
   */
  @NotNull @Size(min = 1, max = 20) 
  @Schema(name = "isbn", description = "書籍 ISBN", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isbn")
  public String getIsbn() {
    return isbn;
  }

  @JsonProperty("isbn")
  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public PostLoansBorrowRequestDTO dueDate(@Nullable LocalDate dueDate) {
    this.dueDate = dueDate;
    return this;
  }

  /**
   * 可選到期日；本 MVP 不計算罰款
   * @return dueDate
   */
  @Valid 
  @Schema(name = "dueDate", description = "可選到期日；本 MVP 不計算罰款", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dueDate")
  public @Nullable LocalDate getDueDate() {
    return dueDate;
  }

  @JsonProperty("dueDate")
  public void setDueDate(@Nullable LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostLoansBorrowRequestDTO postLoansBorrowRequestDTO = (PostLoansBorrowRequestDTO) o;
    return Objects.equals(this.readerId, postLoansBorrowRequestDTO.readerId) &&
        Objects.equals(this.isbn, postLoansBorrowRequestDTO.isbn) &&
        Objects.equals(this.dueDate, postLoansBorrowRequestDTO.dueDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(readerId, isbn, dueDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PostLoansBorrowRequestDTO {\n");
    sb.append("    readerId: ").append(toIndentedString(readerId)).append("\n");
    sb.append("    isbn: ").append(toIndentedString(isbn)).append("\n");
    sb.append("    dueDate: ").append(toIndentedString(dueDate)).append("\n");
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

