package cz.dynawest.jtexy.modules

import cz.dynawest.openjdkregex.Pattern

/**
 * ListType types.
 *
 * @author Martin Večeřa
 */
internal enum class ListType(
    val type: String,
    val firstRegExp: String,
    val isOrdered: Boolean,
    val listStyleType: String?,
    val nextRegExp: String?
) {
    //             first-rexexp          ordered?  list-style-type   next-regexp
    STAR("*", "\\*\\ ", false, null, null), MINUS("-", "[\\u2013-](?![>-])", false, null, null), PLUS(
        "+",
        "\\+\\ ",
        false,
        null,
        null
    ),
    ARABDOT("1.", "1\\.\\ ",  /* not \\d !*/true, null, "\\d{1,3}\\.\\ "), ARABPAR("1)", "\\d{1,3}\\)\\ ", true, null, null), ROMANDOT(
        "I.",
        "I\\.\\ ",
        true,
        "upper-roman",
        "[IVX]{1,4}\\.\\ "
    ),
    ROMANPAR("I)", "[IVX]+\\)\\ ", true, "upper-roman", null),  // before A) !
    ALPHALO("a)", "[a-z]\\)\\ ", true, "lower-alpha", null), ALPHAUP("A)", "[A-Z]\\)\\ ", true, "upper-alpha", null);

    val pattern: Pattern

    init {
        pattern = Pattern.Companion.compile("^$firstRegExp", Pattern.Companion.UNICODE_CASE)
    }

    val nextOrFirstRegExp: String?
        get() = nextRegExp ?: firstRegExp
}
