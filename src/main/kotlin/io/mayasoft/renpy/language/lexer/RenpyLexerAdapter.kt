package io.mayasoft.renpy.language.lexer

import com.intellij.lexer.FlexAdapter

/**
 * Platform adapter bridging the generated JFlex scanner [_RenpyLexer] to the IntelliJ [com.intellij.lexer.Lexer] API.
 *
 * Scans raw character sequences into base tokens (keywords, literals, comments, operators, newlines).
 */
class RenpyLexerAdapter : FlexAdapter(_RenpyLexer(null))
