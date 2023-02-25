package cz.dynawest.jtexy;

/**
 *
 * @author Ondrej Zizka
 */
final public class Constants {

	// JTexy version.
	public static final String VERSION = "1.0-SNAPSHOT";


	// URL filters.
	public static final String FILTER_ANCHOR = "anchor";
	public static final String FILTER_IMAGE = "image";

	// Name of the elements to only hold the sub-elements (will not be rendered).
	public static final String HOLDER_ELEMENT_NAME = "holder";

	// DOM user data keys.
	public static final String DOM_USERDATA_CONTENT_TYPE = "JTexy:ct";


	

	// HTML minor-modes
	public static final int XML = 2;

	// HTML modes
	public enum HtmlModes {
        
		HTML4_TRANSITIONAL(0),
		HTML4_STRICT(1),
		HTML5(4),
		XHTML1_TRANSITIONAL(2),  // HTML4_TRANSITIONAL | XML;
		XHTML1_STRICT(3),        // HTML4_STRICT       | XML;
		XHTML5(6);               // HTML5              | XML;

        
        int val;
                
        private HtmlModes(int val) {
            this.val = val;
        }

        public int getVal() { return val; }
        public void setVal(int val) { this.val = val; }
	
    }// HtmlModes

}// interface




