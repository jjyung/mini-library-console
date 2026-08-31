# User Schema Template

> 當需求新增 `users` table 時，將此檔案複製為
> `docs/schema/users.md`。每一張實體資料表使用一個 Markdown 檔案。
> 欄位名稱、table 名稱、constraint 名稱、API 欄位與 SQL 保留英文；說明
> 內容使用中文。實際欄位與規則仍須以需求文件與架構文件為準。

## 1. 修改紀錄

每次修改都要記錄修改人、日期，以及修改原因與內容。

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-08-31 | 建立 `users` table 的 schema reference template。 |
| [version] | [name or role] | [YYYY-MM-DD] | [填寫修改原因與具體變更內容] |

## 2. Schema

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `user_id` | `UUID` | 否 | 無 | 主鍵。 | 使用者的穩定識別碼。 | `userId` |
| `username` | `VARCHAR(50)` | 否 | 無 | 不分大小寫唯一；必須以英文字母或數字開頭，只允許英數字、`.`, `_`, `-`。 | 使用者登入名稱或識別名稱。 | `username` |
| `display_name` | `VARCHAR(100)` | 否 | 無 | 不可為空白字串。 | 顯示在管理介面的使用者名稱。 | `displayName` |
| `email` | `VARCHAR(254)` | 否 | 無 | 不分大小寫唯一；API 層需驗證 email 格式。 | 使用者聯絡信箱。 | `email` |
| `role` | `VARCHAR(20)` | 否 | `'USER'` | 僅允許 `ADMIN`, `USER`。若要增加值，必須同步更新授權模型。 | 使用者的授權角色。 | `role` |
| `status` | `VARCHAR(20)` | 否 | `'ACTIVE'` | 僅允許 `ACTIVE`, `INACTIVE`。 | 使用者帳號生命週期狀態。 | `status` |
| `last_login_at` | `TIMESTAMPTZ` | 是 | `NULL` | 有值時必須為 UTC 時間戳。 | 最近一次成功登入時間。 | `lastLoginAt` |
| `created_at` | `TIMESTAMPTZ` | 否 | `CURRENT_TIMESTAMP` | 新增後不可修改。 | 資料建立時間。 | `createdAt` |
| `updated_at` | `TIMESTAMPTZ` | 否 | `CURRENT_TIMESTAMP` | 必須大於或等於 `created_at`；每次成功異動時更新。 | 最近一次資料異動時間。 | `updatedAt` |

### 2.4 限制條件 (Constraints)

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_users` | 主鍵 | `user_id` 唯一識別一筆資料。 | 請求資料錯誤時回傳 `A0000`，不可直接暴露資料庫錯誤。 |
| `uk_users_username_ci` | 唯一索引 | `LOWER(username)` 必須唯一，不區分大小寫。 | 回傳 `A0000`，並可附上 `username` 欄位衝突原因。 |
| `uk_users_email_ci` | 唯一索引 | `LOWER(email)` 必須唯一，不區分大小寫。 | 回傳 `A0000`，並可附上 `email` 欄位衝突原因。 |
| `ck_users_username_format` | 檢查條件 | 在資料庫層再次限制 username 字元格式。 | 拒絕不符合規則的資料，並以 trace ID 記錄 constraint violation。 |
| `ck_users_display_name_not_blank` | 檢查條件 | `BTRIM(display_name)` 不可為空字串。 | 請求驗證失敗時回傳 `A0000`。 |
| `ck_users_role` | 檢查條件 | `role IN ('ADMIN', 'USER')`。 | 收到不支援的 role 時回傳 `A0000`。 |
| `ck_users_status` | 檢查條件 | `status IN ('ACTIVE', 'INACTIVE')`。 | 收到不支援的 status 時回傳 `A0000`。 |
| `ck_users_timestamp_order` | 檢查條件 | `updated_at >= created_at`。 | 視為資料完整性或系統錯誤，回傳 `B0000`。 |

## 3. DDL

```sql
CREATE TABLE users (
    user_id UUID NOT NULL,
    username VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT ck_users_username_format CHECK (
        username ~ '^[A-Za-z0-9][A-Za-z0-9._-]*$'
    ),
    CONSTRAINT ck_users_display_name_not_blank CHECK (
        BTRIM(display_name) <> ''
    ),
    CONSTRAINT ck_users_role CHECK (
        role IN ('ADMIN', 'USER')
    ),
    CONSTRAINT ck_users_status CHECK (
        status IN ('ACTIVE', 'INACTIVE')
    ),
    CONSTRAINT ck_users_timestamp_order CHECK (
        updated_at >= created_at
    )
);

CREATE UNIQUE INDEX uk_users_username_ci
    ON users (LOWER(username));

CREATE UNIQUE INDEX uk_users_email_ci
    ON users (LOWER(email));

CREATE INDEX idx_users_status_created_at
    ON users (status, created_at DESC);
```
