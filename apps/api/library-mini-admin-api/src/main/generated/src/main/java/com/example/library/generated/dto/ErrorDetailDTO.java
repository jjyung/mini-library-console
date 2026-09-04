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
 * ErrorDetailDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-04T17:25:23.268957+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class ErrorDetailDTO {

  private String field;

  private String reason;

  public ErrorDetailDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ErrorDetailDTO(String field, String reason) {
    this.field = field;
    this.reason = reason;
  }

  public ErrorDetailDTO field(String field) {
    this.field = field;
    return this;
  }

  /**
   * Get field
   * @return field
   */
  @NotNull 
  @Schema(name = "field", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("field")
  public String getField() {
    return field;
  }

  @JsonProperty("field")
  public void setField(String field) {
    this.field = field;
  }

  public ErrorDetailDTO reason(String reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Get reason
   * @return reason
   */
  @NotNull 
  @Schema(name = "reason", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("reason")
  public String getReason() {
    return reason;
  }

  @JsonProperty("reason")
  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorDetailDTO errorDetailDTO = (ErrorDetailDTO) o;
    return Objects.equals(this.field, errorDetailDTO.field) &&
        Objects.equals(this.reason, errorDetailDTO.reason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, reason);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorDetailDTO {\n");
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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

