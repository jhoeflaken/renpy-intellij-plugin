package io.mayasoft.renpy.language

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type descriptor for Ren'Py script files (`.rpy`).
 *
 * Associates Ren'Py script files with [RenpyLanguage] to enable custom lexing, parsing,
 * syntax highlighting, and editor features in JetBrains IDEs.
 */
class RenpyFileType private constructor() : LanguageFileType(RenpyLanguage.INSTANCE) {
    override fun getName(): String = "RenPy File"
    override fun getDescription(): String = "Ren'Py script file"
    override fun getDefaultExtension(): String = "rpy"
    override fun getIcon(): Icon? = null

    companion object {
        /**
         * Singleton instance representing the Ren'Py script file type.
         */
        @JvmStatic
        val INSTANCE = RenpyFileType()
    }
}
