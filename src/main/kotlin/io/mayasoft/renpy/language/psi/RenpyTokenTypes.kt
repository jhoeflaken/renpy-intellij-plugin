package io.mayasoft.renpy.language.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

/**
 * Constants and token sets defining lexical tokens across the Ren'Py language.
 *
 * Encompasses structural/layout tokens (indentation, newlines), literals, statements,
 * keywords, operators, and Python embedding delimiters.
 */
object RenpyTokenTypes {
    // Structural and Layout Tokens
    @JvmField val NEWLINE = RenpyTokenType("NEWLINE")
    @JvmField val INDENT = RenpyTokenType("INDENT")
    @JvmField val DEDENT = RenpyTokenType("DEDENT")
    @JvmField val LINE_CONTINUATION = RenpyTokenType("LINE_CONTINUATION")
    @JvmField val COMMENT = RenpyTokenType("COMMENT")
    @JvmField val ONE_LINE_PYTHON = RenpyTokenType("ONE_LINE_PYTHON") // '$'
    @JvmField val PYTHON_CODE = RenpyTokenType("PYTHON_CODE")

    // Literals and Identifiers
    @JvmField val IDENTIFIER = RenpyTokenType("IDENTIFIER")
    @JvmField val STRING_LITERAL = RenpyTokenType("STRING_LITERAL")
    @JvmField val NUMBER = RenpyTokenType("NUMBER")

    // Ren'Py Keywords
    @JvmField val KW_LABEL = RenpyTokenType("label")
    @JvmField val KW_JUMP = RenpyTokenType("jump")
    @JvmField val KW_CALL = RenpyTokenType("call")
    @JvmField val KW_RETURN = RenpyTokenType("return")
    @JvmField val KW_SCENE = RenpyTokenType("scene")
    @JvmField val KW_SHOW = RenpyTokenType("show")
    @JvmField val KW_HIDE = RenpyTokenType("hide")
    @JvmField val KW_WITH = RenpyTokenType("with")
    @JvmField val KW_BEHIND = RenpyTokenType("behind")
    @JvmField val KW_AS = RenpyTokenType("as")
    @JvmField val KW_AT = RenpyTokenType("at")
    @JvmField val KW_ONLAYER = RenpyTokenType("onlayer")
    @JvmField val KW_ZORDER = RenpyTokenType("zorder")
    @JvmField val KW_MENU = RenpyTokenType("menu")
    @JvmField val KW_PYTHON = RenpyTokenType("python")
    @JvmField val KW_INIT = RenpyTokenType("init")
    @JvmField val KW_DEFINE = RenpyTokenType("define")
    @JvmField val KW_DEFAULT = RenpyTokenType("default")
    @JvmField val KW_TRANSFORM = RenpyTokenType("transform")
    @JvmField val KW_SCREEN = RenpyTokenType("screen")
    @JvmField val KW_STYLE = RenpyTokenType("style")
    @JvmField val KW_IMAGE = RenpyTokenType("image")
    @JvmField val KW_PLAY = RenpyTokenType("play")
    @JvmField val KW_STOP = RenpyTokenType("stop")
    @JvmField val KW_QUEUE = RenpyTokenType("queue")
    @JvmField val KW_VOICE = RenpyTokenType("voice")
    @JvmField val KW_SOUND = RenpyTokenType("sound")
    @JvmField val KW_MUSIC = RenpyTokenType("music")
    @JvmField val KW_PAUSE = RenpyTokenType("pause")
    @JvmField val KW_WINDOW = RenpyTokenType("window")
    @JvmField val KW_NVL = RenpyTokenType("nvl")

    // Control flow keywords
    @JvmField val KW_IF = RenpyTokenType("if")
    @JvmField val KW_ELIF = RenpyTokenType("elif")
    @JvmField val KW_ELSE = RenpyTokenType("else")
    @JvmField val KW_WHILE = RenpyTokenType("while")
    @JvmField val KW_FOR = RenpyTokenType("for")
    @JvmField val KW_PASS = RenpyTokenType("pass")
    @JvmField val KW_IN = RenpyTokenType("in")
    @JvmField val KW_NOT = RenpyTokenType("not")
    @JvmField val KW_AND = RenpyTokenType("and")
    @JvmField val KW_OR = RenpyTokenType("or")
    @JvmField val KW_IS = RenpyTokenType("is")

    // Boolean and None
    @JvmField val KW_TRUE = RenpyTokenType("True")
    @JvmField val KW_FALSE = RenpyTokenType("False")
    @JvmField val KW_NONE = RenpyTokenType("None")

    // Punctuation and Operators
    @JvmField val COLON = RenpyTokenType(":")
    @JvmField val COMMA = RenpyTokenType(",")
    @JvmField val DOT = RenpyTokenType(".")
    @JvmField val EQ = RenpyTokenType("=")
    @JvmField val EQEQ = RenpyTokenType("==")
    @JvmField val NEQ = RenpyTokenType("!=")
    @JvmField val LEQ = RenpyTokenType("<=")
    @JvmField val GEQ = RenpyTokenType(">=")
    @JvmField val LT = RenpyTokenType("<")
    @JvmField val GT = RenpyTokenType(">")
    @JvmField val PLUS = RenpyTokenType("+")
    @JvmField val MINUS = RenpyTokenType("-")
    @JvmField val STAR = RenpyTokenType("*")
    @JvmField val SLASH = RenpyTokenType("/")
    @JvmField val PERCENT = RenpyTokenType("%")
    @JvmField val LPAREN = RenpyTokenType("(")
    @JvmField val RPAREN = RenpyTokenType(")")
    @JvmField val LBRACKET = RenpyTokenType("[")
    @JvmField val RBRACKET = RenpyTokenType("]")
    @JvmField val LBRACE = RenpyTokenType("{")
    @JvmField val RBRACE = RenpyTokenType("}")
    @JvmField val AT = RenpyTokenType("@")

    /**
     * Token set containing comment tokens.
     */
    @JvmField val COMMENTS = TokenSet.create(COMMENT)

    /**
     * Token set containing string literal tokens.
     */
    @JvmField val STRINGS = TokenSet.create(STRING_LITERAL)

    /**
     * Token set containing whitespace tokens.
     */
    @JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
}
