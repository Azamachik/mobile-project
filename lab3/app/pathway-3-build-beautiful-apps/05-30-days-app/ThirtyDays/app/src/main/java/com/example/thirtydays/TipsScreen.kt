package com.example.thirtydays

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thirtydays.model.Tip
import com.example.thirtydays.model.TipsRepository
import com.example.thirtydays.ui.theme.ThirtyDaysTheme

/**
 * Composable that displays a scrollable list of tips.
 *
 * @param tips the tips to display, one card for each
 * @param modifier modifiers to set to this composable
 * @param contentPadding padding around the whole list, for example the padding of a Scaffold
 */
@Composable
fun TipList(
    tips: List<Tip>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(tips, key = { it.day }) { tip ->
            TipCard(
                tip = tip,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.padding_medium),
                    vertical = dimensionResource(R.dimen.padding_small)
                )
            )
        }
    }
}

/**
 * Composable that displays a card with the day, the title and the image of a tip.
 * The description appears when the card is expanded.
 *
 * @param tip contains the data that populates the card
 * @param modifier modifiers to set to this composable
 */
@Composable
fun TipCard(
    tip: Tip,
    modifier: Modifier = Modifier
) {
    // rememberSaveable keeps the card expanded when it scrolls off the screen and back
    var expanded by rememberSaveable { mutableStateOf(false) }
    val color by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "tipCardColor"
    )
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DayBadge(tip.day)
                    Text(
                        text = stringResource(tip.titleRes),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
                    )
                }
                TipCardButton(
                    expanded = expanded,
                    day = tip.day,
                    onClick = { expanded = !expanded }
                )
            }
            TipImage(
                tipImage = tip.imageRes,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_medium))
            )
            if (expanded) {
                Text(
                    text = stringResource(tip.descriptionRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_medium))
                )
            }
        }
    }
}

/**
 * Composable that displays the number of the day as a small tag.
 *
 * @param day the day of the month, from 1 to 30
 * @param modifier modifiers to set to this composable
 */
@Composable
fun DayBadge(
    day: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.day_label, day),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(
                horizontal = dimensionResource(R.dimen.badge_padding_horizontal),
                vertical = dimensionResource(R.dimen.badge_padding_vertical)
            )
    )
}

/**
 * Composable that displays a button with an arrow. The arrow turns up when the card is expanded.
 *
 * @param expanded represents whether the description of the tip is visible
 * @param day the day of the tip, used to tell the buttons apart for screen readers
 * @param onClick is the action that happens when the button is clicked
 * @param modifier modifiers to set to this composable
 */
@Composable
private fun TipCardButton(
    expanded: Boolean,
    day: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "tipCardButtonRotation"
    )
    // IconButton makes the touch target at least 48dp x 48dp
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = stringResource(
                if (expanded) R.string.collapse_button_content_description
                else R.string.expand_button_content_description,
                day
            ),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.rotate(rotation)
        )
    }
}

/**
 * Composable that displays the image of a tip.
 *
 * @param tipImage is the resource ID for the image of the tip
 * @param modifier modifiers to set to this composable
 */
@Composable
fun TipImage(
    @DrawableRes tipImage: Int,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Crop,
        painter = painterResource(tipImage),

        // Content Description is not needed here - the image repeats what the title says, and
        // setting a null content description allows accessibility services to skip this element
        // during navigation.

        contentDescription = null
    )
}

@Preview
@Composable
fun TipCardPreview() {
    ThirtyDaysTheme {
        TipCard(tip = TipsRepository.tips[0])
    }
}
