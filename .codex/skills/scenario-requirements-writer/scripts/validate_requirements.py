#!/usr/bin/env python3
"""Validate a scenario requirements document against the SA skill contract.

This validator checks one or more rendered requirement documents. It verifies
document structure and local completeness; it deliberately does not validate
cross-artifact traceability or the semantic correctness of business decisions.
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional, Sequence


EXPECTED_SECTIONS = (
    "文件資訊",
    "1) 需求摘要",
    "2) Functional Requirements (FR)",
    "3) Non-Functional Requirements (NFR)",
    "4) Acceptance Criteria (AC, Given/When/Then)",
    "業務錯誤情境與錯誤碼需求",
    "UI → API 對照表",
    "5) 風險、假設與待確認事項",
)

SUMMARY_FIELDS = (
    "情境目標",
    "使用者角色",
    "核心流程",
    "主要畫面區塊",
    "In-scope",
    "Out-of-scope",
    "前置條件與外部依賴",
    "名詞定義",
)

FR_FIELDS = (
    "使用者／觸發者",
    "前置條件",
    "使用者輸入",
    "業務行為",
    "成功結果",
    "失敗結果",
)

NFR_FIELDS = ("類型", "需求", "驗證方式", "MVP 目標或限制")

ID_PATTERNS = (
    re.compile(r"FR(?:-UI)?-\d{3}"),
    re.compile(r"NFR-\d{3}"),
    re.compile(r"AC(?:-UI)?-\d{3}"),
    re.compile(r"BR-\d{3}"),
    re.compile(r"EX-\d{3}"),
    re.compile(r"Q-\d{3}"),
)

PLACEHOLDER_PATTERN = re.compile(r"<[^>\n]+>")
API_ID_PATTERN = re.compile(r"[a-z0-9]+(?:-[a-z0-9]+)+-\d{3}")
HEADING_PATTERN = re.compile(r"^(#{1,6})\s+(.+?)\s*$")
H2_PATTERN = re.compile(r"^##\s+(.+?)\s*$")
H3_PATTERN = re.compile(r"^###\s+(.+?)\s*$")
TABLE_SEPARATOR_PATTERN = re.compile(r"^:?-{3,}:?$")
EMPTY_VALUES = {"", "-", "—", "待填寫", "TODO", "TBD"}


@dataclass(frozen=True)
class Issue:
    severity: str
    message: str
    line_number: Optional[int] = None


@dataclass(frozen=True)
class Section:
    title: str
    start: int
    end: int


@dataclass(frozen=True)
class Table:
    headers: list[str]
    rows: list[list[str]]
    line_number: int


def normalized(value: str) -> str:
    return re.sub(r"\s+", "", value.strip())


def split_table_row(line: str) -> list[str]:
    stripped = line.strip()
    if not stripped.startswith("|") or "|" not in stripped[1:]:
        return []
    return [cell.strip() for cell in stripped.strip("|").split("|")]


def is_table_separator(cells: Sequence[str]) -> bool:
    return bool(cells) and all(
        TABLE_SEPARATOR_PATTERN.fullmatch(cell.replace(" ", ""))
        for cell in cells
    )


def find_table(lines: Sequence[str], start: int, end: int, required_headers: Sequence[str]) -> Optional[Table]:
    normalized_headers = [normalized(header) for header in required_headers]
    for index in range(start, end - 1):
        headers = split_table_row(lines[index])
        separator = split_table_row(lines[index + 1])
        if not headers or not is_table_separator(separator):
            continue
        header_text = [normalized(header) for header in headers]
        if not all(
            any(required in header for header in header_text)
            for required in normalized_headers
        ):
            continue

        rows: list[list[str]] = []
        row_index = index + 2
        while row_index < end:
            row = split_table_row(lines[row_index])
            if not row:
                break
            rows.append(row)
            row_index += 1
        return Table(headers=headers, rows=rows, line_number=index + 1)
    return None


def section_map(lines: Sequence[str]) -> tuple[dict[str, Section], list[Issue]]:
    headings: list[tuple[str, int]] = []
    issues: list[Issue] = []
    for index, line in enumerate(lines):
        match = H2_PATTERN.match(line)
        if match:
            headings.append((match.group(1), index))

    sections: dict[str, Section] = {}
    for heading_index, (title, start) in enumerate(headings):
        end = headings[heading_index + 1][1] if heading_index + 1 < len(headings) else len(lines)
        if title in sections:
            issues.append(Issue("ERROR", f"重複的 H2 章節：{title}", start + 1))
        else:
            sections[title] = Section(title=title, start=start + 1, end=end)
    return sections, issues


def h3_blocks(lines: Sequence[str], start: int, end: int) -> Iterable[tuple[str, int, int]]:
    headings: list[tuple[str, int]] = []
    for index in range(start, end):
        match = H3_PATTERN.match(lines[index])
        if match:
            headings.append((match.group(1), index))
    for heading_index, (title, heading_line) in enumerate(headings):
        block_end = headings[heading_index + 1][1] if heading_index + 1 < len(headings) else end
        yield title, heading_line, block_end


def field_value(lines: Sequence[str], start: int, end: int, label: str) -> tuple[Optional[str], Optional[int]]:
    pattern = re.compile(rf"^\s*-\s*{re.escape(label)}\s*[:：]\s*(.*?)\s*$")
    for index in range(start, end):
        match = pattern.match(lines[index])
        if match:
            return match.group(1).strip(), index + 1
    return None, None


def is_empty_value(value: Optional[str]) -> bool:
    if value is None:
        return True
    return value.strip() in EMPTY_VALUES


def has_placeholder(value: str) -> bool:
    return bool(PLACEHOLDER_PATTERN.search(value))


def block_body(lines: Sequence[str], start: int, end: int) -> str:
    return "\n".join(lines[start:end]).strip()


def first_column(table: Table, row: Sequence[str]) -> str:
    return row[0].strip() if row else ""


def header_index(table: Table, label: str) -> Optional[int]:
    target = normalized(label)
    for index, header in enumerate(table.headers):
        if target in normalized(header):
            return index
    return None


def cell_value(table: Table, row: Sequence[str], label: str) -> str:
    index = header_index(table, label)
    if index is None or index >= len(row):
        return ""
    return row[index].strip()


def check_required_sections(sections: dict[str, Section], issues: list[Issue]) -> None:
    for title in EXPECTED_SECTIONS:
        if title not in sections:
            issues.append(Issue("ERROR", f"缺少必要章節：{title}"))


def check_metadata(lines: Sequence[str], issues: list[Issue]) -> None:
    metadata = {
        "需求文件 ID": re.compile(r"^REQ-[A-Za-z0-9]+-\d{3}$"),
        "來源情境": re.compile(r"^SCN-[A-Za-z0-9]+-\d{3}$"),
    }
    values: dict[str, str] = {}
    for label, pattern in metadata.items():
        value, line_number = field_value(lines, 0, len(lines), label)
        if is_empty_value(value) or value is None:
            issues.append(Issue("ERROR", f"文件資訊欄位不可為空：{label}", line_number))
            continue
        values[label] = value
        if not pattern.fullmatch(value):
            issues.append(Issue("ERROR", f"{label} 格式不正確：{value}", line_number))

    ui_source, ui_line = field_value(lines, 0, len(lines), "UI 設計來源")
    if is_empty_value(ui_source) or ui_source is None:
        issues.append(Issue("ERROR", "文件資訊欄位不可為空：UI 設計來源", ui_line))
    elif has_placeholder(ui_source):
        issues.append(Issue("ERROR", "UI 設計來源仍包含 placeholder", ui_line))

    title_match = re.search(r"^#\s+(SCN-[A-Za-z0-9]+-\d{3})\b", "\n".join(lines), re.MULTILINE)
    if title_match and values.get("來源情境") and title_match.group(1) != values["來源情境"]:
        issues.append(Issue("ERROR", "標題中的 SCN-ID 與文件資訊不一致"))


def check_summary(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    for label in SUMMARY_FIELDS:
        value, line_number = field_value(lines, section.start, section.end, label)
        if is_empty_value(value):
            issues.append(Issue("ERROR", f"需求摘要欄位不可為空：{label}", line_number or section.start + 1))


def check_fr(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    normal_fr_count = 0
    ui_fr_found = False
    for title, heading_line, block_end in h3_blocks(lines, section.start, section.end):
        fr_match = re.match(r"^(FR-(?:UI-)?\d{3})\s+(.+)$", title)
        if not fr_match:
            continue
        requirement_id = fr_match.group(1)
        body = block_body(lines, heading_line + 1, block_end)
        if requirement_id == "FR-UI-001":
            ui_fr_found = True
            if "對齊" not in body:
                issues.append(Issue("ERROR", "FR-UI-001 必須明確要求 UI 對齊設計來源", heading_line + 1))
            continue

        normal_fr_count += 1
        for label in FR_FIELDS:
            value, line_number = field_value(lines, heading_line + 1, block_end, label)
            if is_empty_value(value):
                issues.append(Issue("ERROR", f"{requirement_id} 欄位不可為空：{label}", line_number or heading_line + 1))

    if normal_fr_count == 0:
        issues.append(Issue("ERROR", "至少需要一項一般 FR"))
    if not ui_fr_found:
        issues.append(Issue("ERROR", "缺少 FR-UI-001 前端畫面對齊需求"))

    rule_table = find_table(lines, section.start, section.end, ("Rule ID", "業務規則", "適用流程"))
    check_table_has_data(rule_table, "業務規則與資料約束", issues, section.start + 1)

    state_table = find_table(lines, section.start, section.end, ("目前狀態", "觸發動作", "下一狀態"))
    check_table_has_data(state_table, "狀態與轉移", issues, section.start + 1)

    exception_table = find_table(lines, section.start, section.end, ("Case ID", "情境", "觸發條件"))
    check_table_has_data(exception_table, "例外與邊界情境", issues, section.start + 1)


def check_table_has_data(table: Optional[Table], table_name: str, issues: list[Issue], line_number: int) -> None:
    if table is None:
        issues.append(Issue("ERROR", f"缺少表格：{table_name}", line_number))
        return
    if not any(any(not is_empty_value(cell) for cell in row) for row in table.rows):
        issues.append(Issue("ERROR", f"表格不可沒有資料：{table_name}", table.line_number))


def check_nfr(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    nfr_count = 0
    for title, heading_line, block_end in h3_blocks(lines, section.start, section.end):
        if not re.match(r"^NFR-\d{3}\s+.+$", title):
            continue
        nfr_count += 1
        for label in NFR_FIELDS:
            value, line_number = field_value(lines, heading_line + 1, block_end, label)
            if is_empty_value(value):
                issues.append(Issue("ERROR", f"{title.split()[0]} 欄位不可為空：{label}", line_number or heading_line + 1))
    if nfr_count == 0:
        issues.append(Issue("ERROR", "至少需要一項 NFR"))


def check_ac(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    normal_ac_count = 0
    ui_ac_found = False
    for title, heading_line, block_end in h3_blocks(lines, section.start, section.end):
        ac_match = re.match(r"^(AC-(?:UI-)?\d{3})(?:\s+.*)?$", title)
        if not ac_match:
            continue
        acceptance_id = ac_match.group(1)
        body = block_body(lines, heading_line + 1, block_end)
        for keyword in ("Given", "When", "Then"):
            if not re.search(rf"^\s*-\s*{keyword}\b\s*\S+", body, re.MULTILINE):
                issues.append(Issue("ERROR", f"{acceptance_id} 缺少有效的 {keyword}", heading_line + 1))

        if acceptance_id == "AC-UI-001":
            ui_ac_found = True
            if not any(term in body for term in ("Figma", "設計來源")):
                issues.append(Issue("ERROR", "AC-UI-001 必須驗證 Figma 或設計來源", heading_line + 1))
        else:
            normal_ac_count += 1

    if normal_ac_count == 0:
        issues.append(Issue("ERROR", "至少需要一項一般 AC"))
    if not ui_ac_found:
        issues.append(Issue("ERROR", "缺少 AC-UI-001 前端畫面對齊驗收"))


def check_error_table(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    table = find_table(lines, section.start, section.end, ("錯誤情境", "建議業務碼", "觸發條件"))
    check_table_has_data(table, "業務錯誤情境與錯誤碼需求", issues, section.start + 1)


def check_api_table(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    table = find_table(lines, section.start, section.end, ("UI 區塊/動作", "業務能力", "API ID", "候選路徑"))
    if table is None:
        issues.append(Issue("ERROR", "缺少表格：UI → API 對照表", section.start + 1))
        return
    data_rows = [row for row in table.rows if any(not is_empty_value(cell) for cell in row)]
    if not data_rows:
        issues.append(Issue("ERROR", "UI → API 對照表不可沒有資料", table.line_number))
        return

    required_columns = ("UI 區塊/動作", "業務能力", "API ID", "候選路徑", "輸入摘要", "成功結果", "主要錯誤情境")
    api_ids: set[str] = set()
    for row_index, row in enumerate(data_rows, start=1):
        for column in required_columns:
            value = cell_value(table, row, column)
            if is_empty_value(value):
                issues.append(Issue("ERROR", f"UI → API 第 {row_index} 列欄位不可為空：{column}", table.line_number))
        api_id = cell_value(table, row, "API ID")
        if api_id and not API_ID_PATTERN.fullmatch(api_id):
            issues.append(Issue("ERROR", f"API ID 格式不正確：{api_id}", table.line_number))
        if api_id in api_ids:
            issues.append(Issue("ERROR", f"API ID 不可重複：{api_id}", table.line_number))
        if api_id:
            api_ids.add(api_id)


def check_risk_table(lines: Sequence[str], section: Section, issues: list[Issue]) -> None:
    table = find_table(lines, section.start, section.end, ("Item ID", "類型", "問題／假設", "影響", "狀態"))
    if table is None:
        issues.append(Issue("ERROR", "缺少表格：風險、假設與待確認事項", section.start + 1))
        return
    data_rows = [row for row in table.rows if any(not is_empty_value(cell) for cell in row)]
    if not data_rows:
        issues.append(Issue("ERROR", "風險、假設與待確認事項不可沒有資料", table.line_number))
        return
    for row in data_rows:
        if is_empty_value(cell_value(table, row, "問題／假設")):
            issues.append(Issue("ERROR", "風險表的問題／假設不可為空", table.line_number))


def check_duplicate_ids(lines: Sequence[str], issues: list[Issue]) -> None:
    occurrences: dict[str, list[int]] = {}
    for line_number, line in enumerate(lines, start=1):
        heading_match = H3_PATTERN.match(line)
        if heading_match:
            identifier = heading_match.group(1).split()[0]
            if any(pattern.fullmatch(identifier) for pattern in ID_PATTERNS):
                occurrences.setdefault(identifier, []).append(line_number)
    for identifier, line_numbers in occurrences.items():
        if len(line_numbers) > 1:
            issues.append(Issue("ERROR", f"識別碼不可重複：{identifier}", line_numbers[1]))


def check_placeholders(lines: Sequence[str], issues: list[Issue]) -> None:
    for line_number, line in enumerate(lines, start=1):
        if has_placeholder(line):
            issues.append(Issue("ERROR", "文件仍包含未替換的 placeholder", line_number))


def validate_text(text: str) -> list[Issue]:
    lines = text.splitlines()
    issues: list[Issue] = []
    sections, section_issues = section_map(lines)
    issues.extend(section_issues)
    check_required_sections(sections, issues)
    check_metadata(lines, issues)
    check_placeholders(lines, issues)
    check_duplicate_ids(lines, issues)

    if "1) 需求摘要" in sections:
        check_summary(lines, sections["1) 需求摘要"], issues)
    if "2) Functional Requirements (FR)" in sections:
        check_fr(lines, sections["2) Functional Requirements (FR)"], issues)
    if "3) Non-Functional Requirements (NFR)" in sections:
        check_nfr(lines, sections["3) Non-Functional Requirements (NFR)"], issues)
    if "4) Acceptance Criteria (AC, Given/When/Then)" in sections:
        check_ac(lines, sections["4) Acceptance Criteria (AC, Given/When/Then)"], issues)
    if "業務錯誤情境與錯誤碼需求" in sections:
        check_error_table(lines, sections["業務錯誤情境與錯誤碼需求"], issues)
    if "UI → API 對照表" in sections:
        check_api_table(lines, sections["UI → API 對照表"], issues)
    if "5) 風險、假設與待確認事項" in sections:
        check_risk_table(lines, sections["5) 風險、假設與待確認事項"], issues)
    return issues


def validate_file(path: Path) -> list[Issue]:
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as error:
        return [Issue("ERROR", f"無法讀取文件：{error}")]
    except UnicodeDecodeError:
        return [Issue("ERROR", "文件不是有效的 UTF-8")]
    return validate_text(text)


def report(path: Path, issues: Sequence[Issue]) -> tuple[int, int]:
    error_count = 0
    warning_count = 0
    for issue in issues:
        location = f":{issue.line_number}" if issue.line_number else ""
        print(f"[{issue.severity}] {path}{location} {issue.message}")
        if issue.severity == "ERROR":
            error_count += 1
        else:
            warning_count += 1
    if not issues:
        print(f"[PASS] {path}")
    return error_count, warning_count


def parse_arguments(arguments: Optional[Sequence[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Validate SA scenario requirements documents."
    )
    parser.add_argument("paths", nargs="+", type=Path, help="REQ markdown file(s) to validate")
    parser.add_argument(
        "--warnings-as-errors",
        action="store_true",
        help="Treat warnings as validation errors.",
    )
    return parser.parse_args(arguments)


def main(arguments: Optional[Sequence[str]] = None) -> int:
    parsed = parse_arguments(arguments)
    total_errors = 0
    total_warnings = 0
    for path in parsed.paths:
        errors, warnings = report(path, validate_file(path))
        total_errors += errors
        total_warnings += warnings

    print(
        f"Summary: {len(parsed.paths)} file(s), "
        f"{total_errors} error(s), {total_warnings} warning(s)"
    )
    if total_errors or (parsed.warnings_as_errors and total_warnings):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
