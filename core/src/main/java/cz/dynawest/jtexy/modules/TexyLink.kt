package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpPatterns
import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Pattern

/**
 *
 * @author Ondrej Zizka
 */
class TexyLink(
    /** URL in resolved form.  */
    var url: String
) : Cloneable {
    enum class Type {
        COMMON, BRACKET, IMAGE
    }

    /** string  URL as written in text  */
    var raw: String
    var modifier: TexyModifier? = TexyModifier("")

    /** How was link created?  */
    var type = Type.COMMON

    /** Optional label, used by references.  */
    var label: String? = null

    /** Reference name (if is stored as reference).  */
    var name: String? = null
    override fun toString(): String {
        val sb = StringBuilder("TexyLink{ raw: ").append(raw)
        if (label != null) sb.append(" label: ").append(label)
        if (name != null) sb.append(" name: ").append(name)
        if (name != null) sb.append(" type: ").append(type)
        if (name != null) sb.append(" mod: ").append(modifier)
        return sb.append(" }").toString()
    }

    public override fun clone(): TexyLink {
        val link = TexyLink(url)
        link.raw = raw
        link.modifier = modifier!!.clone()
        link.label = label
        link.type = type
        link.name = name
        return link
    }

    init {
        raw = url
    }

    /**
     * Returns textual representation of URL.
     * @param makeShorter  Replace parts of the url with "...".
     */
    fun asText(makeShorter: Boolean, obfuscateEmails: Boolean): String {

        // E-mail.
        if (obfuscateEmails && PAT_EMAIL.matcher(raw).matches()) {
            return raw!!.replace("@", "&#64;<!---->")
        }
        if (!makeShorter) return raw
        if (!PAT_URL.matcher(raw).matches()) return raw

        // It's URI and should be shortened.
        var raw_ = raw
        if (raw_!!.startsWith("www.")) raw_ = "none://$raw_"
        val uri: URI
        uri = try {
            URI(raw_)
        } catch (ex: URISyntaxException) {
            return raw_
        }
        val sb = StringBuilder()
        if ("" != uri.scheme && "none" != uri.scheme) sb.append(uri.scheme).append("://")
        sb.append(uri.host)
        appendShortened(uri.path, 16, '/', sb)
        appendShortened(uri.query, 4, '?', sb)
        if (uri.query.length == 0) appendShortened(uri.fragment, 4, '#', sb)
        return raw_ + sb.toString()
    } // asText()

    companion object {
        /** Returns a link from the string, or null if it's null or empty.  */
        fun fromString(url: String?): TexyLink? {
            return if (null == url || "" == url) null else TexyLink(url)
        }

        private val PAT_EMAIL = Pattern.compile("^" + RegexpPatterns.TEXY_EMAIL + "$", Pattern.UNICODE_CASE)
        private val PAT_URL = Pattern.compile("^(https?://|ftp://|www\\.|/)", Pattern.CASE_INSENSITIVE)

        /**
         * Shortens the string to the given max length, using UNICODE ellipsis.
         */
        private fun appendShortened(str: String, maxLen: Int, beginChar: Char, sb: StringBuilder) {
            val ELLIPSIS = "\u2026" // "\xe2\x80\xa6"
            val len = str.length
            if (0 == len) return
            if (len > maxLen) sb.append(beginChar).append(ELLIPSIS).append(str.substring(len - maxLen, len)) else sb.append(str)
        }
    }
}
