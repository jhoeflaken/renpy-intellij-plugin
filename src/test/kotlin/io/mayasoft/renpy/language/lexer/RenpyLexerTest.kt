package io.mayasoft.renpy.language.lexer

import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType
import com.intellij.testFramework.LexerTestCase
import io.mayasoft.renpy.language.psi.RenpyTokenTypes

/**
 * Unit test suite validating tokenization and indentation processing in [RenpyIndentLexer].
 *
 * Covers core Ren'Py language constructs including dialogue, control flow, menus,
 * one-line Python statements, blank lines, comments, and line continuations.
 */
class RenpyLexerTest : LexerTestCase() {

    override fun createLexer(): Lexer = RenpyIndentLexer()

    override fun getDirPath(): String = ""

    /**
     * Tests basic Ren'Py script tokenization including statements, dialogue, and block indentation.
     */
    fun testSimpleScript() {
        val code = """
            label start:
                scene bg room
                show eileen happy at left
                e "Hello, world!"
                jump choice1
        """.trimIndent()

        val tokens = tokenize(code)

        val expectedTokenTypes = listOf(
            RenpyTokenTypes.KW_LABEL,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            RenpyTokenTypes.COLON,
            RenpyTokenTypes.NEWLINE,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.INDENT,
            RenpyTokenTypes.KW_SCENE,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            RenpyTokenTypes.NEWLINE,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.KW_SHOW,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.KW_AT,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            RenpyTokenTypes.NEWLINE,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.STRING_LITERAL,
            RenpyTokenTypes.NEWLINE,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.KW_JUMP,
            TokenType.WHITE_SPACE,
            RenpyTokenTypes.IDENTIFIER,
            RenpyTokenTypes.DEDENT
        )

        assertEquals(expectedTokenTypes, tokens.map { it.first })
    }

    /**
     * Tests nested block indentation in menu choices and verifies INDENT/DEDENT token balance.
     */
    fun testNestedIndentationAndMenu() {
        val code = """
            menu:
                "Option 1":
                    jump opt1
                "Option 2":
                    jump opt2
            label after:
                pass
        """.trimIndent()

        val tokens = tokenize(code)
        val tokenTypes = tokens.map { it.first }

        assertTrue(tokenTypes.contains(RenpyTokenTypes.KW_MENU))
        assertTrue(tokenTypes.contains(RenpyTokenTypes.INDENT))
        assertTrue(tokenTypes.contains(RenpyTokenTypes.DEDENT))
        assertTrue(tokenTypes.contains(RenpyTokenTypes.KW_PASS))

        // Verify INDENT/DEDENT counts match
        val indentCount = tokenTypes.count { it == RenpyTokenTypes.INDENT }
        val dedentCount = tokenTypes.count { it == RenpyTokenTypes.DEDENT }
        assertEquals("INDENT and DEDENT count must balance", indentCount, dedentCount)
    }

    /**
     * Tests single-line Python statements beginning with `$`.
     */
    fun testOneLinePython() {
        val code = """
            ${'$'} count += 1
            ${'$'} renpy.pause(1.0)
        """.trimIndent()

        val tokens = tokenize(code)
        val types = tokens.map { it.first }

        assertEquals(RenpyTokenTypes.ONE_LINE_PYTHON, types[0])
        assertEquals(RenpyTokenTypes.PYTHON_CODE, types[1])
        assertEquals(" count += 1", tokens[1].second)
    }

    /**
     * Tests that comments and blank lines within indented blocks do not disrupt indentation state.
     */
    fun testCommentsAndBlankLinesInBlocks() {
        val code = """
            label start:
                # A comment inside block

                scene bg room # trailing comment
                return
        """.trimIndent()

        val tokens = tokenize(code)
        val types = tokens.map { it.first }

        val indentCount = types.count { it == RenpyTokenTypes.INDENT }
        val dedentCount = types.count { it == RenpyTokenTypes.DEDENT }
        assertEquals(1, indentCount)
        assertEquals(1, dedentCount)
    }

    /**
     * Tests that backslash line continuations do not trigger indentation tokens.
     */
    fun testLineContinuation() {
        val code = """
            show eileen \
                happy
        """.trimIndent()

        val tokens = tokenize(code)
        val types = tokens.map { it.first }

        assertTrue(types.contains(RenpyTokenTypes.LINE_CONTINUATION))
        // Line continuation shouldn't create extra indent blocks
        val indentCount = types.count { it == RenpyTokenTypes.INDENT }
        assertEquals(0, indentCount)
    }

    /**
     * Tests scanning of various string literal styles and number formats.
     */
    fun testStringsAndNumbers() {
        val code = """
            define e = Character("Eileen", color="#c8ffc8")
            default score = 42
            default ratio = 3.14
            default hex_val = 0xFF
        """.trimIndent()

        val tokens = tokenize(code)
        val types = tokens.map { it.first }

        assertTrue(types.contains(RenpyTokenTypes.KW_DEFINE))
        assertTrue(types.contains(RenpyTokenTypes.KW_DEFAULT))
        assertTrue(types.contains(RenpyTokenTypes.STRING_LITERAL))
        assertTrue(types.contains(RenpyTokenTypes.NUMBER))
    }

    private fun tokenize(text: String): List<Pair<com.intellij.psi.tree.IElementType, String>> {
        val lexer = createLexer()
        lexer.start(text)
        val result = mutableListOf<Pair<com.intellij.psi.tree.IElementType, String>>()
        while (lexer.tokenType != null) {
            result.add(lexer.tokenType!! to lexer.tokenText)
            lexer.advance()
        }
        return result
    }
}
