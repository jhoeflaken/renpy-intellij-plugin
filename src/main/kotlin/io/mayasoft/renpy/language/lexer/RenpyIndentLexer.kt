package io.mayasoft.renpy.language.lexer

import com.intellij.lexer.Lexer
import com.intellij.lexer.LookAheadLexer
import com.intellij.psi.TokenType
import io.mayasoft.renpy.language.psi.RenpyTokenTypes
import kotlin.math.max

/**
 * Indentation-aware adapter lexer that wraps [RenpyLexerAdapter] to produce Python-style block tokens.
 *
 * Ren'Py script syntax relies on indentation levels to delimit code and statement blocks.
 * This class extends [LookAheadLexer] to inspect base tokens emitted by the JFlex lexer,
 * track leading whitespace and indentation depth using an internal stack, and synthesize
 * [RenpyTokenTypes.INDENT] and [RenpyTokenTypes.DEDENT] tokens at block boundaries.
 *
 * ### Key Behavioral Rules:
 * - **Indentation Stack:** Maintained as a stack of column offsets starting with `[0]`.
 * - **Indentation Calculation:** Spaces count as 1 column; tabs advance to the next multiple of 4 columns.
 * - **Blank & Comment Lines:** Blank lines or lines containing only whitespace and comments do not alter the indentation stack.
 * - **Bracket Nesting:** Enclosing parentheses `()`, brackets `[]`, and braces `{}` suppress indentation shifts until all open pairs are closed.
 * - **Stream Termination:** At EOF, any remaining unclosed indentation levels are unwound by emitting synthetic [RenpyTokenTypes.DEDENT] tokens down to level 0.
 *
 * @param baseLexer the underlying base lexer (defaults to [RenpyLexerAdapter])
 */
class RenpyIndentLexer(baseLexer: Lexer = RenpyLexerAdapter()) : LookAheadLexer(baseLexer) {

    private val indentStack = mutableListOf<Int>()
    private var atLineStart = true
    private var bracketDepth = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        indentStack.clear()
        indentStack.add(0)
        atLineStart = true
        bracketDepth = 0
        super.start(buffer, startOffset, endOffset, initialState)
    }

    override fun lookAhead(baseLexer: Lexer) {
        val currentType = baseLexer.tokenType
        if (currentType == null) {
            if (indentStack.size > 1) {
                while (indentStack.size > 1) {
                    indentStack.removeAt(indentStack.size - 1)
                    addToken(baseLexer.tokenEnd, RenpyTokenTypes.DEDENT)
                }
            } else {
                super.lookAhead(baseLexer)
            }
            return
        }

        if (atLineStart && bracketDepth == 0) {
            processLineIndent(baseLexer)
            return
        }

        when (currentType) {
            RenpyTokenTypes.LPAREN, RenpyTokenTypes.LBRACKET, RenpyTokenTypes.LBRACE -> bracketDepth++
            RenpyTokenTypes.RPAREN, RenpyTokenTypes.RBRACKET, RenpyTokenTypes.RBRACE -> bracketDepth = max(0, bracketDepth - 1)
            RenpyTokenTypes.NEWLINE -> {
                if (bracketDepth == 0) {
                    atLineStart = true
                }
            }
        }
        advanceLexer(baseLexer)
    }

    private fun processLineIndent(baseLexer: Lexer) {
        val currentType = baseLexer.tokenType ?: return

        if (currentType == TokenType.WHITE_SPACE) {
            val wsStart = baseLexer.tokenStart
            val wsEnd = baseLexer.tokenEnd
            val indent = calculateIndent(baseLexer.tokenText)

            baseLexer.advance()
            val nextType = baseLexer.tokenType

            // Blank line or comment-only line
            if (nextType == RenpyTokenTypes.NEWLINE) {
                addToken(wsEnd, TokenType.WHITE_SPACE)
                addToken(baseLexer.tokenEnd, RenpyTokenTypes.NEWLINE)
                baseLexer.advance()
                atLineStart = true
                return
            }

            if (nextType == RenpyTokenTypes.COMMENT) {
                addToken(wsEnd, TokenType.WHITE_SPACE)
                addToken(baseLexer.tokenEnd, RenpyTokenTypes.COMMENT)
                baseLexer.advance()
                atLineStart = false
                return
            }

            if (nextType == null) {
                addToken(wsEnd, TokenType.WHITE_SPACE)
                return
            }

            // Normal line with leading indentation
            addToken(wsEnd, TokenType.WHITE_SPACE)

            val currentIndent = indentStack.last()
            if (indent > currentIndent) {
                indentStack.add(indent)
                addToken(wsEnd, RenpyTokenTypes.INDENT)
            } else if (indent < currentIndent) {
                while (indentStack.size > 1 && indentStack.last() > indent) {
                    indentStack.removeAt(indentStack.size - 1)
                    addToken(wsEnd, RenpyTokenTypes.DEDENT)
                }
            }

            atLineStart = false
            when (nextType) {
                RenpyTokenTypes.LPAREN, RenpyTokenTypes.LBRACKET, RenpyTokenTypes.LBRACE -> bracketDepth++
                RenpyTokenTypes.RPAREN, RenpyTokenTypes.RBRACKET, RenpyTokenTypes.RBRACE -> bracketDepth = max(0, bracketDepth - 1)
            }
            addToken(baseLexer.tokenEnd, nextType)
            baseLexer.advance()
            return
        }

        if (currentType == RenpyTokenTypes.NEWLINE) {
            addToken(baseLexer.tokenEnd, RenpyTokenTypes.NEWLINE)
            baseLexer.advance()
            atLineStart = true
            return
        }

        if (currentType == RenpyTokenTypes.COMMENT) {
            addToken(baseLexer.tokenEnd, RenpyTokenTypes.COMMENT)
            baseLexer.advance()
            atLineStart = false
            return
        }

        // Line with 0 indent
        val currentIndent = indentStack.last()
        if (currentIndent > 0) {
            while (indentStack.size > 1 && indentStack.last() > 0) {
                indentStack.removeAt(indentStack.size - 1)
                addToken(baseLexer.tokenStart, RenpyTokenTypes.DEDENT)
            }
        }

        atLineStart = false
        when (currentType) {
            RenpyTokenTypes.LPAREN, RenpyTokenTypes.LBRACKET, RenpyTokenTypes.LBRACE -> bracketDepth++
            RenpyTokenTypes.RPAREN, RenpyTokenTypes.RBRACKET, RenpyTokenTypes.RBRACE -> bracketDepth = max(0, bracketDepth - 1)
        }
        advanceLexer(baseLexer)
    }

    private fun calculateIndent(text: CharSequence): Int {
        var count = 0
        for (i in 0 until text.length) {
            when (text[i]) {
                ' ' -> count++
                '\t' -> count = (count / 4 + 1) * 4
                else -> count++
            }
        }
        return count
    }
}
