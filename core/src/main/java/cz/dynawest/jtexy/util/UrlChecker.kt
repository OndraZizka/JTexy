package cz.dynawest.jtexy.util

import cz.dynawest.jtexy.RegexpPatterns
import java.util.logging.*

/**
 *
 * @author Ondrej Zizka
 */
object UrlChecker {
    private val log = Logger.getLogger(UrlChecker::class.java.name)

    /*
 		$texy->urlSchemeFilters[Texy::FILTER_ANCHOR] = '#https?:|ftp:|mailto:#A';
		$texy->urlSchemeFilters[Texy::FILTER_IMAGE] = '#https?:#A';
*/
    internal fun getSchemeByType(type: Type?): String? {
        return when (type) {
            Type.ANCHOR -> "https?:|ftp:|mailto:"
            Type.IMAGE -> "https?:"
            else -> null
        }
    }

    /**
     * Filters bad URLs.
     * @param  string   user URL
     * @param  string   type: a-anchor, i-image, c-cite
     */
    fun checkURL(url: String?, type: Type?): Boolean {
        val scheme = getSchemeByType(type)

        // Absolute URL with scheme? Check against the scheme!
        return if (null != scheme && url!!.matches(( /*"(?A)"*/"^" + RegexpPatterns.TEXY_URLSCHEME).toRegex())
            && !url.matches(scheme.toRegex())
        ) false else true
    }

    enum class Type {
        ANCHOR, IMAGE
    }
}
