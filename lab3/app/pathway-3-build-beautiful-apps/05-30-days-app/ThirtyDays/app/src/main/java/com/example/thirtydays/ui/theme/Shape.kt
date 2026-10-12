package com.example.thirtydays.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Day badge: two cut corners make it look like a tag
    extraSmall = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp),
    // Tip image
    small = RoundedCornerShape(12.dp),
    // Tip card
    medium = RoundedCornerShape(20.dp)
)
