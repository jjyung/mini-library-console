package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.example.library.generated.dto.ErrorDetailDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ErrorResponseDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-04T17:25:23.268957+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class ErrorResponseDTO {

  /**
   * Gets or Sets code
   */
  public enum CodeEnum {
    A0000("A0000"),
    
    B0000("B0000"),
    
    C0000("C0000");

    private final String value;

    CodeEnum(String value) {
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
    public static CodeEnum fromValue(String value) {
      for (CodeEnum b : CodeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private CodeEnum code;

  private String message;

  private String traceId;

  private List<@Valid ErrorDetailDTO> details = new ArrayList<>();

  public ErrorResponseDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ErrorResponseDTO(CodeEnum code, String message, String traceId) {
    this.code = code;
    this.message = message;
    this.traceId = traceId;
  }

  public ErrorResponseDTO code(CodeEnum code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @NotNull 
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public CodeEnum getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(CodeEnum code) {
    this.code = code;
  }

  public ErrorResponseDTO message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   */
  @NotNull 
  @Schema(name = "message", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }

  public ErrorResponseDTO traceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  /**
   * Get traceId
   * @return traceId
   */
  @NotNull 
  @Schema(name = "traceId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("traceId")
  public String getTraceId() {
    return traceId;
  }

  @JsonProperty("traceId")
  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public ErrorResponseDTO details(List<@Valid ErrorDetailDTO> details) {
    this.details = details;
    return this;
  }

  public ErrorResponseDTO addDetailsItem(ErrorDetailDTO detailsItem) {
    if (this.details == null) {
      this.details = new ArrayList<>();
    }
    this.details.add(detailsItem);
    return this;
  }

  /**
   * Get details
   * @return details
   */
  @Valid 
  @Schema(name = "details", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("details")
  public List<@Valid ErrorDetailDTO> getDetails() {
    return details;
  }

  @JsonProperty("details")
  public void setDetails(List<@Valid ErrorDetailDTO> details) {
    this.details = details;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorResponseDTO errorResponseDTO = (ErrorResponseDTO) o;
    return Objects.equals(this.code, errorResponseDTO.code) &&
        Objects.equals(this.message, errorResponseDTO.message) &&
        Objects.equals(this.traceId, errorResponseDTO.traceId) &&
        Objects.equals(this.details, errorResponseDTO.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, traceId, details);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorResponseDTO {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

