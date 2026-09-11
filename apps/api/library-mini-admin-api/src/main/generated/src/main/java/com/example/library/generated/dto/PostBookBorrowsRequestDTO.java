package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;
import java.util.Arrays;
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
 * PostBookBorrowsRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostBookBorrowsRequestDTO {

  private String readerId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private JsonNullable<LocalDate> dueDate = JsonNullable.<LocalDate>undefined();

  public PostBookBorrowsRequestDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostBookBorrowsRequestDTO(String readerId) {
    this.readerId = readerId;
  }

  public PostBookBorrowsRequestDTO readerId(String readerId) {
    this.readerId = readerId;
    return this;
  }

  /**
   * 借閱人識別值；MVP 以文字承載 reader name 或 reader ID。
   * @return readerId
   */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "readerId", description = "借閱人識別值；MVP 以文字承載 reader name 或 reader ID。", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("readerId")
  public String getReaderId() {
    return readerId;
  }

  @JsonProperty("readerId")
  public void setReaderId(String readerId) {
    this.readerId = readerId;
  }

  public PostBookBorrowsRequestDTO dueDate(LocalDate dueDate) {
    this.dueDate = JsonNullable.of(dueDate);
    return this;
  }

  /**
   * 可選到期日；MVP 不計算逾期罰款。
   * @return dueDate
   */
  @Valid 
  @Schema(name = "dueDate", description = "可選到期日；MVP 不計算逾期罰款。", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("dueDate")
  public JsonNullable<LocalDate> getDueDate() {
    return dueDate;
  }

  public void setDueDate(JsonNullable<LocalDate> dueDate) {
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
    PostBookBorrowsRequestDTO postBookBorrowsRequestDTO = (PostBookBorrowsRequestDTO) o;
    return Objects.equals(this.readerId, postBookBorrowsRequestDTO.readerId) &&
        equalsNullable(this.dueDate, postBookBorrowsRequestDTO.dueDate);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(readerId, hashCodeNullable(dueDate));
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
    sb.append("class PostBookBorrowsRequestDTO {\n");
    sb.append("    readerId: ").append(toIndentedString(readerId)).append("\n");
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

