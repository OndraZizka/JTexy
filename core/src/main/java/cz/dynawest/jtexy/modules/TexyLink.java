
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpPatterns;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 *
 * @author Ondrej Zizka
 */
public class TexyLink implements Cloneable {

	public enum Type { COMMON, BRACKET, IMAGE };


	/** URL in resolved form. */
	public String url;

	/** string  URL as written in text */
	public String raw;

	public TexyModifier modifier = new TexyModifier("");

	/** How was link created? */
	public Type type = Type.COMMON;

	/** Optional label, used by references. */
	public String label;

	/** Reference name (if is stored as reference). */
	public String name;


	@Override	public String toString() {
        StringBuilder sb = new StringBuilder("TexyLink{ raw: ").append(raw);
        if( label != null )  sb.append(" label: ").append(label);
        if( name  != null )  sb.append(" name: ").append(name);
        if( name  != null )  sb.append(" type: ").append(type);
        if( name  != null )  sb.append(" mod: ").append(modifier);
		return sb.append(" }").toString();
	}




	/** Returns a link from the string, or null if it's null or empty. */
	public static TexyLink fromString(String url) {
		if( null == url || "".equals(url) ) return null;
		return new TexyLink(url);
	}


	public TexyLink(String url) {
		this.url = url;
		this.raw = url;
	}


	@Override
	public TexyLink clone() {
		TexyLink link = new TexyLink(url);
		link.raw = this.raw;
		link.modifier = this.modifier.clone();
		link.label = this.label;
		link.type = this.type;
		link.name = this.name;
		return link;
	}





	private final static Pattern PAT_EMAIL = Pattern.compile("^"+RegexpPatterns.TEXY_EMAIL+"$", Pattern.UNICODE_CASE);
	private final static Pattern PAT_URL   = Pattern.compile("^(https?://|ftp://|www\\.|/)", Pattern.CASE_INSENSITIVE);


	/**
	 * Returns textual representation of URL.
	 * @param makeShorter  Replace parts of the url with "...".
	 */
	public String asText( boolean makeShorter, boolean obfuscateEmails ){

		// E-mail.
		if( obfuscateEmails && PAT_EMAIL.matcher(this.raw).matches() ) {
			return this.raw.replace("@", "&#64;<!---->");
		}

		if( ! makeShorter )
			return this.raw;

		if( ! PAT_URL.matcher( this.raw ).matches() )
			return this.raw;

		// It's URI and should be shortened.
		String raw_ = this.raw;

		if( raw_.startsWith("www.") )
			raw_ = "none://" + raw_;

		URI uri;
		try { uri = new URI(raw_); }
		catch( URISyntaxException ex ){ return raw_; }

		StringBuilder sb = new StringBuilder();

		if( !"".equals(uri.getScheme())  &&  !"none".equals(uri.getScheme()) )
			sb.append( uri.getScheme() ).append("://");

		sb.append( uri.getHost() );

		appendShortened( uri.getPath(), 16, '/', sb );
		appendShortened( uri.getQuery(), 4, '?', sb );
		if( uri.getQuery().length() == 0 )
			appendShortened( uri.getFragment(), 4, '#', sb );

		return raw_ + sb.toString();
	}// asText()



	/**
	 * Shortens the string to the given max length, using UNICODE ellipsis.
	 */
	private static void appendShortened( String str, int maxLen, char beginChar, StringBuilder sb ){
		final String ELLIPSIS = "\u2026"; // "\xe2\x80\xa6"
		int len = str.length();
		if( 0 == len ) return;
		if( len > maxLen )
			sb.append( beginChar ).append( ELLIPSIS ).append( str.substring(len-maxLen, len) );
		else
			sb.append( str );
	}




}// class
