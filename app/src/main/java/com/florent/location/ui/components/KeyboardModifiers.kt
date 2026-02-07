package com.florent.location.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics

fun Modifier.keyboardClickable(onClick: () -> Unit): Modifier {
    return this
        .focusable()
        .semantics { role = Role.Button }
        .onKeyEvent {
            if (it.type == KeyEventType.KeyUp &&
                (it.key == Key.Enter || it.key == Key.NumPadEnter || it.key == Key.Spacebar)
            ) {
                onClick()
                true
            } else {
                false
            }
        }
        .clickable(onClick = onClick)
}
