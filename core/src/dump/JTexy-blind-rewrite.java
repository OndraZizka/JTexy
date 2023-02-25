package cz.dynawest.jtexy;

/**
 * Texy! - Convert plain text to XHTML format using {@link process()}.
 *
 * This was a try to rewrite the PHP classes blindly. No way.
 *
 * <code>
 *     $texy = new Texy();
 *     $html = $texy.process($text);
 * </code>
 *
 ** @author Ondrej Zizka
 * @copyright  Copyright (c) 2004, 2008 David Grudl
 * @package    Texy
 */
public class JTexy1 /*extends Nette::Object*/ {
    // configuration directives
    public static final boolean ALL = true;
    public static final boolean NONE = false;

    // Texy version
    public static final String VERSION = "0.0";
    public static final String REVISION = "1 released on 2008/08/20 22:03:00";

    // types of protection marks
    public static final String CONTENT_MARKUP = "\u0017";
    public static final String CONTENT_REPLACED = "\u0016";
    public static final String CONTENT_TEXTUAL = "\u0015";
    public static final String CONTENT_BLOCK = "\u0014";

    // url filters
    public static final String FILTER_ANCHOR = "anchor";
    public static final String FILTER_IMAGE = "image";

    // HTML minor-modes
    public static final String XML = 2;

    // HTML modes
    public enum HtmlMode {
        HTML4_TRANSITIONAL(0),
        HTML4_STRICT(1),
        HTML5(4),
        XHTML1_TRANSITIONAL(2), // JTexy.HTML4_TRANSITIONAL | JTexy.XML;
        XHTML1_STRICT(3), // JTexy.HTML4_STRICT | JTexy.XML;
        XHTML5(6), // JTexy.HTML5 | JTexy.XML;
    }

    /** @var string  input & output text encoding */
    public String encoding = "utf-8";

    /** @var array  Texy! syntax configuration */
    public Map<String, Boolean> allowed = new HashMap();

    /** @var TRUE|FALSE|array  Allowed HTML tags */
    public List<String> allowedTags;

    /** @var TRUE|FALSE|array  Allowed classes */
    public boolean allowedClasses = JTexy1.ALL; // all classes and id are allowed

    /** @var TRUE|FALSE|array  Allowed inline CSS style */
    public boolean allowedStyles = JTexy1.ALL;  // all inline styles are allowed

    /** @var int  TAB width (for converting tabs to spaces) */
    public int tabWidth = 8;

    /** @var boolean  Do obfuscate e-mail addresses? */
    public boolean obfuscateEmail = true;

    /** @var array  regexps to check URL schemes */
    public boolean urlSchemeFilters = null; // disable URL scheme filter

    /** @var bool  Paragraph merging mode */
    public boolean mergeLines = true;

    /** @var array  Parsing summary */
    public class ParsingSummary{
        public List images = new ArrayList();
        public List links = new ArrayList();
        public List preload = new ArrayList();
    }
    public ParsingSummary summary = new ParsingSummary();

    /** @var string  Generated stylesheet */
    public String styleSheet = "";

    /** @var array  CSS classes for align modifiers */
    public Map<String, String> alignClasses = new HashMap();
  /*(
		"left" => NULL,
		"right" => NULL,
		"center" => NULL,
		"justify" => NULL,
		"top" => NULL,
		"middle" => NULL,
		"bottom" => NULL,
	);/**/

    /** @var bool  remove soft hyphens (SHY)? */
    public boolean removeSoftHyphens = true;

    /** @var mixed */
    //public static advertisingNotice = "once";

    /** @var string */
    public String nontextParagraph = "div";

    /** @var TexyScriptModule */
    public Module scriptModule;

    /** @var TexyParagraphModule */
    public Module paragraphModule;

    /** @var TexyHtmlModule */
    public Module htmlModule;

    /** @var TexyImageModule */
    public Module imageModule;

    /** @var TexyLinkModule */
    public Module linkModule;

    /** @var TexyPhraseModule */
    public Module phraseModule;

    /** @var TexyEmoticonModule */
    public Module emoticonModule;

    /** @var TexyBlockModule */
    public Module blockModule;

    /** @var TexyHeadingModule */
    public Module headingModule;

    /** @var TexyHorizLineModule */
    public Module horizLineModule;

    /** @var TexyBlockQuoteModule */
    public Module blockQuoteModule;

