package cz.dynawest.jtexy

import java.util.*

/**
 * Should be faster than map-based protector.
 * TODO: Get rid of the map-based one, or refactor to share the common code.
 *
 * @author Ondrej Zizka
 */
class ProtectorArray : Protector() {
    // HashMap of stored (protected) strings.
    private val safe: MutableList<String> = LinkedList()

    /**
     * Stores the given string and returns a key under which it's stored.
     */
    override fun protect(str: String, type: ContentType): String {
        val curSize = safe.size
        val key = Utils.intToKey(curSize)
        safe.add( /*curSize,*/str)
        val typeStr = type.delimAsString
        return typeStr + key + typeStr
    }

    fun protect(str: String): String {
        return protect(str, ContentType.MARKUP)
    }
    /**
     * @param encodedKey  Encoded ID, i.e. without delimiters.
     * @Override
     * protected String getStringByEncodedID( String encodedKey ){
     * return this.safe.get(Utils.texyMarkupToInt(encodedKey));
     * }
     */
    /**
     * @param wholeKey  Surrounded by delimiters.
     * TODO: Re-use getStringByEncodedID()?
     */
    override fun unprotect(wholeKey: String): String? {
        val index = Utils.keyToInt(wholeKey)
        return safe[index]
    }
}
