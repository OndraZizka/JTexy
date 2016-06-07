package cz.dynawest.jtexy;

import java.util.HashMap;
import java.util.Map;
import org.dom4j.Element;

/**
 *  Texy content types: MARKUP, REPLACED, TEXTUAL, BLOCK.
 *  @author Ondrej Zizka
 */
public enum ContentType {

	MARKUP('\027'), REPLACED('\026'), TEXTUAL('\025'), BLOCK('\024');

    
	private final char delim;
    
    /** Content type delimiter as String. */
	private final String asString;

	public char getDelim() { return delim; }
	public String getDelimAsString() { return asString; }
	
    private ContentType(char delim) {
		this.delim = delim;
		this.asString = new String(new char[]{this.delim});
	}

    /**
     *  Get texy content type by name - starts with B, R, or T.
     */
	public static ContentType byName(String name) {
		if (null == name || "".equals(name)) {
			return MARKUP;
		}
		char c = name.charAt(0);
		if ('B' == c) {
			return BLOCK;
		}
		if ('R' == c) {
			return REPLACED;
		}
		if ('T' == c) {
			return TEXTUAL;
		}
		return MARKUP;
	}

	// TBD: Move to ProtectedHTMLWriter?
	public static ContentType fromElement(Element elm) {
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
        
		Boolean isReplaced = INLINE_ELEMENTS.get(elm.getName());

        // Not an inline element.
		if (null == isReplaced) {
			return BLOCK;
		}
        // Replaced inline element (<object>, <img>, ..., <br>).
		if (isReplaced) {
			return REPLACED;
		}
        // Normal inline element.
		return MARKUP;
	}
    
    
    
	/**
     *   %inline; elements; Replaced elements + br have value '1'/ 
     */
	private final static Map<String, Boolean> INLINE_ELEMENTS = new HashMap();

	static {
		INLINE_ELEMENTS.put("ins",      Boolean.FALSE);
		INLINE_ELEMENTS.put("del",      Boolean.FALSE);
		INLINE_ELEMENTS.put("tt",       Boolean.FALSE);
		INLINE_ELEMENTS.put("i",        Boolean.FALSE);
		INLINE_ELEMENTS.put("b",        Boolean.FALSE);
		INLINE_ELEMENTS.put("big",      Boolean.FALSE);
		INLINE_ELEMENTS.put("small",    Boolean.FALSE);
		INLINE_ELEMENTS.put("em",       Boolean.FALSE);
		INLINE_ELEMENTS.put("strong",   Boolean.FALSE);
		INLINE_ELEMENTS.put("dfn",      Boolean.FALSE);
		INLINE_ELEMENTS.put("code",     Boolean.FALSE);
		INLINE_ELEMENTS.put("samp",     Boolean.FALSE);
		INLINE_ELEMENTS.put("kbd",      Boolean.FALSE);
		INLINE_ELEMENTS.put("var",      Boolean.FALSE);
		INLINE_ELEMENTS.put("cite",     Boolean.FALSE);
		INLINE_ELEMENTS.put("abbr",     Boolean.FALSE);
		INLINE_ELEMENTS.put("acronym",  Boolean.FALSE);
		INLINE_ELEMENTS.put("sub",      Boolean.FALSE);
		INLINE_ELEMENTS.put("sup",      Boolean.FALSE);
		INLINE_ELEMENTS.put("q",        Boolean.FALSE);
		INLINE_ELEMENTS.put("span",     Boolean.FALSE);
		INLINE_ELEMENTS.put("bdo",      Boolean.FALSE);
		INLINE_ELEMENTS.put("a",        Boolean.FALSE);
		INLINE_ELEMENTS.put("object",   Boolean.TRUE);
		INLINE_ELEMENTS.put("img",      Boolean.TRUE);
		INLINE_ELEMENTS.put("br",       Boolean.TRUE);
		INLINE_ELEMENTS.put("script",   Boolean.TRUE);
		INLINE_ELEMENTS.put("map",      Boolean.FALSE);
		INLINE_ELEMENTS.put("input",    Boolean.TRUE);
		INLINE_ELEMENTS.put("select",   Boolean.TRUE);
		INLINE_ELEMENTS.put("textarea", Boolean.TRUE);
		INLINE_ELEMENTS.put("label",    Boolean.FALSE);
		INLINE_ELEMENTS.put("button",   Boolean.TRUE);

		// Transitional.
		INLINE_ELEMENTS.put("u",        Boolean.FALSE);
		INLINE_ELEMENTS.put("s",        Boolean.FALSE);
		INLINE_ELEMENTS.put("strike",   Boolean.FALSE);
		INLINE_ELEMENTS.put("font",     Boolean.FALSE);
		INLINE_ELEMENTS.put("applet",   Boolean.TRUE);
		INLINE_ELEMENTS.put("basefont", Boolean.FALSE);

		// Proprietary.
		INLINE_ELEMENTS.put("embed",    Boolean.TRUE);
		INLINE_ELEMENTS.put("wbr",      Boolean.FALSE);
		INLINE_ELEMENTS.put("nobr",     Boolean.FALSE);
		INLINE_ELEMENTS.put("canvas",   Boolean.TRUE);
	}

}// enum
