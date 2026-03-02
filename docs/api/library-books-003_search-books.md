# API Flow: library-books-003 Search Books

- API ID: `library-books-003`
- Path: `GET /library/books/search`

## Main Flow

```mermaid
flowchart TD
    A[Receive search request] --> B{keyword provided and valid?}
    B -- No --> E1[Return A0003]
    B -- Yes --> C[Search books by title or isbn or author]
    C --> D[Map results]
    D --> F[Return 00000 with matched books]
    C -->|Unexpected failure| E2[Return B0000]
```

## Given/When/Then Rules

1. Given non-empty `keyword`
   When `GET /library/books/search` is called
   Then search by title/ISBN/author and return `00000`.

2. Given missing or empty `keyword`
   When `GET /library/books/search` is called
   Then return `A0003`.

3. Given query execution error
   When search operation executes
   Then return `B0000`.
