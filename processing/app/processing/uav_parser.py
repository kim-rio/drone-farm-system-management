from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import pandas as pd
import re


@dataclass
class UavData:
    dataframe: pd.DataFrame
    columns: list[str]
    latitude_column: Optional[str]
    longitude_column: Optional[str]
    magnetic_column: Optional[str]


class UavParser:

    def parse(self, file_path: str) -> UavData:

        path = Path(file_path)

        if not path.exists():
            raise FileNotFoundError(
                f"UAV file not found: {file_path}"
            )

        lines = path.read_text(
            encoding="utf-8",
            errors="replace"
        ).splitlines()

        if not lines:
            raise ValueError(
                "UAV file is empty."
            )

        header_index = self._find_measurement_header(lines)

        if header_index is None:
            raise ValueError(
                "Could not find the UAV measurement header."
            )

        columns = self._parse_columns(
            lines[header_index]
        )

        if len(columns) < 10:
            raise ValueError(
                "UAV measurement header does not contain enough columns."
            )

        data_start = header_index + 1

        dataframe = self._read_measurements(
            lines,
            data_start,
            columns
        )

        if dataframe.empty:
            raise ValueError(
                "No UAV measurement records were found."
            )

        dataframe = self._normalise_column_names(
            dataframe
        )

        latitude_column = self._find_exact_column(
            dataframe,
            "lat"
        )

        longitude_column = self._find_exact_column(
            dataframe,
            "lon"
        )

                # RawT is the raw total magnetic field measurement
        # in the Laanila UAV data.
        #
        # We deliberately use RawT here rather than guessing
        # that MagB is the magnetic measurement.
        magnetic_column = self._find_exact_column(
            dataframe,
            "rawt"
        )
        magnetic_column = self._find_exact_column(
            dataframe,
            "rawt"
        )

        return UavData(
            dataframe=dataframe,
            columns=list(dataframe.columns),
            latitude_column=latitude_column,
            longitude_column=longitude_column,
            magnetic_column=magnetic_column
        )

    def _find_measurement_header(
        self,
        lines: list[str]
    ) -> Optional[int]:

        for index, line in enumerate(lines):

            stripped = line.strip()

            if not stripped:
                continue

            tokens = re.split(
                r"\s+",
                stripped
            )

            normalized = [
                token.strip(
                    "\"'"
                ).lower()
                for token in tokens
            ]

            required = {
                "time",
                "lon",
                "lat",
                "rawt"
            }

            if required.issubset(
                set(normalized)
            ):
                return index

        return None

    def _parse_columns(
        self,
        header_line: str
    ) -> list[str]:

        columns = re.split(
            r"\s+",
            header_line.strip()
        )

        columns = [
            column.strip(
                "\"'"
            )
            for column in columns
            if column.strip()
        ]

        return columns

    def _read_measurements(
        self,
        lines: list[str],
        data_start: int,
        columns: list[str]
    ) -> pd.DataFrame:

        rows = []

        for line in lines[data_start:]:

            line = line.strip()

            if not line:
                continue

            values = re.split(
                r"\s+",
                line
            )

            if len(values) < len(columns):
                continue

            if not self._is_measurement_row(
                values
            ):
                continue

            values = values[
                :len(columns)
            ]

            rows.append(values)

        if not rows:
            return pd.DataFrame(
                columns=columns
            )

        return pd.DataFrame(
            rows,
            columns=columns
        )

    def _is_measurement_row(
        self,
        values: list[str]
    ) -> bool:

        if len(values) < 10:
            return False

        numeric_count = 0

        for value in values:

            if self._is_numeric(value):
                numeric_count += 1

        return numeric_count >= 10

    def _is_numeric(
        self,
        value: str
    ) -> bool:

        try:

            float(
                value.replace(",", ".")
            )

            return True

        except (
            ValueError,
            AttributeError
        ):

            return False

    def _normalise_column_names(
        self,
        dataframe: pd.DataFrame
    ) -> pd.DataFrame:

        dataframe.columns = [
            self._normalise_name(
                column
            )
            for column in dataframe.columns
        ]

        return dataframe

    def _normalise_name(
        self,
        name: str
    ) -> str:

        name = str(
            name
        ).strip().lower()

        name = re.sub(
            r"[^a-z0-9_]+",
            "_",
            name
        )

        return name.strip("_")

    def _find_exact_column(
        self,
        dataframe: pd.DataFrame,
        column_name: str
    ) -> Optional[str]:

        column_name = column_name.lower()

        for column in dataframe.columns:

            if column.lower() == column_name:
                return column

        return None