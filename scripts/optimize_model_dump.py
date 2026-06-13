#!/usr/bin/env python3
"""
Converte INSERTs individuais de word_frequencies para COPY (formato nativo Postgres).
Preserva dados literais (aspas, ponto-e-vírgula, vírgulas dentro de strings).
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

INSERT_PREFIX = "INSERT INTO public.word_frequencies VALUES "
ROW_PATTERN = re.compile(
    r"INSERT INTO public\.word_frequencies VALUES \((\d+), '((?:[^']|'')*)', (\d+), (\d+)\);?\s*$"
)


def unescape_sql_string(value: str) -> str:
    return value.replace("''", "'")


def copy_text_escape(value: str) -> str:
    """Escapa campo no formato COPY TEXT (tab-delimited) do PostgreSQL."""
    escaped: list[str] = []
    for char in value:
        if char == "\\":
            escaped.append("\\\\")
        elif char == "\n":
            escaped.append("\\n")
        elif char == "\r":
            escaped.append("\\r")
        elif char == "\t":
            escaped.append("\\t")
        else:
            escaped.append(char)
    return "".join(escaped)


def convert(input_path: Path, output_path: Path) -> int:
    row_count = 0
    in_word_frequencies = False

    with input_path.open("r", encoding="utf-8") as source, output_path.open(
        "w", encoding="utf-8", newline="\n"
    ) as target:
        for line in source:
            if line.startswith(INSERT_PREFIX):
                match = ROW_PATTERN.match(line)
                if match is None:
                    raise ValueError(f"Linha não reconhecida (id ~{row_count + 1}): {line[:120]!r}")

                if not in_word_frequencies:
                    in_word_frequencies = True
                    target.write(
                        "COPY public.word_frequencies (id, word, spam_count, ham_count) FROM stdin;\n"
                    )

                row_id, word, spam_count, ham_count = match.groups()
                word = copy_text_escape(unescape_sql_string(word))
                target.write(f"{row_id}\t{word}\t{spam_count}\t{ham_count}\n")
                row_count += 1
                continue

            if in_word_frequencies:
                in_word_frequencies = False
                target.write("\\.\n\n")

            target.write(line)

        if in_word_frequencies:
            target.write("\\.\n\n")

    return row_count


def main() -> None:
    parser = argparse.ArgumentParser(description="Otimiza dump SQL com COPY para word_frequencies.")
    parser.add_argument(
        "--input",
        type=Path,
        default=Path("modelo_treinado.sql"),
        help="Dump original com INSERTs individuais",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("modelo_treinado2.sql"),
        help="Dump otimizado de saída",
    )
    args = parser.parse_args()

    if not args.input.is_file():
        print(f"Arquivo não encontrado: {args.input}", file=sys.stderr)
        sys.exit(1)

    print(f"Convertendo {args.input} -> {args.output} ...")
    rows = convert(args.input, args.output)
    size_mb = args.output.stat().st_size / (1024 * 1024)
    print(f"Concluído: {rows:,} linhas em word_frequencies ({size_mb:.1f} MB)")


if __name__ == "__main__":
    main()
