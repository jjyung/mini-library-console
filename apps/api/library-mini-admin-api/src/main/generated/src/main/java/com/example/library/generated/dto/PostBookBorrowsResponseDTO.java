package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.example.library.generated.dto.PostBookBorrowsDataResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PostBookBorrowsResponseDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostBookBorrowsResponseDTO {

  /**
   * Gets or Sets code
   */
  public enum CodeEnum {
    _00000("00000");

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

  private PostBookBorrowsDataResponseDTO data;

  public PostBookBorrowsResponseDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostBookBorrowsResponseDTO(CodeEnum code, String message, String traceId, PostBookBorrowsDataResponseDTO data) {
    this.code = code;
    this.message = message;
    this.traceId = traceId;
    this.data = data;
  }

  public PostBookBorrowsResponseDTO code(CodeEnum code) {
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

  public PostBookBorrowsResponseDTO message(String message) {
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

  public PostBookBorrowsResponseDTO traceId(String traceId) {
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

  public PostBookBorrowsResponseDTO data(PostBookBorrowsDataResponseDTO data) {
    this.data = data;
    return this;
  }

  /**
   * Get data
   * @return data
   */
  @NotNull @Valid 
  @Schema(name = "data", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public PostBookBorrowsDataResponseDTO getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(PostBookBorrowsDataResponseDTO data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostBookBorrowsResponseDTO postBookBorrowsResponseDTO = (PostBookBorrowsResponseDTO) o;
    return Objects.equals(this.code, postBookBorrowsResponseDTO.code) &&
        Objects.equals(this.message, postBookBorrowsResponseDTO.message) &&
        Objects.equals(this.traceId, postBookBorrowsResponseDTO.traceId) &&
        Objects.equals(this.data, postBookBorrowsResponseDTO.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, traceId, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PostBookBorrowsResponseDTO {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

