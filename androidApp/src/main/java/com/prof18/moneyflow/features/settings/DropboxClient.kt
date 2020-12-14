package com.prof18.moneyflow.features.settings

import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.prof18.moneyflow.utils.DatabaseImportExport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import presentation.dropboxsync.DropboxSyncUserCase
import timber.log.Timber
import java.io.FileInputStream
import java.util.*


class DropboxClient(
    private val dropboxSyncUserCase: DropboxSyncUserCase,
    private val databaseImportExport: DatabaseImportExport
) {

    private var parentJob = Job()
    private val scope = CoroutineScope(parentJob + Dispatchers.IO)

    private var dbxClientV2: DbxClientV2? = null

    private val dropboxClientLinkStatus = MutableStateFlow(DropboxClientStatus.NOT_LINKED)
    fun observeClientStatus(): StateFlow<DropboxClientStatus> = dropboxClientLinkStatus

    init {
        scope.launch {
            val accessToken = dropboxSyncUserCase.getAccessTokenSuspendable()
            Timber.d("Dropbox access token: $accessToken")
            if (accessToken != null) {
                createDropboxClient(accessToken)
            }
        }
    }

    fun setTokenAndCreateClient(accessToken: String) {
        createDropboxClient(accessToken)
    }

    private fun createDropboxClient(accessToken: String) {
        val userLocale: String = Locale.getDefault().toString()
        val requestConfig = DbxRequestConfig
            .newBuilder("moneyflowapp")
            .withUserLocale(userLocale)
            .build()
        dbxClientV2 = DbxClientV2(requestConfig, accessToken)
        dropboxClientLinkStatus.value = DropboxClientStatus.LINKED
        Timber.d("Dropbox client status: ${if (dbxClientV2 == null) "null" else "not null"}")
    }

    suspend fun upload() = withContext(Dispatchers.IO) {
        val databaseFile = databaseImportExport.generateDatabaseFile()
        if (databaseFile != null) {
            try {
                val metadata = dbxClientV2?.files()
                    ?.uploadBuilder("/MoneyFlow.db")
                    ?.withMode(WriteMode.OVERWRITE)
                    ?.uploadAndFinish(FileInputStream(databaseFile))

                val rev = metadata?.rev
                metadata?.serverModified?.time?.let { lastRefresh ->
                    Timber.d("Last refresh new: $lastRefresh")
                    dropboxSyncUserCase.updateLastRefreshSuspendable(lastRefresh)
                }

                Timber.d("Upload Done")

            } catch (e: DbxException) {
                Timber.e("Unable to upload backup on dropbox")
                e.printStackTrace()
            }
        }

        Timber.d("file null?: ${if (databaseFile == null) "yes" else "no"}")
    }

    fun dispose() {
        scope.cancel()
    }

    companion object {
        private const val UPLOAD_FILE_SIZE_LIMIT: Long = 150 // MB
        private const val LARGE_FILE = "File larger then $UPLOAD_FILE_SIZE_LIMIT MB"
    }
}