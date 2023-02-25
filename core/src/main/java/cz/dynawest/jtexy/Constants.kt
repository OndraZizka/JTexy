package cz.dynawest.jtexy

/**
 *
 * @author Ondrej Zizka
 */
object Constants {
    // JTexy version.
    const val VERSION = "1.0-SNAPSHOT"

    // URL filters.
    const val FILTER_ANCHOR = "anchor"
    const val FILTER_IMAGE = "image"

    // Name of the elements to only hold the sub-elements (will not be rendered).
    const val HOLDER_ELEMENT_NAME = "holder"

    // DOM user data keys.
    const val DOM_USERDATA_CONTENT_TYPE = "JTexy:ct"

    // HTML minor-modes
    const val XML = 2

    // HTML modes
    enum class HtmlModes( // HTML5              | XML;
        var `val`: Int
    ) {
        HTML4_TRANSITIONAL(0), HTML4_STRICT(1), HTML5(4), XHTML1_TRANSITIONAL(2),  // HTML4_TRANSITIONAL | XML;
        XHTML1_STRICT(3),  // HTML4_STRICT       | XML;
        XHTML5(6)
    } // HtmlModes
} // interface
