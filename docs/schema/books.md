# `books` Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-09-01 | 依 REQ-LIB-001 定義書目、館藏數量與上架狀態的 schema draft。 |

## 2. Schema

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `book_id` | `UUID` | 否 | application generated | 主鍵。 | 書籍聚合的穩定識別碼。 | `bookId` |
| `title` | `VARCHAR(200)` | 否 | 無 | 去除前後空白後不可為空。 | 書名。 | `title` |
| `isbn` | `VARCHAR(13)` | 否 | 無 | 儲存正規化 ISBN（移除連字號、X 大寫）；唯一。 | 書籍國際標準書號。 | `isbn` |
| `author` | `VARCHAR(200)` | 是 | `NULL` | 有值時去除前後空白。 | 作者，可選。 | `author` |
| `category` | `VARCHAR(30)` | 否 | 無 | 僅接受 OpenAPI 定義的分類代碼。 | 書籍分類代碼。 | `category` |
| `status` | `VARCHAR(20)` | 否 | `'AVAILABLE'` | 僅允許 `AVAILABLE`, `BORROWED`, `INACTIVE`；由上架狀態與數量維護。 | 對管理介面呈現的彙總狀態。 | `status` |
| `is_active` | `BOOLEAN` | 否 | `TRUE` | `FALSE` 時不得借閱。 | 是否上架。 | `isActive` |
| `available_count` | `INTEGER` | 否 | 無 | `0 <= available_count <= total_count`。 | 目前可借副本數。 | `availableCount` |
| `total_count` | `INTEGER` | 否 | 無 | 必須大於或等於 1。 | 館藏總數。 | `totalCount` |
| `version` | `BIGINT` | 否 | `0` | 每次成功狀態異動遞增，用於併發控制。 | 聚合版本。 | 不直接輸出 |
| `created_at` | `TIMESTAMPTZ` | 否 | `CURRENT_TIMESTAMP` | 建立後不可修改。 | 建立時間（UTC）。 | 不直接輸出 |
| `updated_at` | `TIMESTAMPTZ` | 否 | `CURRENT_TIMESTAMP` | 必須大於或等於 `created_at`。 | 最近異動時間（UTC）。 | 不直接輸出 |

### 2.4 限制條件 (Constraints)

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_books` | 主鍵 | `book_id` 唯一識別一筆書籍。 | 回傳 `B0001`，不暴露資料庫錯誤。 |
| `uk_books_isbn` | 唯一約束 | 正規化後 `isbn` 不得重複。 | 回傳 `A0003`。 |
| `ck_books_title_not_blank` | 檢查條件 | `title` 去除前後空白後不可為空。 | API 層回傳 `A0001`。 |
| `ck_books_quantity_range` | 檢查條件 | `total_count >= 1` 且 `available_count BETWEEN 0 AND total_count`。 | 回傳 `A0001`；若異動後違反則回傳 `B0001`。 |
| `ck_books_status_values` | 檢查條件 | `status IN ('AVAILABLE', 'BORROWED', 'INACTIVE')`。 | 回傳 `B0001`，並記錄 constraint violation。 |
| `ck_books_status_consistency` | 檢查條件 | 未上架必為 `INACTIVE`；上架且無可借數量為 `BORROWED`；其餘上架書為 `AVAILABLE`。 | 回傳 `B0001`，不得讓前端顯示部分成功。 |
| `ck_books_timestamp_order` | 檢查條件 | `updated_at >= created_at`。 | 回傳 `B0001`，以 `traceId` 關聯 log。 |
| `idx_books_status_updated_at` | 索引 | 支援館藏列表依狀態／更新時間讀取；非業務唯一性依據。 | migration 失敗視為部署錯誤，不轉成 client error。 |

## 3. DDL

```sql
CREATE TABLE books (
    book_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(13) NOT NULL,
    author VARCHAR(200) NULL,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    available_count INTEGER NOT NULL,
    total_count INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_title_not_blank CHECK (BTRIM(title) <> ''),
    CONSTRAINT ck_books_quantity_range CHECK (
        total_count >= 1 AND available_count BETWEEN 0 AND total_count
    ),
    CONSTRAINT ck_books_status_values CHECK (
        status IN ('AVAILABLE', 'BORROWED', 'INACTIVE')
    ),
    CONSTRAINT ck_books_status_consistency CHECK (
        (is_active = FALSE AND status = 'INACTIVE')
        OR (is_active = TRUE AND available_count = 0 AND status = 'BORROWED')
        OR (is_active = TRUE AND available_count > 0 AND status = 'AVAILABLE')
    ),
    CONSTRAINT ck_books_timestamp_order CHECK (updated_at >= created_at)
);

CREATE INDEX idx_books_status_updated_at
    ON books (status, updated_at DESC);
```

> DDL 為關聯式 Store draft。Store 產品、migration 工具與正式環境設定仍由 PG／BE 依 Q-005 凍結。
