import pandas as pd


class SurveyQualityChecker:

    def check(
        self,
        dataframe: pd.DataFrame,
        latitude_column: str | None,
        longitude_column: str | None,
        magnetic_column: str | None
    ) -> list[str]:

        warnings = []

        if dataframe.empty:
            warnings.append(
                "Survey contains no measurement records."
            )
            return warnings

        if latitude_column is None:
            warnings.append(
                "Latitude column could not be identified."
            )

        if longitude_column is None:
            warnings.append(
                "Longitude column could not be identified."
            )

        if magnetic_column is None:
            warnings.append(
                "Magnetic measurement column could not be identified."
            )

        if latitude_column:

            latitude = pd.to_numeric(
                dataframe[latitude_column],
                errors="coerce"
            )

            invalid = (
                latitude.isna()
                | (latitude < -90)
                | (latitude > 90)
            )

            if invalid.any():

                warnings.append(
                    f"{invalid.sum()} records have invalid latitude values."
                )

        if longitude_column:

            longitude = pd.to_numeric(
                dataframe[longitude_column],
                errors="coerce"
            )

            invalid = (
                longitude.isna()
                | (longitude < -180)
                | (longitude > 180)
            )

            if invalid.any():

                warnings.append(
                    f"{invalid.sum()} records have invalid longitude values."
                )

        return warnings