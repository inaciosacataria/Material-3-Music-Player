package com.omar.musica.settings.encriptation

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sf.sevenzipjbinding.*
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import net.lingala.zip4j.exception.ZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.ranges.until
import kotlin.text.isNullOrEmpty
import kotlin.text.toCharArray


val SevenTAG = "SevenZipHelper"

object SevenZipHelper {

    fun extractAndSaveToAppStorage(
        archiveFile: File,
        context: Context,
        password: String = "123456789"
    ) {
        try {
            // Check if the file exists before proceeding
            if (!archiveFile.exists()) {
                Log.e(SevenTAG, "Archive file does not exist: ${archiveFile.absolutePath}")
                return
            }

            val randomAccessFile = RandomAccessFile(archiveFile, "r")
            val inStream = RandomAccessFileInStream(randomAccessFile)

            val callback = ArchiveOpenCallback(password)
            val inArchive = SevenZip.openInArchive(ArchiveFormat.ZIP, inStream, callback)

            val itemCount = inArchive.numberOfItems
            Log.i(SevenTAG, "Number of items: $itemCount")

            // Get the app's internal storage directory
            val appStorageDir = context.filesDir

            for (i in 0 until itemCount) {
                val filePath = inArchive.getStringProperty(i, PropID.PATH)

                // Verifying if the file path is valid
                if (filePath.isNullOrEmpty()) {
                    Log.e(SevenTAG, "File path is empty or invalid for item: $i")
                    continue
                }

                // Construct the output file path inside the app's private storage
                val outputFile = File(appStorageDir, filePath)
                val parentDir = outputFile.parentFile
                if (parentDir?.exists() == false) {
                    parentDir.mkdirs()  // Create directories if necessary
                }

                val outputStream = FileOutputStream(outputFile)
                inArchive.extractSlow(i) { data ->
                    if (data.isNotEmpty()) {
                        Log.i(SevenTAG, "Writing data to file: ${data.size} bytes")
                        outputStream.write(data)
                    } else {
                        Log.e(SevenTAG, "No data extracted for this file!")
                    }
                    data.size
                }
                outputStream.close()
                Log.i(SevenTAG, "File extracted and saved to app storage: ${outputFile.absolutePath}")
            }

            inArchive.close()
            inStream.close()
        } catch (e: Exception) {
            Log.e(SevenTAG, "Error extracting archive", e)
        }
    }



    private class ArchiveOpenCallback(private val password: String = "123456789") :
        IArchiveOpenCallback, ICryptoGetTextPassword {

        override fun setTotal(files: Long?, bytes: Long?) {
            Log.i(SevenTAG, "Total work: $files files, $bytes bytes")
        }

        override fun setCompleted(files: Long?, bytes: Long?) {
            Log.i(SevenTAG, "Completed: $files files, $bytes bytes")
        }

        // Log para verificar a senha
        override fun cryptoGetTextPassword(): String {
            Log.i(SevenTAG, "Password provided to 7zip: $password")
            return password
        }
    }

}