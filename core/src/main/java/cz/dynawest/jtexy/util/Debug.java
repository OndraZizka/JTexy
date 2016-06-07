
package cz.dynawest.jtexy.util;

import cz.dynawest.jtexy.ContentType;

/**
 *
 * @author Ondrej Zizka
 */
public class Debug {

    public static String showCodes( String str ){
        return str.replace(ContentType.BLOCK   .getDelim(), '▉')
                 .replace(ContentType.TEXTUAL .getDelim(), '₮')
                 .replace(ContentType.MARKUP  .getDelim(), 'ℳ')
                 .replace(ContentType.REPLACED.getDelim(), '℞');
    }
    
    public static String showCodesWithNumbers( String str ){
        str = str.replace(ContentType.BLOCK   .getDelim(), '▉')
                 .replace(ContentType.TEXTUAL .getDelim(), '₮')
                 .replace(ContentType.MARKUP  .getDelim(), 'ℳ')
                 .replace(ContentType.REPLACED.getDelim(), '℞');
        
        // TODO: Show numbers inside delimiters.
        
        return str;
    }
    
}// class
