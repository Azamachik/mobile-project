package com.example.thirtydays.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * A data class to represent the tip for one day of the month
 */
data class Tip(
    val day: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val imageRes: Int
)
