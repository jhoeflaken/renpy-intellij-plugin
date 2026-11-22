package io.mayasoft.renpy.language.psi

import com.intellij.psi.tree.IElementType
import io.mayasoft.renpy.language.RenpyLanguage

/**
 * Element type for composite PSI and AST nodes in Ren'Py syntax trees.
 *
 * @param debugName the human-readable identifier for debugging and AST inspection
 */
class RenpyElementType(debugName: String) : IElementType(debugName, RenpyLanguage.INSTANCE) {
    override fun toString(): String = "RenpyElementType." + super.toString()
}
