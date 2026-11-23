package io.mayasoft.renpy.lexer.model

/**
 * Represents a range of text within a document.
 *
 * @param startOffset The offset of the first character in the range.
 * @param endOffset The offset of the character following the last character in the range.
 */
data class TextRange(
    val startOffset: Int,
    val endOffset: Int
)

/**
 * Represents a physical line within a document.
 *
 * @param lineNumber The line number of the physical line.
 * @param startOffset The offset of the first character in the line.
 * @param contentEndOffset The offset of the character following the last character in the line content.
 * @param endOffset The offset of the character following the last character in the line.
 * @param content The content of the physical line.
 */
data class PhysicalLine(
    val lineNumber: Int,
    val startOffset: Int,
    val contentEndOffset: Int,
    val endOffset: Int,
    val content: String
)

/**
 * Represents a logical line within a document.
 *
 * @param startLineNumber The line number of the first physical line in the logical line.
 * @param endLineNumber The line number of the last physical line in the logical line.
 * @param startOffset The offset of the first character in the logical line.
 * @param endOffset The offset of the character following the last character in the logical line.
 * @param indent The indentation level of the logical line.
 * @param segments The list of text ranges that make up the logical line.
 * @param content The content of the logical line.
 */
data class LogicalLine(
    val startLineNumber: Int,
    val endLineNumber: Int,
    val startOffset: Int,
    val endOffset: Int,
    val indent: Int,
    val segments: List<TextRange>,
    val content: String
)

/**
 * The diagnostic codes used by the lexer.
 */
enum class DiagnosticCode {
    /**
     * The closing delimiter was not matched.
     */
    UNMATCHED_CLOSING_DELIMITER,

    /**
     * The delimiter was not closed.
     */
    UNCLOSED_DELIMITER,

    /**
     * The string was not closed.
     */
    UNCLOSED_STRING,

    /**
     * The indentation was inconsistent.
     */
    TAB_INDENTATION,

    /**
     * The indentation was inconsistent.
     */
    INCONSISTENT_INDENTATION
}

/**
 * Representation of a diagnostic produced by the lexer.
 *
 * @param code The diagnostic code.
 * @param message The diagnostic message.
 * @param offset The offset of the diagnostic.
 * @param lineNumber The line number of the diagnostic.
 */
data class LexicalDiagnostic(
    val code: DiagnosticCode,
    val message: String,
    val offset: Int,
    val lineNumber: Int
)

/**
 * The result of a logical scan.
 *
 * @param logicalLines The list of logical lines.
 * @param diagnostics The list of diagnostics.
 * @param sourceLength The length of the source.
 */
data class LogicalScanResult(
    val logicalLines: List<LogicalLine>,
    val diagnostics: List<LexicalDiagnostic>,
    val sourceLength: Int
)