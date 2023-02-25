package cz.dynawest.jtexy

import cz.dynawest.jtexy.util.JTexyStringUtils
import org.apache.commons.lang.StringUtils

/**
 * Interface of protector - a key-value storage for strings.
 * @author Ondrej Zizka
 */
abstract class Protector {
    /**
     * Stores the given string and returns a key (with delimiters) under which it's stored.
     */
    abstract fun protect(str: String?, type: ContentType): String

    /**
     * Retrieves a string stored under given key (with delimiters).
     */
    abstract fun unprotect(wholeKey: String): String?

    /**
     * Unprotects all protected sub-strings in given string.
     */
    @Throws(TexyException::class)
    fun unprotectAll(str: String?): String {
        val sb = StringBuilder()
        var nextPos = 0
        var keyStart: Int

        // For each protector key found in the string...
        while (-1 != JTexyStringUtils.indexOfAny(str, CONTENT_TYPE_CHARS.toCharArray(), nextPos).also { keyStart = it }) {

            // Append text between this and the prev key (if any).
            if (nextPos != keyStart) sb.append(str!!.substring(nextPos, keyStart))

            // The key.
            val delimChar = str!![keyStart]
            val keyEnd = str.indexOf(delimChar, keyStart + 1)
            if (keyEnd == -1) throw TexyException("Protector key end missing: " + StringUtils.abbreviate(str, keyStart, 20))

            // Get the string.
            //String key = str.substring( keyStart+1, keyEnd );
            //String ret = getStringByEncodedID(key); // this.safe.get(Utils.texyMarkupToInt(key));
            val wholeKey = str.substring(keyStart, keyEnd + 1)
            val ret = unprotect(wholeKey)
                ?: throw TexyException(
                    "Protector key [" + Utils.texyMarkupToInt(wholeKey) + "] not found: " + StringUtils.abbreviate(
                        str,
                        keyStart,
                        20
                    )
                )
            sb.append(ret)

            // Advance to the next position.
            nextPos = keyEnd + 1
        }
        sb.append(str!!.substring(nextPos, str.length))
        return sb.toString()
    } // unprotectAll( String str )

    /**
     * Key string conversion helper methods.
     */
    object Utils {
        /**
         * Encodes integer to a key value (without delimiters).
         */
        fun intToKey(`val`: Int): String {
            var key = Integer.toOctalString(`val`)
            key = StringUtils.replaceChars(key, "01234567", "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F")
            return key
        }

        /**
         * Translates the whole key, i.e. with content delimiters, to an integer.
         */
        fun keyToInt(key: String): Int {
            //key = StringUtils.strip(key, "\u0014\u0015\u0016\u0017");
            var key = key
            key = key.substring(1, key.length - 1)
            return texyMarkupToInt(key)
        }

        /**
         * Translates the encoded ID (from between delimiters) to an integer.
         */
        fun texyMarkupToInt(code: String?): Int {
            val num = StringUtils.replaceChars(code, "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F", "01234567")
            return num.toInt(8)
        }
    }

    companion object {
        /**
         * @param key  Encoded ID, i.e. without delimiters.
         */
        //protected abstract String getStringByEncodedID( String key ); // Not used.
        const val CONTENT_TYPE_CHARS = "\u0014\u0015\u0016\u0017"
    }
}
