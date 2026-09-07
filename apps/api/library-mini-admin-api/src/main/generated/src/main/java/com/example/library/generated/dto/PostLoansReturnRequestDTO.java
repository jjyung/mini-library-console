package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
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
 * PostLoansReturnRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-07T10:06:39.879502+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostLoansReturnRequestDTO {

  private String isbn;

  private @Nullable String readerId;

  public PostLoansReturnRequestDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostLoansReturnRequestDTO(String isbn) {
    this.isbn = isbn;
  }

  public PostLoansReturnRequestDTO isbn(String isbn) {
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

  public PostLoansReturnRequestDTO readerId(@Nullable String readerId) {
    this.readerId = readerId;
    return this;
  }

  /**
   * 可選借閱人識別值；多筆 active loan 時用於唯一選擇
   * @return readerId
   */
  @Size(min = 1, max = 100) 
  @Schema(name = "readerId", description = "可選借閱人識別值；多筆 active loan 時用於唯一選擇", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("readerId")
  public @Nullable String getReaderId() {
    return readerId;
  }

  @JsonProperty("readerId")
  public void setReaderId(@Nullable String readerId) {
    this.readerId = readerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostLoansReturnRequestDTO postLoansReturnRequestDTO = (PostLoansReturnRequestDTO) o;
    return Objects.equals(this.isbn, postLoansReturnRequestDTO.isbn) &&
        Objects.equals(this.readerId, postLoansReturnRequestDTO.readerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isbn, readerId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PostLoansReturnRequestDTO {\n");
    sb.append("    isbn: ").append(toIndentedString(isbn)).append("\n");
    sb.append("    readerId: ").append(toIndentedString(readerId)).append("\n");
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

