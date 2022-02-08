package kniezrec.com.flightinfo.cards.map

import android.content.Context
import kniezrec.com.flightinfo.common.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Copyright by Kamil Niezrecki
 */

class MapHelper(private val context: Context) {

    suspend fun doCopy() : Boolean = withContext(Dispatchers.IO) {
        Timber.i("Start map copy")
        var inStream: InputStream? = null
        var out: OutputStream? = null
        var isSuccessful = true

        try {
            val osmFile = Constants.getMapFile(context)
            inStream = context.assets.open(Constants.OSMDROID_FILE)
            out = FileOutputStream(osmFile)

            copyFile(inStream, out)
        } catch (e: IOException) {
            isSuccessful = false
            Timber.e(e)
        } finally {
            try {
                inStream?.close()
                out?.flush()
                out?.close()
            } catch (e: IOException) {
                isSuccessful = false
                Timber.e(e)
            }
        }

        Timber.i("Map copy finish. Returning results.")
        return@withContext isSuccessful
    }

    @Throws(IOException::class)
    private suspend fun copyFile(inStream: InputStream?, out: OutputStream?) = withContext(Dispatchers.IO) {
        val buffer = ByteArray(1024)
        var read: Int? = 0

        while (run {
                read = inStream?.read(buffer)
                read
            } != -1) {
            out?.write(buffer, 0, read!!)
        }
    }
}