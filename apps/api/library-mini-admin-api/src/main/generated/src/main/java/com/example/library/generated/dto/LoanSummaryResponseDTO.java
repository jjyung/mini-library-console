package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.format.annotation.DateTimeFormat;
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
 * LoanSummaryResponseDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class LoanSummaryResponseDTO {

  private UUID loanId;

  private UUID bookId;

  private String readerId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime borrowedAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private JsonNullable<LocalDate> dueDate = JsonNullable.<LocalDate>undefined();

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private JsonNullable<OffsetDateTime> returnedAt = JsonNullable.<OffsetDateTime>undefined();

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    ACTIVE("ACTIVE"),
    
    RETURNED("RETURNED");

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

  public LoanSummaryResponseDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public LoanSummaryResponseDTO(UUID loanId, UUID bookId, String readerId, OffsetDateTime borrowedAt, StatusEnum status) {
    this.loanId = loanId;
    this.bookId = bookId;
    this.readerId = readerId;
    this.borrowedAt = borrowedAt;
    this.status = status;
  }

  public LoanSummaryResponseDTO loanId(UUID loanId) {
    this.loanId = loanId;
    return this;
  }

  /**
   * Get loanId
   * @return loanId
   */
  @NotNull @Valid 
  @Schema(name = "loanId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("loanId")
  public UUID getLoanId() {
    return loanId;
  }

  @JsonProperty("loanId")
  public void setLoanId(UUID loanId) {
    this.loanId = loanId;
  }

  public LoanSummaryResponseDTO bookId(UUID bookId) {
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

  public LoanSummaryResponseDTO readerId(String readerId) {
    this.readerId = readerId;
    return this;
  }

  /**
   * Get readerId
   * @return readerId
   */
  @NotNull 
  @Schema(name = "readerId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("readerId")
  public String getReaderId() {
    return readerId;
  }

  @JsonProperty("readerId")
  public void setReaderId(String readerId) {
    this.readerId = readerId;
  }

  public LoanSummaryResponseDTO borrowedAt(OffsetDateTime borrowedAt) {
    this.borrowedAt = borrowedAt;
    return this;
  }

  /**
   * Get borrowedAt
   * @return borrowedAt
   */
  @NotNull @Valid 
  @Schema(name = "borrowedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("borrowedAt")
  public OffsetDateTime getBorrowedAt() {
    return borrowedAt;
  }

  @JsonProperty("borrowedAt")
  public void setBorrowedAt(OffsetDateTime borrowedAt) {
    this.borrowedAt = borrowedAt;
  }

  public LoanSummaryResponseDTO dueDate(LocalDate dueDate) {
    this.dueDate = JsonNullable.of(dueDate);
    return this;
  }

  /**
   * Get dueDate
   * @return dueDate
   */
  @Valid 
  @Schema(name = "dueDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("dueDate")
  public JsonNullable<LocalDate> getDueDate() {
    return dueDate;
  }

  public void setDueDate(JsonNullable<LocalDate> dueDate) {
    this.dueDate = dueDate;
  }

  public LoanSummaryResponseDTO returnedAt(OffsetDateTime returnedAt) {
    this.returnedAt = JsonNullable.of(returnedAt);
    return this;
  }

  /**
   * Get returnedAt
   * @return returnedAt
   */
  @Valid 
  @Schema(name = "returnedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("returnedAt")
  public JsonNullable<OffsetDateTime> getReturnedAt() {
    return returnedAt;
  }

  public void setReturnedAt(JsonNullable<OffsetDateTime> returnedAt) {
    this.returnedAt = returnedAt;
  }

  public LoanSummaryResponseDTO status(StatusEnum status) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoanSummaryResponseDTO loanSummaryResponseDTO = (LoanSummaryResponseDTO) o;
    return Objects.equals(this.loanId, loanSummaryResponseDTO.loanId) &&
        Objects.equals(this.bookId, loanSummaryResponseDTO.bookId) &&
        Objects.equals(this.readerId, loanSummaryResponseDTO.readerId) &&
        Objects.equals(this.borrowedAt, loanSummaryResponseDTO.borrowedAt) &&
        equalsNullable(this.dueDate, loanSummaryResponseDTO.dueDate) &&
        equalsNullable(this.returnedAt, loanSummaryResponseDTO.returnedAt) &&
        Objects.equals(this.status, loanSummaryResponseDTO.status);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(loanId, bookId, readerId, borrowedAt, hashCodeNullable(dueDate), hashCodeNullable(returnedAt), status);
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
    sb.append("class LoanSummaryResponseDTO {\n");
    sb.append("    loanId: ").append(toIndentedString(loanId)).append("\n");
    sb.append("    bookId: ").append(toIndentedString(bookId)).append("\n");
    sb.append("    readerId: ").append(toIndentedString(readerId)).append("\n");
    sb.append("    borrowedAt: ").append(toIndentedString(borrowedAt)).append("\n");
    sb.append("    dueDate: ").append(toIndentedString(dueDate)).append("\n");
    sb.append("    returnedAt: ").append(toIndentedString(returnedAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

