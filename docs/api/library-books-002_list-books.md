# API Flow: library-books-002 List Books

- API ID: `library-books-002`
- Path: `GET /library/books`

## Main Flow

```mermaid
flowchart TD
    A[Receive list books request] --> B{Optional filter valid?}
    B -- No --> E1[Return A0003]
    B -- Yes --> C[Query books table]
    C --> D[Map rows to response DTO]
    D --> F[Return 00000 with books array]
    C -->|Unexpected failure| E2[Return B0000]
```

## Given/When/Then Rules

1. Given no filter or valid `shelfStatus` filter
   When `GET /library/books` is called
   Then return `00000` with current book list.

2. Given invalid query format
   When `GET /library/books` is called
   Then return `A0003`.

3. Given database query failure
   When list operation executes
   Then return `B0000`.
