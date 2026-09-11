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
 * GetBooksRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class GetBooksRequestDTO {

  private @Nullable String query;

  private Integer page = 1;

  private Integer pageSize = 20;

  public GetBooksRequestDTO query(@Nullable String query) {
    this.query = query;
    return this;
  }

  /**
   * 書名、ISBN 或作者的搜尋文字。
   * @return query
   */
  @Size(max = 100) 
  @Schema(name = "query", description = "書名、ISBN 或作者的搜尋文字。", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("query")
  public @Nullable String getQuery() {
    return query;
  }

  @JsonProperty("query")
  public void setQuery(@Nullable String query) {
    this.query = query;
  }

  public GetBooksRequestDTO page(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * 頁碼，從 1 開始。
   * minimum: 1
   * @return page
   */
  @Min(value = 1) 
  @Schema(name = "page", description = "頁碼，從 1 開始。", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("page")
  public Integer getPage() {
    return page;
  }

  @JsonProperty("page")
  public void setPage(Integer page) {
    this.page = page;
  }

  public GetBooksRequestDTO pageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * 每頁筆數。
   * minimum: 1
   * maximum: 100
   * @return pageSize
   */
  @Min(value = 1) @Max(value = 100) 
  @Schema(name = "pageSize", description = "每頁筆數。", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("pageSize")
  public Integer getPageSize() {
    return pageSize;
  }

  @JsonProperty("pageSize")
  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetBooksRequestDTO getBooksRequestDTO = (GetBooksRequestDTO) o;
    return Objects.equals(this.query, getBooksRequestDTO.query) &&
        Objects.equals(this.page, getBooksRequestDTO.page) &&
        Objects.equals(this.pageSize, getBooksRequestDTO.pageSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, page, pageSize);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetBooksRequestDTO {\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
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

