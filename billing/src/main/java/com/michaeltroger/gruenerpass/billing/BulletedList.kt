package com.michaeltroger.gruenerpass.billing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
internal fun BulletedList(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        items.forEach { text ->
            Row(Modifier.padding(vertical = AppPadding.xxs)) {
                Text("•", Modifier.padding(end = AppPadding.s))
                Text(
                    text = text,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
