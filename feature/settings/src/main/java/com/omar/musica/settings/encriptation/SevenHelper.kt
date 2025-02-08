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
import java.io.IOException
import java.io.InputStream



//val SevenTAG = "SevenZipHelper"
//
//object SevenZipHelper {
//
//    suspend fun extractAndSaveToAppStorage(
//        archiveFile: File,
//        context: Context,
//        password: String = "123456789"
//    ) {
//        try {
//            // Verifica se o arquivo existe antes de continuar
//            if (!archiveFile.exists()) {
//                Log.e(SevenTAG, "Arquivo de archive não existe: ${archiveFile.absolutePath}")
//                return
//            }
//
//            withContext(Dispatchers.IO) {  // Mover o processo para o dispatcher de IO
//                val randomAccessFile = RandomAccessFile(archiveFile, "r")
//                val inStream = RandomAccessFileInStream(randomAccessFile)
//
//                val callback = ArchiveOpenCallback(password)
//                val inArchive = SevenZip.openInArchive(ArchiveFormat.ZIP, inStream, callback)
//
//                val itemCount = inArchive.numberOfItems
//                Log.i(SevenTAG, "Número de itens: $itemCount")
//
//                // Pega o diretório de armazenamento interno do app
//                val appStorageDir = context.filesDir
//
//                for (i in 0 until itemCount) {
//                    val filePath = inArchive.getStringProperty(i, PropID.PATH)
//
//                    // Verificando se o caminho do arquivo é válido
//                    if (filePath.isNullOrEmpty()) {
//                        Log.e(SevenTAG, "Caminho do arquivo vazio ou inválido para o item: $i")
//                        continue
//                    }
//
//                    // Construa o caminho do arquivo de saída dentro do armazenamento privado do app
//                    val outputFile = File(appStorageDir, filePath)
//                    val parentDir = outputFile.parentFile
//                    if (parentDir?.exists() == false) {
//                        parentDir.mkdirs()  // Cria diretórios se necessário
//                    }
//
//                    val outputStream = FileOutputStream(outputFile)
//                    inArchive.extractSlow(i) { data ->
//                        if (data.isNotEmpty()) {
//                            Log.i(SevenTAG, "Gravando dados no arquivo: ${data.size} bytes")
//                            outputStream.write(data)
//                        } else {
//                            Log.e(SevenTAG, "Nenhum dado extraído para este arquivo!")
//                        }
//                        data.size
//                    }
//                    outputStream.close()
//                    Log.i(SevenTAG, "Arquivo extraído e salvo no armazenamento do app: ${outputFile.absolutePath}")
//                }
//
//                inArchive.close()
//                inStream.close()
//            }
//        } catch (e: Exception) {
//            Log.e(SevenTAG, "Erro ao extrair o archive", e)
//        }
//    }
//
//
//
//    private class ArchiveOpenCallback(private val password: String = "123456789") :
//        IArchiveOpenCallback, ICryptoGetTextPassword {
//
//        override fun setTotal(files: Long?, bytes: Long?) {
//            Log.i(SevenTAG, "Total work: $files files, $bytes bytes")
//        }
//
//        override fun setCompleted(files: Long?, bytes: Long?) {
//            Log.i(SevenTAG, "Completed: $files files, $bytes bytes")
//        }
//
//        // Log para verificar a senha
//        override fun cryptoGetTextPassword(): String {
//            Log.i(SevenTAG, "Password provided to 7zip: $password")
//            return password
//        }
//    }
//
//}


val SevenTAG = "SevenZipHelper"


object  SevenZipHelper {


    fun extractAndSaveToAppStorage(
        archiveFile: File,
        context: Context,
        password: String = "4Oncdcz9HM"
    ): Boolean {
        try {

             val outputDir: File = context.filesDir
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val zipFile = ZipFile(archiveFile)


            if (zipFile.isEncrypted) {
                zipFile.setPassword(password.toCharArray())
            }

            zipFile.extractAll(outputDir.absolutePath)

            println("Arquivos extraídos com sucesso em: ${outputDir.absolutePath}")

            Log.d(SevenTAG, "Arquivos extraídos com sucesso em: ${outputDir.absolutePath}")

            return true
        } catch (e: Exception) {
            println("Erro ao extrair ZIP: ${e.message}")
            return false
        }
    }
}
