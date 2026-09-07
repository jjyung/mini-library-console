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
 * PostBooksRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-07T10:06:39.879502+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostBooksRequestDTO {

  private String title;

  private String isbn;

  private @Nullable String author;

  private String category;

  private Integer quantity;

  private Boolean isActive = true;

  public PostBooksRequestDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostBooksRequestDTO(String title, String isbn, String category, Integer quantity) {
    this.title = title;
    this.isbn = isbn;
    this.category = category;
    this.quantity = quantity;
  }

  public PostBooksRequestDTO title(String title) {
    this.title = title;
    return this;
  }

  /**
   * 書名
   * @return title
   */
  @NotNull @Size(min = 1, max = 200) 
  @Schema(name = "title", description = "書名", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  public PostBooksRequestDTO isbn(String isbn) {
    this.isbn = isbn;
    return this;
  }

  /**
   * 書籍唯一識別值；MVP 以 ISBN 實作
   * @return isbn
   */
  @NotNull @Size(min = 1, max = 20) 
  @Schema(name = "isbn", description = "書籍唯一識別值；MVP 以 ISBN 實作", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isbn")
  public String getIsbn() {
    return isbn;
  }

  @JsonProperty("isbn")
  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public PostBooksRequestDTO author(@Nullable String author) {
    this.author = author;
    return this;
  }

  /**
   * 作者；可選
   * @return author
   */
  @Size(max = 200) 
  @Schema(name = "author", description = "作者；可選", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("author")
  public @Nullable String getAuthor() {
    return author;
  }

  @JsonProperty("author")
  public void setAuthor(@Nullable String author) {
    this.author = author;
  }

  public PostBooksRequestDTO category(String category) {
    this.category = category;
    return this;
  }

  /**
   * 分類
   * @return category
   */
  @NotNull @Size(min = 1, max = 30) 
  @Schema(name = "category", description = "分類", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }

  @JsonProperty("category")
  public void setCategory(String category) {
    this.category = category;
  }

  public PostBooksRequestDTO quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * 初始副本總數
   * minimum: 1
   * @return quantity
   */
  @NotNull @Min(value = 1) 
  @Schema(name = "quantity", description = "初始副本總數", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("quantity")
  public Integer getQuantity() {
    return quantity;
  }

  @JsonProperty("quantity")
  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public PostBooksRequestDTO isActive(Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * 是否上架；未提供時預設為 true
   * @return isActive
   */
  
  @Schema(name = "isActive", description = "是否上架；未提供時預設為 true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isActive")
  public Boolean getIsActive() {
    return isActive;
  }

  @JsonProperty("isActive")
  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostBooksRequestDTO postBooksRequestDTO = (PostBooksRequestDTO) o;
    return Objects.equals(this.title, postBooksRequestDTO.title) &&
        Objects.equals(this.isbn, postBooksRequestDTO.isbn) &&
        Objects.equals(this.author, postBooksRequestDTO.author) &&
        Objects.equals(this.category, postBooksRequestDTO.category) &&
        Objects.equals(this.quantity, postBooksRequestDTO.quantity) &&
        Objects.equals(this.isActive, postBooksRequestDTO.isActive);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, isbn, author, category, quantity, isActive);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PostBooksRequestDTO {\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    isbn: ").append(toIndentedString(isbn)).append("\n");
    sb.append("    author: ").append(toIndentedString(author)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
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

