package cz.dynawest.jtexy;

import java.util.*;


/**
 * Should be faster than map-based protector.
 * TODO: Get rid of the map-based one, or refactor to share the common code.
 *
 * @author Ondrej Zizka
 */
public class ProtectorArray extends Protector {

	// HashMap of stored (protected) strings.
	private List<String> safe = new LinkedList();


	/**
	 * Stores the given string and returns a key under which it's stored.
	 */
	@Override
	public String protect( String str, ContentType type ){
		int curSize = safe.size();
		String key = Utils.intToKey( curSize );
		safe.add( /*curSize,*/ str );
		String typeStr = type.getDelimAsString();
		return typeStr + key + typeStr;
	}
    
	public String protect( String str ){ return protect(str, ContentType.MARKUP); }
    

	


    /**
     * @param encodedKey  Encoded ID, i.e. without delimiters.
    @Override
    protected String getStringByEncodedID( String encodedKey ){
        return this.safe.get(Utils.texyMarkupToInt(encodedKey));
    }
     */
	
    /**
     * @param wholeKey  Surrounded by delimiters.
     * TODO: Re-use getStringByEncodedID()?
     */
    @Override
	public String unprotect( String wholeKey ){
		int index = Utils.keyToInt( wholeKey );
		return this.safe.get( index );
	}




}
