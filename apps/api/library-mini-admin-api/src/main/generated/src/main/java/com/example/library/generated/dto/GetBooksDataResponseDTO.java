package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.example.library.generated.dto.BookSummaryResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * GetBooksDataResponseDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class GetBooksDataResponseDTO {

  private List<@Valid BookSummaryResponseDTO> items = new ArrayList<>();

  private Integer page;

  private Integer pageSize;

  private Integer totalPages;

  private Integer total;

  public GetBooksDataResponseDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GetBooksDataResponseDTO(List<@Valid BookSummaryResponseDTO> items, Integer page, Integer pageSize, Integer totalPages, Integer total) {
    this.items = items;
    this.page = page;
    this.pageSize = pageSize;
    this.totalPages = totalPages;
    this.total = total;
  }

  public GetBooksDataResponseDTO items(List<@Valid BookSummaryResponseDTO> items) {
    this.items = items;
    return this;
  }

  public GetBooksDataResponseDTO addItemsItem(BookSummaryResponseDTO itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
   */
  @NotNull @Valid 
  @Schema(name = "items", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("items")
  public List<@Valid BookSummaryResponseDTO> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<@Valid BookSummaryResponseDTO> items) {
    this.items = items;
  }

  public GetBooksDataResponseDTO page(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * Get page
   * minimum: 1
   * @return page
   */
  @NotNull @Min(value = 1) 
  @Schema(name = "page", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("page")
  public Integer getPage() {
    return page;
  }

  @JsonProperty("page")
  public void setPage(Integer page) {
    this.page = page;
  }

  public GetBooksDataResponseDTO pageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * Get pageSize
   * minimum: 1
   * @return pageSize
   */
  @NotNull @Min(value = 1) 
  @Schema(name = "pageSize", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("pageSize")
  public Integer getPageSize() {
    return pageSize;
  }

  @JsonProperty("pageSize")
  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public GetBooksDataResponseDTO totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * Get totalPages
   * minimum: 0
   * @return totalPages
   */
  @NotNull @Min(value = 0) 
  @Schema(name = "totalPages", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalPages")
  public Integer getTotalPages() {
    return totalPages;
  }

  @JsonProperty("totalPages")
  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public GetBooksDataResponseDTO total(Integer total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * minimum: 0
   * @return total
   */
  @NotNull @Min(value = 0) 
  @Schema(name = "total", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }

  @JsonProperty("total")
  public void setTotal(Integer total) {
    this.total = total;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetBooksDataResponseDTO getBooksDataResponseDTO = (GetBooksDataResponseDTO) o;
    return Objects.equals(this.items, getBooksDataResponseDTO.items) &&
        Objects.equals(this.page, getBooksDataResponseDTO.page) &&
        Objects.equals(this.pageSize, getBooksDataResponseDTO.pageSize) &&
        Objects.equals(this.totalPages, getBooksDataResponseDTO.totalPages) &&
        Objects.equals(this.total, getBooksDataResponseDTO.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, page, pageSize, totalPages, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetBooksDataResponseDTO {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