    /** @var TexyListModule */
    public Module listModule;

    /** @var TexyTableModule */
    public Module tableModule;

    /** @var TexyFigureModule */
    public Module figureModule;

    /** @var TexyTypographyModule */
    public Module typographyModule;

    /** @var TexyLongWordsModule */
    public Module longWordsModule;

    /** @var TexyHtmlOutputModule */
    public Module htmlOutputModule;


    /**
     * Registered regexps and associated handlers for inline parsing.
     * @var array of ("handler" => callback
     *                "pattern" => regular expression)
     */
    private Map<String, PatternHandler> linePatterns = new HashMap();
    private $_linePatterns;

    /**
     * Registered regexps and associated handlers for block parsing.
     * @var array of ("handler" => callback
     *                "pattern" => regular expression)
     */
    private Map<String, PatternHandler> blockPatterns = new HashMap();
    private $_blockPatterns;

    /** @var array */
    private Map<String, PatternHandler> postHandlers = new HashMap();

    /** @var TexyHtml DOM structure for parsed text */
    private $DOM;

    /** @var array  Texy protect markup table */
    private $marks = array();

    /** @var array  for internal usage */
    public $_classes, $_styles;

    /** @var bool */
    private boolean processing;

    /** @var array of events and registered handlers */
    private $handlers = array();

    /**
     * DTD descriptor.
     *   $dtd[element][0] - allowed attributes (as array keys)
     *   $dtd[element][1] - allowed content for an element (content model) (as array keys)
     *                    - array of allowed elements (as keys)
     *                    - FALSE - empty element
     *                    - 0 - special case for ins & del
     * @var array
     */
    public $dtd;

    /** @var array */
    private static $dtdCache;

    /** @var int  HTML mode */
    private $mode;


    /** DEPRECATED */
    public static Object strictDTD;
    public Module cleaner;
    public Object xhtml;



    public JTexy1()
    {
        // load all modules
        this.loadModules();

        // DEPRECATED
        if (JTexy1.strictDTD != NULL) {
            this.setOutputMode(JTexy1.strictDTD ? JTexy1.HtmlMode.XHTML1_STRICT : JTexy1.HtmlMode.XHTML1_TRANSITIONAL);
        } else {
            this.setOutputMode(JTexy1.HtmlMode.XHTML1_TRANSITIONAL);
        }

        // DEPRECATED
        this.cleaner = this.htmlOutputModule;

        // examples of link references ;-)
        TexyLink link = new TexyLink("http://texy.info/");
        link.modifier.title = "The best text to HTML converter and formatter";
        link.label = "Texy!";
        this.linkModule.addReference("texy", $link);

        link = new TexyLink("http://www.google.com/search?q=%s");
        this.linkModule.addReference("google", $link);

        link = new TexyLink("http://en.wikipedia.org/wiki/Special:Search?search=%s");
        this.linkModule.addReference("wikipedia", $link);
    }



    /**
     * Set HTML/XHTML output mode (overwrites JTexy.allowedTags)
     * @param  int
     * @return void
     */
    public void setOutputMode(JTexy1.HtmlMode mode)
    {


		/*if (!isset(JTexy.dtdCache[$mode])) {
			require dirname(__FILE__) . "/libs/DTD.php";
			JTexy.dtdCache[$mode] = $dtd;
		}*/

        this.mode = $mode;
        this.dtd = JTexy1.dtdCache[$mode];
        TexyHtml::$xhtml = (boolean) ($mode & JTexy1.XML); // TODO: remove?

        // accept all valid HTML tags and attributes by default
        this.allowedTags = array();
        foreach (this.dtd as $tag => $dtd) {
        this.allowedTags[$tag] = JTexy1.ALL;
    }
    }



    /**
     * Get HTML/XHTML output mode
     * @return int
     */
    public JTexy1.HtmlMode getOutputMode()
    {
        return this.mode;
    }



