package com.omar.musica.settings.encriptation

import android.content.Context
import android.util.Log
import java.io.File
import net.lingala.zip4j.ZipFile
import kotlin.text.toCharArray



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
