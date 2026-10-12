package com.example.thirtydays

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.thirtydays.model.TipsRepository
import com.example.thirtydays.ui.theme.ThirtyDaysTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ThirtyDaysTheme {
                ThirtyDaysApp()
            }
        }
    }
}

/**
 * Composable that displays an app bar and a list of tips.
 */
@Composable
fun ThirtyDaysApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ThirtyDaysTopAppBar()
        }
    ) { innerPadding ->
        TipList(
            tips = TipsRepository.tips,
            contentPadding = innerPadding
        )
    }
}

/**
 * Composable that displays a top bar with the name of the app.
 *
 * @param modifier modifiers to set to this composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThirtyDaysTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge
            )
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun ThirtyDaysPreview() {
    ThirtyDaysTheme(darkTheme = false) {
        ThirtyDaysApp()
    }
}

// uiMode switches the night colors of the tip images too, not only the theme
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ThirtyDaysDarkThemePreview() {
    ThirtyDaysTheme(darkTheme = true) {
        ThirtyDaysApp()
    }
}
