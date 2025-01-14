package com.prof18.moneyflow.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prof18.moneyflow.ComposeNavigationFactory
import com.prof18.moneyflow.R
import com.prof18.moneyflow.Screen
import com.prof18.moneyflow.ui.components.SwitchWithText
import com.prof18.moneyflow.ui.style.AppMargins
import com.prof18.moneyflow.ui.style.MoneyFlowTheme
import org.koin.androidx.compose.getViewModel
import timber.log.Timber

object SettingsScreenFactory : ComposeNavigationFactory {
    override fun create(navGraphBuilder: NavGraphBuilder, navController: NavController) {
        navGraphBuilder.composable(Screen.SettingsScreen.route) {
            val viewModel = getViewModel<SettingsViewModel>()
            val hideDataState by viewModel.hideSensitiveDataState.collectAsState()

            SettingsScreen(
                performBackup = { uri -> viewModel.performBackup(uri) },
                performRestore = { uri -> viewModel.performRestore(uri) },
                biometricState = viewModel.biometricState,
                onBiometricEnabled = { viewModel.updateBiometricState(it) },
                hideSensitiveDataState= hideDataState,
                onHideSensitiveDataEnabled= { viewModel.updateHideSensitiveDataState(it) }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    performBackup: (Uri) -> Unit,
    performRestore: (Uri) -> Unit,
    biometricState: Boolean,
    onBiometricEnabled: (Boolean) -> Unit,
    hideSensitiveDataState: Boolean,
    onHideSensitiveDataEnabled: (Boolean) -> Unit,
) {

    val context = LocalContext.current

    val createFileURI = remember { mutableStateOf<Uri?>(null) }
    val createFileAction =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) {
            createFileURI.value = it
        }
    createFileURI.value?.let { uri ->
        performBackup(uri)
        Toast.makeText(context, stringResource(R.string.db_export_completed), Toast.LENGTH_SHORT)
            .show()
    }

    val openFileURI = remember { mutableStateOf<Uri?>(null) }
    val openFileAction = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        openFileURI.value = it
    }
    openFileURI.value?.let { uri ->
        performRestore(uri)
        Toast.makeText(context, stringResource(R.string.db_import_completed), Toast.LENGTH_SHORT)
            .show()
    }

    SettingsScreenContent(
        onImportDatabaseClick = { openFileAction.launch(arrayOf("*/*")) },
        onExportDatabaseClick = { createFileAction.launch("MoneyFlowDB.db") },
        openDropboxSetup = {
            context.startActivity(
                Intent(
                    context,
                    DropboxLoginActivity::class.java
                )
            )
        },
        isBiometricSupported = isBiometricSupported(LocalContext.current),
        biometricState = biometricState,
        onBiometricEnabled = onBiometricEnabled,
        hideSensitiveDataState = hideSensitiveDataState,
        onHideSensitiveDataEnabled = onHideSensitiveDataEnabled,
    )
}

@Composable
private fun SettingsScreenContent(
    onImportDatabaseClick: () -> Unit,
    onExportDatabaseClick: () -> Unit,
    openDropboxSetup: () -> Unit,
    isBiometricSupported: Boolean,
    biometricState: Boolean,
    onBiometricEnabled: (Boolean) -> Unit,
    hideSensitiveDataState: Boolean,
    onHideSensitiveDataEnabled: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            Text(
                text = stringResource(id = R.string.settings_screen),
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .padding(horizontal = AppMargins.regular)
                    .padding(top = AppMargins.regular)
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .padding(top = AppMargins.regular)
            ) {

                Text(
                    text = stringResource(R.string.security),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = AppMargins.regular)
                )

                SwitchWithText(
                    onSwitchChanged = onHideSensitiveDataEnabled,
                    switchStatus = hideSensitiveDataState,
                    title = stringResource(R.string.hide_sensitive_data)
                )

                if (isBiometricSupported) {
                    SwitchWithText(
                        onSwitchChanged = onBiometricEnabled,
                        switchStatus = biometricState,
                        title = stringResource(R.string.biometric_support)
                    )
                }

                Text(
                    text = stringResource(R.string.database_management),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = AppMargins.regular)
                )

                Text(
                    stringResource(R.string.import_database),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onImportDatabaseClick() }
                        .padding(AppMargins.regular)
                )
                Divider()
                Text(
                    stringResource(R.string.export_database),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportDatabaseClick() }
                        .padding(AppMargins.regular)
                )
                Divider()
                Text(
                    stringResource(R.string.setup_dropbox),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDropboxSetup() }
                        .padding(AppMargins.regular)
                )
                Divider()
            }
        }
    )
}

private fun isBiometricSupported(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> {
            Timber.d("Reached some auth state. It should be impossible to reach this state!")
            false
        }
    }
}

private fun hasToSetupBiometric(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> true
        else -> {
            false
        }
    }
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    MoneyFlowTheme {
        Surface {
            SettingsScreenContent(
                onImportDatabaseClick = {},
                onExportDatabaseClick = {},
                openDropboxSetup = {},
                biometricState = true,
                isBiometricSupported = true,
                onBiometricEnabled = {},
                hideSensitiveDataState = true,
                onHideSensitiveDataEnabled = {},
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenLightNoBiometricPreview() {
    MoneyFlowTheme {
        Surface {
            SettingsScreenContent(
                onImportDatabaseClick = {},
                onExportDatabaseClick = {},
                openDropboxSetup = {},
                isBiometricSupported = false,
                biometricState = true,
                onBiometricEnabled = {},
                hideSensitiveDataState = false,
                onHideSensitiveDataEnabled = {},
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    MoneyFlowTheme(darkTheme = true) {
        Surface {
            SettingsScreenContent(
                onImportDatabaseClick = {},
                onExportDatabaseClick = {},
                openDropboxSetup = {},
                isBiometricSupported = true,
                biometricState = true,
                onBiometricEnabled = {},
                hideSensitiveDataState = false,
                onHideSensitiveDataEnabled = {},
            )
        }
    }
}