# `loans` Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-09-01 | 依 REQ-LIB-001 建立借閱與歸還紀錄的 schema draft。 |

## 2. Schema

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `loan_id` | `UUID` | 否 | application generated | 主鍵。 | 一次借閱的穩定識別碼。 | `loanId` |
| `book_id` | `UUID` | 否 | 無 | 外鍵參照 `books.book_id`。 | 被借閱的書籍聚合識別碼。 | `bookId` |
| `reader_id` | `VARCHAR(100)` | 否 | 無 | 去除前後空白後不可為空。 | 借閱人識別資訊；目前沿用 Figma 的讀者 ID 語意。 | `readerId` |
| `due_date` | `DATE` | 是 | `NULL` | 只有輸入時保存；預設期限與逾期規則待 Q-003。 | 可選到期日。 | `dueDate` |
| `loaned_at` | `TIMESTAMPTZ` | 否 | `CURRENT_TIMESTAMP` | 建立後不可修改。 | 借出時間（UTC）。 | `loanedAt` |
| `returned_at` | `TIMESTAMPTZ` | 是 | `NULL` | `NULL` 表示借閱中；有值時不得早於 `loaned_at`。 | 歸還時間（UTC）。 | `returned` 由此衍生 |
| `version` | `BIGINT` | 否 | `0` | 成功歸還時遞增，用於併發控制。 | 借閱紀錄版本。 | 不直接輸出 |

### 2.4 限制條件 (Constraints)

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_loans` | 主鍵 | `loan_id` 唯一識別一筆借閱。 | 回傳 `B0001`，不暴露資料庫錯誤。 |
| `fk_loans_book_id` | 外鍵 | `book_id` 必須對應既有 `books`。 | 借書找不到書籍回傳 `A0002`；資料完整性異常回傳 `B0001`。 |
| `ck_loans_reader_not_blank` | 檢查條件 | `reader_id` 去除前後空白後不可為空。 | API 層回傳 `A0001`。 |
| `ck_loans_returned_after_loaned` | 檢查條件 | `returned_at IS NULL OR returned_at >= loaned_at`。 | 回傳 `B0001`，不得產生部分歸還。 |
| `idx_loans_active_by_book` | 部分索引 | 支援依 `book_id` 找借閱中紀錄（`returned_at IS NULL`）。 | migration 失敗視為部署錯誤。 |
| `idx_loans_reader_created_at` | 索引 | 支援後續依借閱人與時間追查；本次不提供歷史查詢 API。 | 不影響本次 API 業務結果。 |
| `aggregate_books_count_consistency` | 應用服務一致性規則 | 借出／歸還須與 `books.available_count` 同一一致性邊界完成；不以跨表 CHECK 取代。 | 競爭更新或不一致回傳 `B0001`，並要求重新載入。 |

## 3. DDL

```sql
CREATE TABLE loans (
    loan_id UUID NOT NULL,
    book_id UUID NOT NULL,
    reader_id VARCHAR(100) NOT NULL,
    due_date DATE NULL,
    loaned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMPTZ NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_loans PRIMARY KEY (loan_id),
    CONSTRAINT fk_loans_book_id FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT ck_loans_reader_not_blank CHECK (BTRIM(reader_id) <> ''),
    CONSTRAINT ck_loans_returned_after_loaned CHECK (
        returned_at IS NULL OR returned_at >= loaned_at
    )
);

CREATE INDEX idx_loans_active_by_book
    ON loans (book_id, loaned_at)
    WHERE returned_at IS NULL;

CREATE INDEX idx_loans_reader_created_at
    ON loans (reader_id, loaned_at DESC);
```

> `returned_at IS NULL` 是本次「借閱中」的唯一判定。當同 ISBN 有多筆借閱中紀錄且 request 未提供 `readerId` 時，API flow 採最早借出紀錄（FIFO）作為預設；Q-002／Q-001 確認後可改為強制指定 `loanId` 或 reader match。
