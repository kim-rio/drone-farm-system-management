import pandas as pd


class SurveyCleaner:

    def clean(
        self,
        dataframe: pd.DataFrame,
        latitude_column: str | None,
        longitude_column: str | None,
        magnetic_column: str | None
    ) -> pd.DataFrame:

        df = dataframe.copy()

        if latitude_column:

            df[latitude_column] = pd.to_numeric(
                df[latitude_column],
                errors="coerce"
            )

        if longitude_column:

            df[longitude_column] = pd.to_numeric(
                df[longitude_column],
                errors="coerce"
            )

        if magnetic_column:

            df[magnetic_column] = pd.to_numeric(
                df[magnetic_column],
                errors="coerce"
            )

        required_columns = []

        if latitude_column:
            required_columns.append(
                latitude_column
            )

        if longitude_column:
            required_columns.append(
                longitude_column
            )

        if magnetic_column:
            required_columns.append(
                magnetic_column
            )

        if required_columns:

            df = df.dropna(
                subset=required_columns
            )

        if latitude_column:

            df = df[
                (df[latitude_column] >= -90)
                &
                (df[latitude_column] <= 90)
            ]

        if longitude_column:

            df = df[
                (df[longitude_column] >= -180)
                &
                (df[longitude_column] <= 180)
            ]

        return df.reset_index(
            drop=True
        )