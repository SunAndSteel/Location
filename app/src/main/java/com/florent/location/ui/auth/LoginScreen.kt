package com.florent.location.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.florent.location.R
import com.florent.location.ui.components.ScreenScaffold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoggedIn: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoggedIn()
        }
    }
    ScreenScaffold(
        title = "",
        contentMaxWidth = 1200.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            val isLargeScreen = maxWidth > 840.dp

            if (isLargeScreen) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoginHeader()
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoginCard(
                            state = state,
                            onEvent = viewModel::onEvent
                        )                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    LoginHeader()

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        LoginCard(
                            state = state,
                            onEvent = viewModel::onEvent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.widthIn(max = 460.dp)
    ) {

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.size(120.dp)
        ) {
            AsyncImage(
                model = R.drawable.logov3,
                contentDescription = "Logo",
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            )
        }

        Text(
            text = "Bienvenue",
            style = MaterialTheme.typography.displaySmall
        )

        Text(
            text = "Gérez vos logements et vos baux",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LoginHighlights()
    }
}

@Composable
private fun LoginHighlights(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoginHighlightItem(
            icon = Icons.Outlined.AutoAwesome,
            text = "Vue claire de vos logements, locataires et baux"
        )
        LoginHighlightItem(
            icon = Icons.Outlined.Bolt,
            text = "Actions rapides pour gagner du temps au quotidien"
        )
        LoginHighlightItem(
            icon = Icons.Outlined.VerifiedUser,
            text = "Données centralisées pour un suivi plus serein"
        )
    }
}

@Composable
private fun LoginHighlightItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginCard(
    state: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit
) {
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 3.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .onPreviewKeyEvent {
                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                        onEvent(LoginUiEvent.Submit)
                        true
                    } else false
                },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Connectez-vous pour retrouver votre espace en quelques secondes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(LoginUiEvent.EmailChanged(it)) },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocus.requestFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus)
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { onEvent(LoginUiEvent.PasswordChanged(it)) },
                label = { Text("Mot de passe") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            null
                        )
                    }
                },
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEvent(LoginUiEvent.Submit)
                    }
                ),
                singleLine = true,
                isError = state.errorMessage != null,
                supportingText = {
                    state.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocus)
            )

            Button(
                onClick = {
                    onEvent(LoginUiEvent.Submit)
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Se connecter")
                }
            }

            Text(
                text = "Astuce : appuyez sur Entrée pour valider plus vite.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
