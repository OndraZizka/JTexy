
package cz.dynawest.jtexy;

/**
 * Regular expression tokens to be re-used in module's patterns (currently in .properties files)
 *
 * @author Ondrej Zizka
 */
public interface RegexpPatterns {


    // Unicode character classes
    public static final String TEXY_CHAR = "A-Za-z\\xC0-\\u02FF\\u0370-\\u1EFF";

    // marking meta-characters
    // any mark:               \x14-\x1F
    // CONTENT_MARKUP mark:    \x17-\x1F
    // CONTENT_REPLACED mark:  \x16-\x1F
    // CONTENT_TEXTUAL mark:   \x15-\x1F
    // CONTENT_BLOCK mark:     \x14-\x1F
    public static final String TEXY_MARK = "\\x14-\\x1F";


    // modifier .(title)[class]{style}
    public static final String TEXY_MODIFIER = "(?: *(?<= |^)\\.((?:\\([^)\\n]+\\)|\\[[^\\]\\n]+\\]|\\{[^}\\n]+\\}){1,3}?))";

    // modifier .(title)[class]{style}<>
    public static final String TEXY_MODIFIER_H= "(?: *(?<= |^)\\.((?:\\([^)\\n]+\\)|\\[[^\\]\\n]+\\]|\\{[^}\\n]+\\}|<>|>|=|<){1,4}?))";

    // modifier .(title)[class]{style}<>^
    public static final String TEXY_MODIFIER_HV= "(?: *(?<= |^)\\.((?:\\([^)\\n]+\\)|\\[[^\\]\\n]+\\]|\\{[^}\\n]+\\}|<>|>|=|<|\\^|\\-|\\_){1,5}?))";



    // images   [* urls .(title)[class]{style} >]
    public static final String TEXY_IMAGE= "\\[\\*([^\\n"+TEXY_MARK+"]+)"+TEXY_MODIFIER+"? *(\\*|>|<)\\]";


    // links
                                                //  [reference]    or       non-WS or Texy marks followed by non- :).,!?
    public static final String TEXY_LINK_URL  = "(?:\\[[^\\]\\n]+\\]|(?!\\[)[^\\s"+TEXY_MARK+"]*[^:);,.!?\\s"+TEXY_MARK+"])";
    // Unlike Texy's *?, JTexy uses `...TEXY_MARK+"]*[...`, otherwise only first char of URL would match.
    public static final String TEXY_LINK      = "(?::("+TEXY_LINK_URL+"))";       // any link
    public static final String TEXY_LINK_N    = "(?::("+TEXY_LINK_URL+"|:))";     // any link (also unstated)

    /*
     * In UTF-8 mode, "\x{...}" is allowed, where the contents of the braces
     * is a string of hexadecimal digits. It is interpreted as a UTF-8 character
     * whose code number is the given hexadecimal number.
     * The original hexadecimal escape sequence, \xhh, matches a two-byte
     * UTF-8 character if the value is greater than 127.
     */
    //public static final String TEXY_EMAIL   = "[A-Za-z0-9.+_-]{1,64}@[0-9.+_"+TEXY_CHAR+"\\x{ad}-]{1,252}\\.[a-z]{2,6}";    // name@exaple.com
    public static final String TEXY_EMAIL     = "[A-Za-z0-9.+_-]{1,64}@[0-9.+_"+TEXY_CHAR+"\\xAD-]{1,252}\\.[a-z]{2,6}";    // name@exaple.com
    public static final String TEXY_URLSCHEME = "[a-z][a-z0-9+.-]*:";    // http:  |  mailto:


}