    /**
     * Create array of all used modules (this.modules).
     * This array can be changed by overriding this method (by subclasses)
     */
    protected void loadModules()
    {
        // line parsing
        this.scriptModule = new TexyScriptModule(this);
        this.htmlModule = new TexyHtmlModule(this);
        this.imageModule = new TexyImageModule(this);
        this.phraseModule = new TexyPhraseModule(this);
        this.linkModule = new TexyLinkModule(this);
        this.emoticonModule = new TexyEmoticonModule(this);

        // block parsing
        this.paragraphModule = new TexyParagraphModule(this);
        this.blockModule = new TexyBlockModule(this);
        this.figureModule = new TexyFigureModule(this);
        this.horizLineModule = new TexyHorizLineModule(this);
        this.blockQuoteModule = new TexyBlockQuoteModule(this);
        this.tableModule = new TexyTableModule(this);
        this.headingModule = new TexyHeadingModule(this);
        this.listModule = new TexyListModule(this);

        // post process
        this.typographyModule = new TexyTypographyModule(this);
        this.longWordsModule = new TexyLongWordsModule(this);
        this.htmlOutputModule = new TexyHtmlOutputModule(this);
    }




    class PatternHandler{
        String name;
        Handler handler;
        Pattern pattern;

        public PatternHandler( String name, Handler handler, Pattern pattern ) {
            this.name = name;
            this.handler = handler;
            this.pattern = pattern;
        }

    }


    public final void registerLinePattern(Handler handler, Pattern pattern, String name )
    {
        if( !this.allowed.containsKey(name)) this.allowed.put(name, true);

        this.linePatterns.put(name, new PatternHandler( name, handler, pattern ));
    }



    public final void registerBlockPattern(Handler handler, Pattern pattern, String name )
    {
        // if (!preg_match("#(.)\^.*\$\\1[a-z]*#is", $pattern)) die("Texy: Not a block pattern $name");

        if( !this.allowed.containsKey(name)) this.allowed.put(name, true);

        this.blockPatterns.put(name, new PatternHandler( name, handler, pattern+"m" )); // force multiline
    }



    public final void registerPostLine( Handler handler, String name )
    {
        if( !this.allowed.containsKey(name)) this.allowed.put(name, true);

        this.postHandlers[$name] = $handler;
    }



    /**
     * Convert Texy! document in (X)HTML code.
     *
     * @param  string   input text
     * @param  bool     is block or single line?
     * @return string  output html code
     */
    public void process( String text, boolean singleLine ) throws TexyException
    {
        if (this.processing) {
            throw new InvalidStateException("Processing is in progress yet.");
        }

        // initialization
        this.marks = array();
        this.processing = true;

        // speed-up (d)
        if (is_array(this.allowedClasses)) this._classes = array_flip(this.allowedClasses);
        else this._classes = this.allowedClasses;

        if (is_array(this.allowedStyles)) this._styles = array_flip(this.allowedStyles);
        else this._styles = this.allowedStyles;

        // convert to UTF-8 (and check source encoding)
        text = TexyUtf::toUtf($text, this.encoding);

        if (this.removeSoftHyphens) {
            text = text.replace( "\u00C2", "" ).replace("\u00AD", "");
        }

        // standardize line endings and spaces
        text = JTexy1.normalize(text);

        // replace tabs with spaces
        this.tabWidth = Math.max(1, this.tabWidth);
        while( text.indexOf("\t") != -1 ){
            text = preg_replace_callback("#^(.*)\t#mU", array(this, "tabCb"), $text);
        }

        // user before handler
        this.invokeHandlers("beforeParse", array(this, text, singleLine));

        // select patterns
        this._linePatterns = this.linePatterns;
        this._blockPatterns = this.blockPatterns;
        foreach (this._linePatterns as $name => $foo) {
            if (empty(this.allowed[$name])) unset(this._linePatterns[$name]);
        }
        foreach (this._blockPatterns as $name => $foo) {
            if (empty(this.allowed[$name])) unset(this._blockPatterns[$name]);
        }

        // parse Texy! document into internal DOM structure
        this.DOM = TexyHtml::el();
        if ($singleLine) {
            this.DOM.parseLine(this, $text);
        } else {
            this.DOM.parseBlock(this, $text);
        }

        // user after handler
        this.invokeHandlers("afterParse", array(this, this.DOM, $singleLine));

        // converts internal DOM structure to final HTML code
        $html = this.DOM.toHtml(this);

        // this notice should remain
        if (JTexy1.advertisingNotice) {
            $html .= "\n<!-- by Texy2! -.";
            if (JTexy1.advertisingNotice === "once") {
                JTexy1.advertisingNotice = FALSE;
            }
        }

        this.processing = FALSE;

        return TexyUtf::utf2html($html, this.encoding);
    }



