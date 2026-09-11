package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * PostBookReturnsRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostBookReturnsRequestDTO {

  private UUID loanId;

  private JsonNullable<@Size(max = 100) String> readerId = JsonNullable.<String>undefined();

  public PostBookReturnsRequestDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostBookReturnsRequestDTO(UUID loanId) {
    this.loanId = loanId;
  }

  public PostBookReturnsRequestDTO loanId(UUID loanId) {
    this.loanId = loanId;
    return this;
  }

  /**
   * 要結束的唯一借閱紀錄識別值。
   * @return loanId
   */
  @NotNull @Valid 
  @Schema(name = "loanId", description = "要結束的唯一借閱紀錄識別值。", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("loanId")
  public UUID getLoanId() {
    return loanId;
  }

  @JsonProperty("loanId")
  public void setLoanId(UUID loanId) {
    this.loanId = loanId;
  }

  public PostBookReturnsRequestDTO readerId(String readerId) {
    this.readerId = JsonNullable.of(readerId);
    return this;
  }

  /**
   * 可選的 UI 輔助識別值；業務定位以 loanId 為準。
   * @return readerId
   */
  @Size(max = 100) 
  @Schema(name = "readerId", description = "可選的 UI 輔助識別值；業務定位以 loanId 為準。", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("readerId")
  public JsonNullable<@Size(max = 100) String> getReaderId() {
    return readerId;
  }

  public void setReaderId(JsonNullable<String> readerId) {
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
    PostBookReturnsRequestDTO postBookReturnsRequestDTO = (PostBookReturnsRequestDTO) o;
    return Objects.equals(this.loanId, postBookReturnsRequestDTO.loanId) &&
        equalsNullable(this.readerId, postBookReturnsRequestDTO.readerId);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(loanId, hashCodeNullable(readerId));
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
    sb.append("class PostBookReturnsRequestDTO {\n");
    sb.append("    loanId: ").append(toIndentedString(loanId)).append("\n");
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

