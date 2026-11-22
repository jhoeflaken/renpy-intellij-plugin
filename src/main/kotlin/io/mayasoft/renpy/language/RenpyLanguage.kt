package io.mayasoft.renpy.language

import com.intellij.lang.Language

/**
 * Definition of the Ren'Py language for the IntelliJ Platform.
 *
 * Serves as the language identifier and root descriptor for file types, parsers, and PSI elements.
 */
class RenpyLanguage private constructor() : Language("RenPy") {
    companion object {
        /**
         * Singleton instance representing the Ren'Py language.
         */
        @JvmStatic
        val INSTANCE = RenpyLanguage()
    }
}