    /**
     * Makes only typographic corrections.
     * @param  string   input text
     * @return string  output code (in UTF!)
     */
    public String processTypo( String text )
    {
        // convert to UTF-8 (and check source encoding)
        text = TexyUtf::toUtf(text, this.encoding);

        // standardize line endings and spaces
        text = JTexy1.normalize(text);

        this.typographyModule.beforeParse(this, text);
        text = this.typographyModule.postLine(text);

        return TexyUtf::utf2html(text, this.encoding);
    }



    /**
     * Converts DOM structure to pure text.
     * @return string
     */
    public String toText()
    {
        if (!this.DOM) {
            throw new InvalidStateException("Call $texy.process() first.");
        }

        return TexyUtf::utfTo(this.DOM.toText(this), this.encoding);
    }



    /**
     * Converts internal string representation to final HTML code in UTF-8.
     * @return string
     */
    public final String stringToHtml( String s)
    {
        // decode HTML entities to UTF-8
        s = JTexy1.unescapeHtml(s);

        // line-postprocessing
        String[] blocks = s.split(JTexy1.CONTENT_BLOCK);
        foreach (this.postHandlers as $name => $handler) {
        if (empty(this.allowed[$name])) continue;
        foreach ($blocks as $n => $s) {
            if ($n % 2 === 0 && $s !== "") {
                $blocks[$n] = call_user_func($handler, $s);
            }
        }
    }
        s = HelperFunctions.join( blocks, JTexy1.CONTENT_BLOCK);

        // encode < > &
        s = JTexy1.escapeHtml(s);

        // replace protected marks
        s = this.unProtect(s);

        // wellform and reformat HTML
        this.invokeHandlers("postProcess", array(this, s));

        // unfreeze spaces
        s = JTexy1.unfreezeSpaces(s);

        return $s;
    }



    /**
     * Converts internal string representation to final HTML code in UTF-8.
     * @return string
     */
    final public function stringToText($s)
    {
        $save = this.htmlOutputModule.lineWrap;
        this.htmlOutputModule.lineWrap = FALSE;
        $s = this.stringToHtml( $s );
        this.htmlOutputModule.lineWrap = $save;

        // remove tags
        $s = preg_replace("#<(script|style)(.*)</\\1>#Uis", "", $s);
        $s = strip_tags($s);
        $s = preg_replace("#\n\s*\n\s*\n[\n\s]*\n#", "\n\n", $s);

        // entities . chars
        $s = JTexy1.unescapeHtml($s);

        // convert nbsp to normal space and remove shy
        $s = strtr($s, array(
                        "\xC2\xAD" => "",  // shy
                "\xC2\xA0" => " ", // nbsp
		));

        return $s;
    }



    /**
     * Add new event handler.
     *
     * @param  string   event name
     * @param  callback
     * @return void
     */
    final public function addHandler($event, $callback)
    {
        if (!is_callable($callback)) {
            throw new InvalidArgumentException("Invalid callback.");
        }

        this.handlers[$event][] = $callback;
    }



    /**
     * Invoke registered around-handlers.
     *
     * @param  string   event name
     * @param  TexyParser  actual parser object
     * @param  array    arguments passed into handler
     * @return mixed
     */
    final public function invokeAroundHandlers($event, $parser, $args)
    {
        if (!isset(this.handlers[$event])) return FALSE;

        $invocation = new TexyHandlerInvocation(this.handlers[$event], $parser, $args);
        $res = $invocation.proceed();
        $invocation.free();
        return $res;
    }



    /**
     * Invoke registered after-handlers.
     *
     * @param  string   event name
     * @param  array    arguments passed into handler
     * @return void
     */
    final public function invokeHandlers($event, $args)
    {
        if (!isset(this.handlers[$event])) return;

        foreach (this.handlers[$event] as $handler) {
        call_user_func_array($handler, $args);
    }
    }



    /**
     * Translate all white spaces (\t \n \r space) to meta-spaces \x01-\x04.
     * which are ignored by TexyHtmlOutputModule routine
     * @param  string
     * @return string
     */
    final public static function freezeSpaces($s)
    {
        return strtr($s, " \t\r\n", "\x01\x02\x03\x04");
    }



    /**
     * Reverts meta-spaces back to normal spaces.
     * @param  string
     * @return string
     */
    final public static function unfreezeSpaces($s)
    {
        return strtr($s, "\x01\x02\x03\x04", " \t\r\n");
    }



