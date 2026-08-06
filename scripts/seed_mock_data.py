#!/usr/bin/env python3
import argparse
import os
import sys
from pathlib import Path

import mysql.connector
from mysql.connector import Error


DEFAULT_MARKER_KEY = "mock_seed_version"
DEFAULT_MARKER_VALUE = "v4"
DEFAULT_GOOGLE_SUBJECT = "legacy-single-user"


def parse_args():
    parser = argparse.ArgumentParser(
        description="Seed mock portfolio data into MySQL without rerunning Flyway migrations."
    )
    parser.add_argument("--host", default=os.getenv("DB_HOST", "localhost"))
    parser.add_argument("--port", type=int, default=int(os.getenv("DB_PORT", "3306")))
    parser.add_argument("--database", default=os.getenv("DB_NAME", "portfolio_manager"))
    parser.add_argument("--user", default=os.getenv("DB_USERNAME", "portfolio_user"))
    parser.add_argument("--password", default=os.getenv("DB_PASSWORD", "portfolio_pass"))
    parser.add_argument("--subject", default=DEFAULT_GOOGLE_SUBJECT)
    parser.add_argument("--force", action="store_true", help="Run seed even if marker exists")
    return parser.parse_args()


def migration_sql_path():
    repo_root = Path(__file__).resolve().parents[1]
    return repo_root / "src" / "main" / "resources" / "db" / "migration" / "V4__seed_demo_portfolio_data.sql"


def load_seed_sql() -> str:
    sql_file = migration_sql_path()
    if not sql_file.exists():
        raise FileNotFoundError(f"Seed SQL file not found: {sql_file}")
    return sql_file.read_text(encoding="utf-8")


def get_user_id(cursor, subject: str):
    cursor.execute(
        "SELECT id FROM app_users WHERE google_subject = %s LIMIT 1",
        (subject,),
    )
    row = cursor.fetchone()
    return row[0] if row else None


def marker_exists(cursor, user_id: int) -> bool:
    cursor.execute(
        """
        SELECT 1
        FROM app_settings
        WHERE user_id = %s
          AND setting_key = %s
          AND setting_value = %s
        LIMIT 1
        """,
        (user_id, DEFAULT_MARKER_KEY, DEFAULT_MARKER_VALUE),
    )
    return cursor.fetchone() is not None


def set_marker(cursor, user_id: int):
    cursor.execute(
        """
        INSERT INTO app_settings (user_id, setting_key, setting_value)
        VALUES (%s, %s, %s)
        ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
        """,
        (user_id, DEFAULT_MARKER_KEY, DEFAULT_MARKER_VALUE),
    )


def run_seed(cursor, sql_text: str):
    for _ in cursor.execute(sql_text, multi=True):
        pass


def main():
    args = parse_args()
    sql_text = load_seed_sql()

    try:
        conn = mysql.connector.connect(
            host=args.host,
            port=args.port,
            database=args.database,
            user=args.user,
            password=args.password,
        )
        conn.autocommit = False
        cursor = conn.cursor()

        user_id = get_user_id(cursor, args.subject)
        if user_id and marker_exists(cursor, user_id) and not args.force:
            print(
                "Mock data already seeded for this user. "
                "Use --force to seed again."
            )
            conn.rollback()
            return 0

        run_seed(cursor, sql_text)

        user_id = get_user_id(cursor, args.subject)
        if not user_id:
            raise RuntimeError("Seed user was not created. Aborting.")

        set_marker(cursor, user_id)
        conn.commit()
        print("Mock data seed completed successfully.")
        return 0

    except (Error, FileNotFoundError, RuntimeError) as exc:
        print(f"Seeding failed: {exc}", file=sys.stderr)
        return 1
    finally:
        try:
            cursor.close()
            conn.close()
        except Exception:
            pass


if __name__ == "__main__":
    raise SystemExit(main())
