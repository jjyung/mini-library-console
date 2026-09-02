#!/usr/bin/env python3
"""Verify requirement-to-test traceability and run the declared FE tests.

This helper is owned by the fe-development skill. It intentionally reads
requirement, manifest, and test files from the target repository without being
copied into that repository.
"""

from __future__ import annotations

import argparse
import json
import re
import shlex
import subprocess
import sys
from pathlib import Path
from typing import Any


ALLOWED_TEST_LEVELS = frozenset({"unit", "component", "integration"})
CRITERION_HEADING_PATTERN = re.compile(
    r"^\s{0,3}#{2,6}\s+((?:FR|AC)-[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)\b"
)
REQUIREMENT_ID_PATTERN = re.compile(
    r"(?:需求文件\s*ID|Requirement\s*ID)\s*[:：]\s*"
    r"([A-Za-z0-9]+(?:-[A-Za-z0-9]+)+)",
    re.IGNORECASE,
)


def build_argument_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description=(
            "Verify a canonical requirement against a FE traceability manifest "
            "and run the declared test commands."
        )
    )
    parser.add_argument(
        "--requirement",
        required=True,
        type=Path,
        help="Path to the canonical Markdown requirement document.",
    )
    parser.add_argument(
        "--matrix",
        required=True,
        type=Path,
        help="Path to the JSON traceability manifest.",
    )
    parser.add_argument(
        "--project-root",
        default=Path("."),
        type=Path,
        help="Project root used to resolve test files and run commands.",
    )
    parser.add_argument(
        "--test-command",
        action="append",
        required=True,
        help=(
            "Test command to execute from the project root. Repeat this option "
            "for unit, component, or integration commands."
        ),
    )
    parser.add_argument(
        "--coverage-file",
        required=True,
        type=Path,
        help="Coverage artifact produced by the test command; it must exist after execution.",
    )
    return parser


def read_text_file(file_path: Path, description: str) -> str:
    try:
        return file_path.read_text(encoding="utf-8")
    except FileNotFoundError as error:
        raise ValueError(f"{description} does not exist: {file_path}") from error
    except OSError as error:
        raise ValueError(f"Cannot read {description} {file_path}: {error}") from error


def load_matrix(matrix_path: Path) -> dict[str, Any]:
    matrix_source = read_text_file(matrix_path, "Matrix file")
    try:
        matrix_data = json.loads(matrix_source)
    except json.JSONDecodeError as error:
        raise ValueError(f"Matrix is not valid JSON: {error}") from error

    if not isinstance(matrix_data, dict):
        raise ValueError("Matrix root must be a JSON object.")
    return matrix_data


def load_requirement(requirement_path: Path) -> tuple[str, set[str]]:
    requirement_source = read_text_file(requirement_path, "Requirement file")
    requirement_id: str | None = None
    criterion_ids: set[str] = set()
    inside_code_fence = False
    for line in requirement_source.splitlines():
        stripped_line = line.lstrip()
        if stripped_line.startswith("```") or stripped_line.startswith("~~~"):
            inside_code_fence = not inside_code_fence
            continue
        if inside_code_fence:
            continue
        if requirement_id is None:
            requirement_match = REQUIREMENT_ID_PATTERN.search(line)
            if requirement_match is not None:
                requirement_id = requirement_match.group(1)
        criterion_match = CRITERION_HEADING_PATTERN.match(line)
        if criterion_match is not None:
            criterion_ids.add(criterion_match.group(1))

    if requirement_id is None:
        raise ValueError(
            "Requirement file must declare a requirement ID using "
            "'需求文件 ID: ...' or 'Requirement ID: ...'."
        )
    if not criterion_ids:
        raise ValueError(
            "Requirement file must contain at least one FR-* or AC-* heading."
        )
    return requirement_id, criterion_ids


