package com.michaeltroger.gruenerpass.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.michaeltroger.gruenerpass.BuildConfig
import com.michaeltroger.gruenerpass.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Suppress("FunctionNaming")
@Composable
fun FooterComposable() {
    val year = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }

    Column(
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 24.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("© $year Michael Troger",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(stringResource(R.string.more_screen_footer_developed_in_eu),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stringResource(
                R.string.version,
                "${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR} (${BuildConfig.VERSION_CODE})"
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Suppress("FunctionNaming")
@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        FooterComposable()
    }
}
