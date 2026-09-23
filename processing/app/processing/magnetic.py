import numpy as np
import pandas as pd


class MagneticProcessor:

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
                f"Magnetic column '{magnetic_column}' was not found."
            )

        # Convert selected magnetic field to numeric.
        magnetic = pd.to_numeric(
            df[magnetic_column],
            errors="coerce"
        )

        # Preserve the selected magnetic input.
        df["tmi_input"] = magnetic

        # Preserve raw magnetic field when available.
        if "rawt" in df.columns:
            df["raw_magnetic"] = pd.to_numeric(
                df["rawt"],
                errors="coerce"
            )

        # Preserve FiTT when available.
        if "fitt" in df.columns:
            df["final_tuned_tmi"] = pd.to_numeric(
                df["fitt"],
                errors="coerce"
            )

        # Survey median.
        median = magnetic.median()

        df["tmi_median"] = median

        # Survey-relative magnetic residual.
        df["magnetic_residual"] = (
            magnetic - median
        )

        # Absolute deviation.
        df["magnetic_absolute_deviation"] = (
            magnetic - median
        ).abs()

        # MAD.
        valid_magnetic = magnetic.dropna()

        if len(valid_magnetic) > 0:

            mad = np.median(
                np.abs(
                    valid_magnetic - median
                )
            )

        else:

            mad = np.nan

        df["magnetic_mad"] = mad

        # Robust Z-score.
        if pd.isna(mad) or mad == 0:

            df["magnetic_robust_z"] = np.nan
            df["magnetic_outlier"] = False

        else:

            robust_z = (
                0.6745
                * (magnetic - median)
                / mad
            )

            df["magnetic_robust_z"] = robust_z

            # This does NOT remove geological anomalies.
            df["magnetic_outlier"] = (
                robust_z.abs() > 3.5
            )

        # Keep the original selected magnetic measurement.
        df["processed_magnetic"] = magnetic

        # Record provenance.
        df["magnetic_source_column"] = (
            magnetic_column
        )

        # Record available correction-related fields.
        correction_candidates = [
            "CorT",
            "ComT",
            "CoLT",
            "BasT",
            "TieT",
            "HeaT",
            "LevT",
            "FinT",
            "FiTT",
            "cort",
            "comt",
            "colt",
            "bast",
            "tiet",
            "heat",
            "levt",
            "fint",
            "fitt"
        ]

        available_corrections = []

        for column in correction_candidates:

            if column not in df.columns:
                continue

            values = pd.to_numeric(
                df[column],
                errors="coerce"
            )

            if values.notna().any():
                available_corrections.append(
                    column
                )

        # Remove duplicate names while preserving order.
        available_corrections = list(
            dict.fromkeys(
                available_corrections
            )
        )

        df["available_magnetic_corrections"] = (
            ",".join(
                available_corrections
            )
        )

        return df
