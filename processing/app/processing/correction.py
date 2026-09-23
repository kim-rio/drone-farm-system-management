import pandas as pd


class MagneticCorrectionProcessor:

    # Laanila reference identifies FiTT as the
    # final corrected and tuned TMI field.
    PREFERRED_MAGNETIC_COLUMNS = [
        "FiTT",
        "fitt",
        "FinT",
        "fint",
        "LevT",
        "levt",
        "TieT",
        "tiet",
        "BasT",
        "bast",
        "ComT",
        "comt",
        "CoLT",
        "colt",
        "CorT",
        "cort",
        "rawt",
    ]

    def select_magnetic_column(
        self,
        dataframe: pd.DataFrame,
        detected_column: str | None = None
    ) -> str | None:

        # Prefer the final corrected/tuned TMI.
        for column in self.PREFERRED_MAGNETIC_COLUMNS:

            if column not in dataframe.columns:
                continue

            values = pd.to_numeric(
                dataframe[column],
                errors="coerce"
            )

            if values.notna().any():
                return column

        # Fall back to whatever the parser detected.
        if detected_column:

            # Exact match.
            if detected_column in dataframe.columns:

                values = pd.to_numeric(
                    dataframe[detected_column],
                    errors="coerce"
                )

                if values.notna().any():
                    return detected_column

            # Case-insensitive match.
            detected_lower = (
                detected_column.lower()
            )

            for column in dataframe.columns:

                if column.lower() != detected_lower:
                    continue

                values = pd.to_numeric(
                    dataframe[column],
                    errors="coerce"
                )

                if values.notna().any():
                    return column

        return None

    def process(
        self,
        dataframe: pd.DataFrame,
        magnetic_column: str | None
    ) -> pd.DataFrame:

        df = dataframe.copy()

        if magnetic_column is None:
            return df

        if magnetic_column not in df.columns:
            raise ValueError(
                f"Magnetic column '{magnetic_column}' "
                f"was not found."
            )

        magnetic = pd.to_numeric(
            df[magnetic_column],
            errors="coerce"
        )

        # This is a survey-relative reference.
        # It is NOT claimed as a final geophysical correction.
        reference = magnetic.median()

        df["magnetic_reference"] = reference

        df["corrected_magnetic"] = (
            magnetic - reference
        )

        # Explicit semantic name so downstream code
        # knows this is survey-relative.
        df["relative_magnetic_anomaly"] = (
            magnetic - reference
        )

        return df