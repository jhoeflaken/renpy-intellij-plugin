package io.mayasoft.renpy.lexer

import io.mayasoft.renpy.lexer.model.DiagnosticCode
import io.mayasoft.renpy.lexer.model.LexicalDiagnostic
import io.mayasoft.renpy.lexer.model.LogicalLine
import io.mayasoft.renpy.lexer.model.LogicalScanResult
import io.mayasoft.renpy.lexer.model.TextRange

/**
 * Converts physical source lines into logical Ren'Py statements.
 *
 * A *physical line* is a line separated by a newline in the source file. A
 * *logical line* is one statement as seen by the parser. A logical line can
 * span multiple physical lines, for example:
 *
 * ```renpy
 * $ result = calculate(
 *     first_value,
 *     second_value,
 * )
 * ```
 *
 * This scanner deliberately does not parse expressions or determine what a
 * statement means. It only tracks enough lexical context to know whether a
 * physical newline ends a statement or whether scanning must continue.
 *
 * Source offsets in the result always refer to the original source, not to
 * the joined text. This is important because the IDE uses those offsets for
 * highlighting, diagnostics and navigation.
 */
class RenPyLogicalLineScanner {

    /**
     * Scan the complete source and return logical lines plus recoverable
     * lexical diagnostics. Malformed input is reported rather than throwing,
     * because an editor must also be able to scan a file while it is being typed.
     */
    fun scan(source: CharSequence): LogicalScanResult {
        // First split the input without losing the original offsets. The
        // physical-line reader handles LF, CRLF and CR consistently.
        val physical = PhysicalLineReader.read(source)

        val output = mutableListOf<LogicalLine>()
        val diagnostics = mutableListOf<LexicalDiagnostic>()
        var active = false                          // Is there an active logical line in progress?
        var startOffset = 0                         // First offset of the current logical line. (excludes leading indentation)
        var startLine = 1                           // Physical line number of the current logical line.
        var indent = 0                              // Indentation of the current logical line.
        var segments = mutableListOf<TextRange>()   // Original source ranges contributing content to the current logical line.
        val text = StringBuilder()                  // Text content of the current logical line.

        // Open (), [] and {} delimiters keep a statement open across physical
        // newlines. Store the opening character and its source position so an
        // unclosed delimiter can be reported at the point where it began.
        val delimiters = ArrayDeque<Pair<Char, Pair<Int, Int>>>()

        var quote: Char? = null
        var triple = false                          // Whether the current string is a triple-quoted string.
        var escaped = false                         // Whether the previous character was a backslash.
        var stringOffset = -1                       // Opening quote position for diagnostic purposes.
        var stringLine = -1

        /**
         * Finish scanning the current logical line and add it to the output.
         *
         * @param end The end offset of the logical line.
         * @param endLine The end line number of the logical line.
         */
        fun finish(end: Int, endLine: Int) {
            if (!active) return

            output += LogicalLine(
                startLine,
                endLine, startOffset,
                end,
                indent,
                segments.toList(),
                text.toString()
            )

            active = false
            segments = mutableListOf()
            text.setLength(0)
        }

        for ((lineNumber, lineStartOffset, contentEndOffset, _, s) in physical) {
            var i = 0

            // Indentation is meaningful only at the beginning of a new
            // statement, when we are not already inside a multiline string or
            // an open bracket expression. Continuation lines do not establish
            // a new indentation level.
            if (!active && quote == null && delimiters.isEmpty()) {
                var p = 0
                var columns = 0

                // Count spaces as one column. Tabs advance to the next
                // 8-column boundary (the conventional Python indentation rule)
                // and are diagnosed because spaces are safer and predictable.
                while (p < s.length && (s[p] == ' ' || s[p] == '\t')) {
                    if (s[p] == '\t') {
                        diagnostics += LexicalDiagnostic(
                            DiagnosticCode.TAB_INDENTATION,
                            "Tab in indentation.",
                            lineStartOffset + p,
                            lineNumber
                        )
                        columns = (columns / 8 + 1) * 8
                    } else
                        columns++

                    p++
                }

                // Empty and comment-only lines are not statements and must
                // not create INDENT/DEDENT changes in the later grouper.
                if (p == s.length || s[p] == '#')
                    continue

                active = true
                startOffset = lineStartOffset + p
                startLine = lineNumber
                indent = columns
                i = p
            } else if (!active) {
                active = true
                startOffset = lineStartOffset
                startLine = lineNumber
                indent = 0
            }

            // Remember where this physical line contributes text. `i` can be
            // non-zero on the first line because leading indentation is not
            // part of LogicalLine.text; continuation lines start at column 0.
            val segmentStart = i
            var commentAt: Int? = null

            while (i < s.length) {
                val c = s[i]
                val absolute = lineStartOffset + i

                if (quote != null) {
                    // Inside a string, ignore comment markers and delimiters.
                    // Escaping is processed before looking for a closing quote,
                    // so an escaped quote cannot terminate the string.
                    if (escaped) {
                        escaped = false; i++; continue
                    }

                    if (c == '\\') {
                        escaped = true; i++; continue
                    }

                    // A triple-quoted string closes only on three consecutive
                    // copies of its opening quote. A normal string closes on
                    // the next unescaped matching quote.
                    if (triple && (c == quote) && ((i + 2) < s.length) && (s[i + 1] == quote) && (s[i + 2] == quote)) {
                        quote = null; triple = false; i += 3; continue
                    }

                    if (!triple && c == quote) {
                        quote = null; i++; continue
                    }

                    i++; continue
                }

                // Outside strings, these characters affect how the rest of
                // the line is interpreted. In particular, '#' starts a comment
                // only when it is not inside a quoted string.
                when (c) {
                    '#' -> {
                        commentAt = i; break
                    }
                    '\'', '"' -> {
                        quote = c; stringOffset = absolute; stringLine = lineNumber
                        triple = i + 2 < s.length && s[i + 1] == c && s[i + 2] == c
                        i += if (triple) 3 else 1
                    }
                    '(', '[', '{' -> {
                        delimiters.addLast(c to (absolute to lineNumber)); i++
                    }
                    ')', ']', '}' -> {
                        // Closing delimiters must match the most recently
                        // opened delimiter. Do not pop on a mismatch: retaining
                        // the stack gives the end-of-file diagnostic useful
                        // information about what was left unclosed.
                        val expected = when (c) {
                            ')' -> '('
                            ']' -> '['
                            else -> '{'
                        }

                        if (delimiters.lastOrNull()?.first == expected)
                            delimiters.removeLast()
                        else diagnostics += LexicalDiagnostic(
                            DiagnosticCode.UNMATCHED_CLOSING_DELIMITER,
                            "Unmatched closing delimiter '$c'.",
                            absolute,
                            lineNumber
                        )

                        i++
                    }
                    else -> i++
                }
            }

            // Exclude comments from the logical text, but leave the original
            // source untouched. The source ranges let later stages map joined
            // text back to the editor document.
            val endInLine = commentAt ?: s.length

            if (endInLine > segmentStart) {
                val a = lineStartOffset + segmentStart
                val b = lineStartOffset + endInLine
                segments += TextRange(a, b)
                if (text.isNotEmpty()) text.append('\n')
                text.append(s.substring(segmentStart, endInLine))
            }

            // Explicit continuation: a final, unescaped backslash joins the
            // next physical line even when no bracket is open. Trailing spaces
            // are ignored when deciding whether the backslash is final.
            var last = endInLine - 1

            while (last >= segmentStart && (s[last] == ' ' || s[last] == '\t'))
                last--

            val continuation = quote == null && commentAt == null && last >= segmentStart &&
                    s[last] == '\\' && isUnescapedBackslash(s, last)

            if (continuation && segments.isNotEmpty()) {
                // The backslash is a line-joining marker, not part of the
                // logical statement text/range, so trim it from both views.
                segments[segments.lastIndex] = segments.last().copy(endOffset = lineStartOffset + last)
                if (text.isNotEmpty() && text.last() == '\\') text.setLength(text.length - 1)
            }

            // A newline ends the logical line only when every continuation
            // condition has cleared: no open string, no open bracket and no
            // explicit trailing backslash.
            if (quote == null && delimiters.isEmpty() && !continuation)
                finish(contentEndOffset, lineNumber)
        }

        // End-of-file recovery. These diagnostics do not prevent returning
        // the logical lines already scanned; that is important for live editing.
        if (quote != null)
            diagnostics += LexicalDiagnostic(
                DiagnosticCode.UNCLOSED_STRING,
                "Unterminated string.",
                stringOffset,
                stringLine
        )

        if (delimiters.isNotEmpty()) {
            val (d, pos) = delimiters.last()
            diagnostics += LexicalDiagnostic(
                DiagnosticCode.UNCLOSED_DELIMITER,
                "Unclosed delimiter '$d'.",
                pos.first,
                pos.second
            )
        }

        if (active)
            finish(source.length, physical.lastOrNull()?.lineNumber ?: 1)

        return LogicalScanResult(output, diagnostics, source.length)
    }

    /**
     * Determines if the backslash at the given [index] is unescaped.
     *
     * @param s The source string.
     * @param index The index of the backslash.
     * @return True if the backslash is unescaped, false otherwise.
     */
    private fun isUnescapedBackslash(s: String, index: Int): Boolean {
        var count = 0
        var i = index
        while (i >= 0 && s[i] == '\\') {
            count++
            i--
        }

        return count % 2 == 1
    }

}