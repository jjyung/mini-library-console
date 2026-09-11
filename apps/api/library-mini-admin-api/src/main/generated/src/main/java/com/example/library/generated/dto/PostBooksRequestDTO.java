package com.example.library.generated.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
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
 * PostBooksRequestDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-09T12:37:43.604229+08:00[Asia/Taipei]", comments = "Generator version: 7.25.0")
public class PostBooksRequestDTO {

  private String title;

  private String isbn;

  private JsonNullable<@Size(max = 100) String> author = JsonNullable.<String>undefined();

  /**
   * 書籍分類。
   */
  public enum CategoryEnum {
    LITERATURE("literature"),
    
    SCIENCE("science"),
    
    TECHNOLOGY("technology"),
    
    HISTORY("history"),
    
    ART("art"),
    
    PHILOSOPHY("philosophy"),
    
    BUSINESS("business"),
    
    EDUCATION("education");

    private final String value;

    CategoryEnum(String value) {
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
    public static CategoryEnum fromValue(String value) {
      for (CategoryEnum b : CategoryEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private CategoryEnum category;

  private Integer quantity;

  private Boolean isActive = true;

  public PostBooksRequestDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PostBooksRequestDTO(String title, String isbn, CategoryEnum category, Integer quantity, Boolean isActive) {
    this.title = title;
    this.isbn = isbn;
    this.category = category;
    this.quantity = quantity;
    this.isActive = isActive;
  }

  public PostBooksRequestDTO title(String title) {
    this.title = title;
    return this;
  }

  /**
   * 書名。
   * @return title
   */
  @NotNull @Size(min = 1, max = 200) 
  @Schema(name = "title", description = "書名。", requiredMode = Schema.RequiredMode.REQUIRED)
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
   * 書目的唯一 ISBN 識別值。
   * @return isbn
   */
  @NotNull @Size(min = 1, max = 20) 
  @Schema(name = "isbn", description = "書目的唯一 ISBN 識別值。", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isbn")
  public String getIsbn() {
    return isbn;
  }

  @JsonProperty("isbn")
  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public PostBooksRequestDTO author(String author) {
    this.author = JsonNullable.of(author);
    return this;
  }

  /**
   * 作者；依 Figma MVP baseline 可省略。
   * @return author
   */
  @Size(max = 100) 
  @Schema(name = "author", description = "作者；依 Figma MVP baseline 可省略。", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
  @JsonProperty("author")
  public JsonNullable<@Size(max = 100) String> getAuthor() {
    return author;
  }

  public void setAuthor(JsonNullable<String> author) {
    this.author = author;
  }

  public PostBooksRequestDTO category(CategoryEnum category) {
    this.category = category;
    return this;
  }

  /**
   * 書籍分類。
   * @return category
   */
  @NotNull 
  @Schema(name = "category", description = "書籍分類。", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public CategoryEnum getCategory() {
    return category;
  }

  @JsonProperty("category")
  public void setCategory(CategoryEnum category) {
    this.category = category;
  }

  public PostBooksRequestDTO quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * 初始複本數量。
   * minimum: 1
   * @return quantity
   */
  @NotNull @Min(value = 1) 
  @Schema(name = "quantity", description = "初始複本數量。", requiredMode = Schema.RequiredMode.REQUIRED)
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
   * 是否上架並可供借閱。
   * @return isActive
   */
  @NotNull 
  @Schema(name = "isActive", description = "是否上架並可供借閱。", requiredMode = Schema.RequiredMode.REQUIRED)
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
        equalsNullable(this.author, postBooksRequestDTO.author) &&
        Objects.equals(this.category, postBooksRequestDTO.category) &&
        Objects.equals(this.quantity, postBooksRequestDTO.quantity) &&
        Objects.equals(this.isActive, postBooksRequestDTO.isActive);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, isbn, hashCodeNullable(author), category, quantity, isActive);
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