def require_non_empty_string(value: Any, field_name: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{field_name} must be a non-empty string.")
    return value.strip()


def resolve_project_file(project_root: Path, file_name: str) -> Path:
    root_path = project_root.resolve()
    file_path = (root_path / file_name).resolve()
    try:
        file_path.relative_to(root_path)
    except ValueError as error:
        raise ValueError(f"Test file escapes project root: {file_name}") from error
    return file_path


def test_title_mentions_criterion(test_source: str, criterion_id: str) -> bool:
    escaped_criterion_id = re.escape(criterion_id)
    title_pattern = re.compile(
        rf"\b(?:describe|it|test)\s*\(\s*['\"`][^'\"`]*"
        rf"{escaped_criterion_id}[^'\"`]*['\"`]"
    )
    return title_pattern.search(test_source) is not None


def validate_criteria(
    matrix_data: dict[str, Any],
    project_root: Path,
    requirement_path: Path,
) -> tuple[list[str], str, int]:
    errors: list[str] = []
    requirement_id = require_non_empty_string(
        matrix_data.get("requirementId"), "requirementId"
    )
    canonical_requirement_id, canonical_criteria = load_requirement(requirement_path)
    if requirement_id != canonical_requirement_id:
        errors.append(
            f"requirementId mismatch: matrix has {requirement_id}, "
            f"canonical document has {canonical_requirement_id}"
        )

    criteria = matrix_data.get("criteria")
    if not isinstance(criteria, list) or not criteria:
        raise ValueError("criteria must be a non-empty array.")

    criterion_ids: set[str] = set()
    for criterion_index, criterion in enumerate(criteria, start=1):
        if not isinstance(criterion, dict):
            errors.append(f"criteria[{criterion_index}] must be an object")
            continue

        criterion_id = require_non_empty_string(
            criterion.get("id"), f"criteria[{criterion_index}].id"
        )
        if criterion_id in criterion_ids:
            errors.append(f"{criterion_id}: duplicate criterion id")
            continue
        criterion_ids.add(criterion_id)

        if criterion_id not in canonical_criteria:
            errors.append(
                f"{criterion_id}: criterion is not present in the canonical "
                "requirement document"
            )

        required_levels = criterion.get("requiredLevels", ["unit"])
        if not isinstance(required_levels, list) or not required_levels:
            errors.append(f"{criterion_id}: requiredLevels must be a non-empty array")
            required_levels = []
        invalid_levels = [
            level
            for level in required_levels
            if not isinstance(level, str) or level not in ALLOWED_TEST_LEVELS
        ]
        if invalid_levels:
            errors.append(
                f"{criterion_id}: unsupported required test levels: {invalid_levels}"
            )
        if "unit" not in required_levels:
            errors.append(
                f"{criterion_id}: every requirement criterion must require "
                "at least one unit test"
            )

        test_evidence = criterion.get("tests")
        if not isinstance(test_evidence, list) or not test_evidence:
            errors.append(f"{criterion_id}: tests must be a non-empty array")
            test_evidence = []

        observed_levels: set[str] = set()
        for test_index, test_case in enumerate(test_evidence, start=1):
            if not isinstance(test_case, dict):
                errors.append(
                    f"{criterion_id}: tests[{test_index}] must be an object"
                )
                continue
            test_level = test_case.get("level")
            test_file_name = test_case.get("file")
            if test_level not in ALLOWED_TEST_LEVELS:
                errors.append(
                    f"{criterion_id}: tests[{test_index}].level must be one of "
                    f"{sorted(ALLOWED_TEST_LEVELS)}"
                )
                continue
            try:
                test_file_name = require_non_empty_string(
                    test_file_name,
                    f"{criterion_id}: tests[{test_index}].file",
                )
                test_file_path = resolve_project_file(project_root, test_file_name)
            except ValueError as error:
                errors.append(str(error))
                continue

            if not test_file_path.is_file():
                errors.append(f"{criterion_id}: test file does not exist: {test_file_name}")
                continue
            try:
                test_source = test_file_path.read_text(encoding="utf-8")
            except OSError as error:
                errors.append(f"{criterion_id}: cannot read {test_file_name}: {error}")
                continue

            if not test_title_mentions_criterion(test_source, criterion_id):
                errors.append(
                    f"{criterion_id}: test title must mention the criterion id: "
                    f"{test_file_name}"
                )
                continue
            observed_levels.add(test_level)

        missing_levels = sorted(set(required_levels) - observed_levels)
        if missing_levels:
            errors.append(
                f"{criterion_id}: missing test evidence at level(s): "
                f"{', '.join(missing_levels)}"
            )

    missing_criteria = sorted(canonical_criteria - criterion_ids)
    extra_criteria = sorted(criterion_ids - canonical_criteria)
    if missing_criteria:
        errors.append(
            "Canonical requirement criteria missing from manifest: "
            + ", ".join(missing_criteria)
        )
    if extra_criteria:
        errors.append(
            "Manifest criteria not found in canonical requirement: "
            + ", ".join(extra_criteria)
        )

    if errors:
        errors.insert(0, f"Requirement {requirement_id} verification failed")
    return errors, requirement_id, len(canonical_criteria)


def run_test_commands(test_commands: list[str], project_root: Path) -> list[str]:
    errors: list[str] = []
    resolved_root = project_root.resolve()
    if not resolved_root.is_dir():
        return [f"Project root does not exist or is not a directory: {project_root}"]

    for test_command in test_commands:
        try:
            command_arguments = shlex.split(test_command)
        except ValueError as error:
            errors.append(f"Invalid test command {test_command!r}: {error}")
            continue
        if not command_arguments:
            errors.append("Test command must not be empty.")
            continue

        print(f"RUN: {test_command}")
        try:
            completed_process = subprocess.run(
                command_arguments,
                cwd=resolved_root,
                check=False,
            )
        except OSError as error:
            errors.append(f"Could not execute {test_command!r}: {error}")
            continue
        if completed_process.returncode != 0:
            errors.append(
                f"Test command failed with exit code "
                f"{completed_process.returncode}: {test_command}"
            )
    return errors


def validate_coverage_file(
    coverage_file: Path,
    project_root: Path,
) -> str | None:
    try:
        coverage_path = resolve_project_file(project_root, str(coverage_file))
    except ValueError as error:
        return str(error)
    if not coverage_path.is_file():
        return f"Coverage artifact does not exist: {coverage_file}"
    return None


def main() -> int:
    parser = build_argument_parser()
    arguments = parser.parse_args()
    try:
        matrix_data = load_matrix(arguments.matrix)
        static_errors, requirement_id, criterion_count = validate_criteria(
            matrix_data,
            arguments.project_root,
            arguments.requirement,
        )
    except ValueError as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 1

    if static_errors:
        for error in static_errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    command_errors = run_test_commands(arguments.test_command, arguments.project_root)
    if command_errors:
        for error in command_errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    coverage_error = validate_coverage_file(
        arguments.coverage_file,
        arguments.project_root,
    )
    if coverage_error is not None:
        print(f"ERROR: {coverage_error}", file=sys.stderr)
        return 1

    print(
        f"PASS: {requirement_id} has unit evidence for all "
        f"{criterion_count} canonical requirement criteria."
        f" Executed {len(arguments.test_command)} test command(s)."
        f" Coverage artifact exists; test-runner coverage policy must be 80%."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
