package cz.dynawest.jtexy

import org.dom4j.Element

/**
 * Texy content types: MARKUP, REPLACED, TEXTUAL, BLOCK.
 * @author Ondrej Zizka
 */
enum class ContentType(val delim: Char) {
    MARKUP('\u0017'), REPLACED('\u0016'), TEXTUAL('\u0015'), BLOCK('\u0014');

    /** Content type delimiter as String.  */
    val delimAsString: String

    init {
        delimAsString = charArrayOf(delim).concatToString()
    }

    companion object {
        /**
         * Get texy content type by name - starts with B, R, or T.
         */
        fun byName(name: String?): ContentType {
            if (null == name || "" == name) {
                return MARKUP
            }
            val c = name[0]
            if ('B' == c) {
                return BLOCK
            }
            if ('R' == c) {
                return REPLACED
            }
            return if ('T' == c) {
                TEXTUAL
            } else MARKUP
        }

        // TBD: Move to ProtectedHTMLWriter?
        fun fromElement(elm: Element): ContentType {
            // Cached?
            /*if( elm instanceof org.w3c.dom.Element ){
            // ContentType cType = (ContentType) ((DOMElement)elm)x.getUserData(JTexyConstants.DOM_USERDATA_CONTENT_TYPE); // Does not work - bug?
            org.w3c.dom.Element x = (org.w3c.dom.Element) elm;
            // TODO: Check type safety - we're getting
            // java.lang.AbstractMethodError: org.dom4j.dom.DOMElement.getUserData(Ljava/lang/String;)Ljava/lang/Object;
            ContentType cType = (ContentType) x.getUserData(JTexyConstants.DOM_USERDATA_CONTENT_TYPE);
            if( null != cType)  return cType;
            // TBD: Set somewhere.
            //elm.setUserData(JTexyConstants.DOM_USERDATA_CONTENT_TYPE, MARKUP, null);
		}*/
            val isReplaced = INLINE_ELEMENTS[elm.name] ?: return BLOCK

            // Not an inline element.
            // Replaced inline element (<object>, <img>, ..., <br>).
            return if (isReplaced) {
                REPLACED
            } else MARKUP
            // Normal inline element.
        }

        /**
         * %inline; elements; Replaced elements + br have value '1'/
         */
        private val INLINE_ELEMENTS: MutableMap<String, Boolean> = HashMap()

        init {
            INLINE_ELEMENTS["ins"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["del"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["tt"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["i"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["b"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["big"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["small"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["em"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["strong"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["dfn"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["code"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["samp"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["kbd"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["var"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["cite"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["abbr"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["acronym"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["sub"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["sup"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["q"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["span"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["bdo"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["a"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["object"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["img"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["br"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["script"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["map"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["input"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["select"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["textarea"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["label"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["button"] = java.lang.Boolean.TRUE

            // Transitional.
            INLINE_ELEMENTS["u"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["s"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["strike"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["font"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["applet"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["basefont"] = java.lang.Boolean.FALSE

            // Proprietary.
            INLINE_ELEMENTS["embed"] = java.lang.Boolean.TRUE
            INLINE_ELEMENTS["wbr"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["nobr"] = java.lang.Boolean.FALSE
            INLINE_ELEMENTS["canvas"] = java.lang.Boolean.TRUE
        }
    }
} // enum
