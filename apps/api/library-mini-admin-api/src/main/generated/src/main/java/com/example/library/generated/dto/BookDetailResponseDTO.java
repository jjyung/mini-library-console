package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.example.library.generated.dto.LoanSummaryResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.lang.Nullable;
import java.util.NoSuchElementException;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BookDetailResponseDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class BookDetailResponseDTO {

  private UUID bookId;

  private String title;

  private String isbn;

  private JsonNullable<String> author = JsonNullable.<String>undefined();

  private String category;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    AVAILABLE("available"),
    
    BORROWED("borrowed"),
    
    INACTIVE("inactive");

    private final String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status;

  private Integer availableCount;

  private Integer totalCount;

  private JsonNullable<LoanSummaryResponseDTO> activeLoan = JsonNullable.<LoanSummaryResponseDTO>undefined();

  public BookDetailResponseDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BookDetailResponseDTO(UUID bookId, String title, String isbn, String category, StatusEnum status, Integer availableCount, Integer totalCount) {
    this.bookId = bookId;
    this.title = title;
    this.isbn = isbn;
    this.category = category;
    this.status = status;
    this.availableCount = availableCount;
    this.totalCount = totalCount;
  }

  public BookDetailResponseDTO bookId(UUID bookId) {
    this.bookId = bookId;
    return this;
  }

  /**
   * Get bookId
   * @return bookId
   */
  @NotNull @Valid 
  @Schema(name = "bookId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("bookId")
  public UUID getBookId() {
    return bookId;
  }

  @JsonProperty("bookId")
  public void setBookId(UUID bookId) {
    this.bookId = bookId;
  }

  public BookDetailResponseDTO title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
   */
  @NotNull 
  @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  public BookDetailResponseDTO isbn(String isbn) {
    this.isbn = isbn;
    return this;
  }

  /**
   * Get isbn
   * @return isbn
   */
  @NotNull 
  @Schema(name = "isbn", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isbn")
  public String getIsbn() {
    return isbn;
  }

  @JsonProperty("isbn")
  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public BookDetailResponseDTO author(String author) {
    this.author = JsonNullable.of(author);
    return this;
  }

  /**
   * Get author
   * @return author
   */
  
  @Schema(name = "author", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("author")
  public JsonNullable<String> getAuthor() {
    return author;
  }

  public void setAuthor(JsonNullable<String> author) {
    this.author = author;
  }

  public BookDetailResponseDTO category(String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
   */
  @NotNull 
  @Schema(name = "category", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }

  @JsonProperty("category")
  public void setCategory(String category) {
    this.category = category;
  }

  public BookDetailResponseDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public BookDetailResponseDTO availableCount(Integer availableCount) {
    this.availableCount = availableCount;
    return this;
  }

  /**
   * Get availableCount
   * minimum: 0
   * @return availableCount
   */
  @NotNull @Min(value = 0) 
  @Schema(name = "availableCount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("availableCount")
  public Integer getAvailableCount() {
    return availableCount;
  }

  @JsonProperty("availableCount")
  public void setAvailableCount(Integer availableCount) {
    this.availableCount = availableCount;
  }

  public BookDetailResponseDTO totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * Get totalCount
   * minimum: 1
   * @return totalCount
   */
  @NotNull @Min(value = 1) 
  @Schema(name = "totalCount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalCount")
  public Integer getTotalCount() {
    return totalCount;
  }

  @JsonProperty("totalCount")
  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public BookDetailResponseDTO activeLoan(LoanSummaryResponseDTO activeLoan) {
    this.activeLoan = JsonNullable.of(activeLoan);
    return this;
  }

  /**
   * Get activeLoan
   * @return activeLoan
   */
  @Valid 
  @Schema(name = "activeLoan", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("activeLoan")
  public JsonNullable<LoanSummaryResponseDTO> getActiveLoan() {
    return activeLoan;
  }

  public void setActiveLoan(JsonNullable<LoanSummaryResponseDTO> activeLoan) {
    this.activeLoan = activeLoan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BookDetailResponseDTO bookDetailResponseDTO = (BookDetailResponseDTO) o;
    return Objects.equals(this.bookId, bookDetailResponseDTO.bookId) &&
        Objects.equals(this.title, bookDetailResponseDTO.title) &&
        Objects.equals(this.isbn, bookDetailResponseDTO.isbn) &&
        equalsNullable(this.author, bookDetailResponseDTO.author) &&
        Objects.equals(this.category, bookDetailResponseDTO.category) &&
        Objects.equals(this.status, bookDetailResponseDTO.status) &&
        Objects.equals(this.availableCount, bookDetailResponseDTO.availableCount) &&
        Objects.equals(this.totalCount, bookDetailResponseDTO.totalCount) &&
        equalsNullable(this.activeLoan, bookDetailResponseDTO.activeLoan);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(bookId, title, isbn, hashCodeNullable(author), category, status, availableCount, totalCount, hashCodeNullable(activeLoan));
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BookDetailResponseDTO {\n");
    sb.append("    bookId: ").append(toIndentedString(bookId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    isbn: ").append(toIndentedString(isbn)).append("\n");
    sb.append("    author: ").append(toIndentedString(author)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    availableCount: ").append(toIndentedString(availableCount)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    activeLoan: ").append(toIndentedString(activeLoan)).append("\n");
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

