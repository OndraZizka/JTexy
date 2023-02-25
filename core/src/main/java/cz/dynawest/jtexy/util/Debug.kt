package cz.dynawest.jtexy.util

import cz.dynawest.jtexy.ContentType

/**
 *
 * @author Ondrej Zizka
 */
object Debug {
    fun showCodes(str: String?): String {
        return str!!.replace(ContentType.BLOCK.delim, '▉')
            .replace(ContentType.TEXTUAL.delim, '₮')
            .replace(ContentType.MARKUP.delim, 'ℳ')
            .replace(ContentType.REPLACED.delim, '℞')
    }

    fun showCodesWithNumbers(str: String): String {
        var str = str
        str = str.replace(ContentType.BLOCK.delim, '▉')
            .replace(ContentType.TEXTUAL.delim, '₮')
            .replace(ContentType.MARKUP.delim, 'ℳ')
            .replace(ContentType.REPLACED.delim, '℞')

        // TODO: Show numbers inside delimiters.
        return str
    }
}
