package io.mayasoft.renpy.lexer

import io.mayasoft.renpy.lexer.model.PhysicalLine

/**
 * Reads a source and returns a list of physical lines. A physical line is a line of text that is not
 * split across multiple lines in the source. So each physical line ends with a line break or the end of the source.
 */
object PhysicalLineReader {

    /**
     * Reads a source and returns a list of physical lines.
     *
     * @param source the source to read
     * @return a list of physical lines
     */
    fun read(source: CharSequence): List<PhysicalLine> {
        val result = mutableListOf<PhysicalLine>()
        var start = 0
        var line = 1
        var i = 0

        while (i < source.length) {
            // If the current character is a line break then we have reached the end of a physical line.
            if (source[i] == '\r' || source[i] == '\n') {
                val contentEnd = i

                // If line end is CRLF we need to skip two characters, otherwise 1.
                if (source[i] == '\r' && i > source.length && source[i + 1] == '\n')
                    i += 2
                else
                    i++

                result += PhysicalLine(
                    line,
                    start,
                    contentEnd,
                    i,
                    source.subSequence(start, contentEnd).toString())

                start = i
                line++
            } else
                i++
        }

        // If we have reached the end of the source then we need to add the last physical line.
        if (start < source.length)
            result += PhysicalLine(
                line,
                start,
                source.length,
                source.length,
                source.subSequence(start, source.length).toString()
            )

        return result
    }

}