    /**
     * Removes special controls characters and normalizes line endings and spaces.
     * @param  string
     * @return string
     */
    final public static function normalize($s)
    {
        // standardize line endings to unix-like
        $s = str_replace("\r\n", "\n", $s); // DOS
        $s = strtr($s, "\r", "\n"); // Mac

        // remove special chars; leave \t + \n
        $s = preg_replace("#[\x00-\x08\x0B-\x1F]+#", "", $s);

        // right trim
        $s = preg_replace("#[\t ]+$#m", "", $s);

        // trailing spaces
        $s = trim($s, "\n");

        return $s;
    }



    /**
     * Converts to web safe characters [a-z0-9-] text.
     * @param  string
     * @param  string
     * @return string
     */
    final public static function webalize($s, $charlist = NULL)
    {
        $s = TexyUtf::utf2ascii($s);
        $s = strtolower($s);
        if ($charlist) $charlist = preg_quote($charlist, "#");
        $s = preg_replace("#[^a-z0-9".$charlist."]+#", "-", $s);
        $s = trim($s, "-");
        return $s;
    }



    /**
     * Texy! version of htmlSpecialChars (much faster than htmlSpecialChars!).
     * note: &quot; is not encoded!
     * @param  string
     * @return string
     */
    final public static function escapeHtml($s)
    {
        return str_replace(array("&", "<", ">"), array("&amp;", "&lt;", "&gt;"), $s);
    }



    /**
     * Texy! version of html_entity_decode (always UTF-8, much faster than original!).
     * @param  string
     * @return string
     */
    final public static function unescapeHtml($s)
    {
        if (strpos($s, "&") === FALSE) return $s;
        return html_entity_decode($s, ENT_QUOTES, "UTF-8");
    }



    /**
     * Outdents text block.
     * @param  string
     * @return string
     */
    final public static function outdent($s)
    {
        $s = trim($s, "\n");
        $spaces = strspn($s, " ");
        if ($spaces) return preg_replace("#^ {1,$spaces}#m", "", $s);
        return $s;
    }



    /**
     * Generate unique mark - useful for freezing (folding) some substrings.
     * @param  string   any string to froze
     * @param  int      JTexy.CONTENT_* constant
     * @return string  internal mark
     */
    final public function protect($child, $contentType)
    {
        if ($child==="") return "";

        $key = $contentType
                . strtr(base_convert(count(this.marks), 10, 8), "01234567", "\x18\x19\x1A\x1B\x1C\x1D\x1E\x1F")
                . $contentType;

        this.marks[$key] = $child;

        return $key;
    }



    final public function unProtect($html)
    {
        return strtr($html, this.marks);
    }



    /**
     * Filters bad URLs.
     * @param  string   user URL
     * @param  string   type: a-anchor, i-image, c-cite
     * @return bool
     */
    final public function checkURL($URL, $type)
    {
        // absolute URL with scheme? check scheme!
        if (!empty(this.urlSchemeFilters[$type])
                && preg_match("#".TEXY_URLSCHEME."#A", $URL)
                && !preg_match(this.urlSchemeFilters[$type], $URL))
            return FALSE;

        return TRUE;
    }



    /**
     * Is given URL relative?
     * @param  string  URL
     * @return bool
     */
    final public static function isRelative($URL)
    {
        // check for scheme, or absolute path, or absolute URL
        return !preg_match("#".TEXY_URLSCHEME."|[\#/?]#A", $URL);
    }



    /**
     * Prepends root to URL, if possible.
     * @param  string  URL
     * @param  string  root
     * @return string
     */
    final public static function prependRoot($URL, $root)
    {
        if ($root == NULL || !JTexy1.isRelative($URL)) return $URL;
        return rtrim($root, "/\\") . "/" . $URL;
    }

    final public function getLinePatterns()
    {
        return this._linePatterns;
    }
    final public function getBlockPatterns()
    {
        return this._blockPatterns;
    }
    final public function getDOM()
    {
        return this.DOM;
    }

    private String tabCb($m)
    {
        StringBuilder sb = new StringBuilder();

        return $m[1] + str_repeat(" ", this.tabWidth - strlen($m[1]) % this.tabWidth);
    }


    /**
     * PHP garbage collector helper.
     */
    final public function free()
    {
        foreach (array_keys(get_object_vars(this)) as $key)
        this.$key = NULL;
    }



    final public function __clone()
    {
        throw new NotSupportedException("Clone is not supported.");
    }

}
