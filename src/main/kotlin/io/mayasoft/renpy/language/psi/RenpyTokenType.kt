package io.mayasoft.renpy.language.psi

import com.intellij.psi.tree.IElementType
import io.mayasoft.renpy.language.RenpyLanguage

/**
 * Element type for lexical tokens emitted by the Ren'Py lexer (e.g. keywords, identifiers, operators, layout tokens).
 *
 * @param debugName the human-readable identifier for debugging and AST inspection
 */
class RenpyTokenType(debugName: String) : IElementType(debugName, RenpyLanguage.INSTANCE) {
    override fun toString(): String = "RenpyTokenType." + super.toString()
}
