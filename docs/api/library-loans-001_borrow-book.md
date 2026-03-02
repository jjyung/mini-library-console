# API Flow: library-loans-001 Borrow Book

- API ID: `library-loans-001`
- Path: `POST /library/loans`

## Main Flow

```mermaid
flowchart TD
    A[Receive borrow request] --> B{Required fields valid?}
    B -- No --> E1[Return A0003]
    B -- Yes --> C{Book exists?}
    C -- No --> E2[Return A0001]
    C -- Yes --> D{Book on shelf?}
    D -- No --> E3[Return A0005]
    D -- Yes --> F{availableQuantity > 0?}
    F -- No --> E4[Return A0004]
    F -- Yes --> G[Begin tx: decrease inventory and create loan]
    G --> H[Set dueDate from request or default +14 days]
    H --> I[Commit tx and return 00000]
    G -->|Unexpected failure| E5[Rollback and return B0000]
```

## Given/When/Then Rules

1. Given valid `readerId` and existing `isbn` with available stock on shelf
   When `POST /library/loans` is called
   Then create loan, decrease available quantity by 1, and return `00000`.

2. Given missing required fields
   When `POST /library/loans` is called
   Then return `A0003`.

3. Given target `isbn` does not exist
   When `POST /library/loans` is called
   Then return `A0001`.

4. Given book shelf status is off shelf
   When `POST /library/loans` is called
   Then return `A0005`.

5. Given `availableQuantity` equals 0
   When `POST /library/loans` is called
   Then return `A0004`.

6. Given due date is omitted
   When borrow succeeds
   Then apply default due date as borrowed date + 14 days.
