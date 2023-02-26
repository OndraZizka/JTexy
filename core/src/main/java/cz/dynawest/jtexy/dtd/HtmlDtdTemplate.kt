package cz.dynawest.jtexy.dtd

/**
 * Default HTML DTD.
 *
 * @author Ondrej Zizka
 */
class HtmlDtdTemplate {
    var dtd = Dtd("html")
    val rootDtdElement = dtd.rootElement

    internal class AttributeGroups {
        var coreAttrs: MutableSet<DtdAttr> = HashSet()
        var i18n: MutableSet<DtdAttr> = HashSet()
        var attrs: MutableSet<DtdAttr> = HashSet()
        var cellalign: MutableSet<DtdAttr> = HashSet()
    }

    private val attrGroups = AttributeGroups()
    private val blockElms: MutableSet<DtdElement> = HashSet()
    private val inlineElms: MutableSet<DtdElement> = HashSet()
    private val blockInlineElms: MutableSet<DtdElement> = HashSet()
    // Moved from TexyHtml.php
    /** Empty elements.  */
    var emptyElements: MutableSet<DtdElement> = HashSet()
    /** %inline; elements; replaced elements + br have value '1'.  */ //public static Set<DtdElement> inlineElements = new HashSet();
    /** Elements with optional end tag in HTML.  */
    var optionalEndElements: MutableSet<DtdElement> = HashSet()

    init {
        init()
    }

    /**
     * Creates the DTD structure.
     */
    private fun init() {
        val strict = false

        // Attributes
        attrGroups.coreAttrs.addAll(Dtd.createAttributes("id class style title xml:id"))
        attrGroups.i18n.addAll(Dtd.createAttributes("lang dir xml:lang"))
        attrGroups.attrs.addAll(attrGroups.coreAttrs)
        attrGroups.attrs.addAll(attrGroups.i18n)
        attrGroups.attrs.addAll(Dtd.createAttributes("onclick ondblclick onmousedown onmouseup onmouseover onmousemove onmouseout onkeypress onkeydown onkeyup"))
        attrGroups.cellalign.addAll(attrGroups.attrs)
        attrGroups.cellalign.addAll(Dtd.createAttributes("align char charoff valign"))


        // Content elements.

        // %block;
        blockElms.addAll(dtd.getOrCreateElements("ins del p h1 h2 h3 h4 h5 h6 ul ol dl pre div blockquote noscript noframes form hr table address fieldset"))
        if (!strict) blockElms.addAll(dtd.getOrCreateElements("dir menu center iframe isindex" + " marquee")) // transitional + proprietary


        // %inline;
        inlineElms.addAll(dtd.getOrCreateElements("ins del tt i b big small em strong dfn code samp kbd var cite abbr acronym sub sup q span bdo a object img br script map input select textarea label button %DATA"))
        if (!strict) inlineElms.addAll(dtd.getOrCreateElements("u s strike font applet basefont" + " embed wbr nobr canvas")) // transitional + proprietary
        blockInlineElms.addAll(blockElms)
        blockInlineElms.addAll(inlineElms)

        // Build DTD.
        var tmp: DtdElement
        rootDtdElement!!.add(
            dtd.getOrCreate("html")
                .addAllAttrs(attrGroups.i18n).addAll("version " + if (strict) "xmlns" else "")
                .addAll("head body")
        )
        rootDtdElement.add(
            dtd.getOrCreate("head")
                .addAllAttrs(attrGroups.i18n).addAllAttrs("profile")
                .addAll("title script style base meta link object isindex")
        )
        rootDtdElement.add(
            dtd.getOrCreate("title")
                .addAll("%DATA")
        )
        rootDtdElement.add(dtd.getOrCreate("body")
            .addAllAttrs(attrGroups.attrs).addAllAttrs("onload onunload").also {
                tmp = it
            }
        )
        if (strict) {
            tmp.addAll("script")
            tmp.addAll(blockElms)
        } else tmp.addAll(blockInlineElms)
        rootDtdElement.add(
            dtd.getOrCreate("script")
                .addAllAttrs("charset type src defer event for")
                .addAll("%DATA")
        )
        rootDtdElement.add(
            dtd.getOrCreate("style")
                .addAllAttrs(attrGroups.i18n).addAllAttrs("type media title")
                .addAll("%DATA")
        )
        rootDtdElement.add(
            dtd.getOrCreate("p")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("h1")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("h2")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("h3")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("h4")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("h5")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("h6")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("ul")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("type compact", !strict)
                .addAll("li")
        )
        rootDtdElement.add(
            dtd.getOrCreate("ol")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("type compact start", !strict)
                .addAll("li")
        )
        rootDtdElement.add(
            dtd.getOrCreate("li")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("type value", !strict)
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("dl")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("compact", !strict)
                .addAll("dt dd")
        )
        rootDtdElement.add(
            dtd.getOrCreate("dt")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("dd")
                .addAllAttrs(attrGroups.attrs)
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("pre")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("width", !strict) // %inline minus those listed.
                .addAll(inlineElms)
                .removeAll("img object applet big small sub sup font basefont")
        )
        rootDtdElement.add(
            dtd.getOrCreate("div")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(dtd.getOrCreate("blockquote")
            .addAllAttrs(attrGroups.attrs).addAllAttrs("cite").also {
                tmp = it
            }
        )
        if (strict) {
            tmp.addAll("script")
            tmp.addAll(blockElms)
        } else tmp.addAll(blockInlineElms)
        rootDtdElement.add(
            dtd.getOrCreate("noscript")
                .addAllAttrs(attrGroups.attrs)
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(dtd.getOrCreate("form")
            .addAllAttrs(attrGroups.attrs).addAllAttrs("action method enctype accept name onsubmit onreset accept-charset").also {
                tmp = it
            }
        )
        if (strict) {
            tmp.addAll("script")
            tmp.addAll(blockElms)
        } else tmp.addAll(blockInlineElms)
        rootDtdElement.add(
            dtd.getOrCreate("table")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("summary width border frame rules cellspacing cellpadding datapagesize")
                .addAll("caption colgroup col thead tbody tfoot tr")
        )
        rootDtdElement.add(
            dtd.getOrCreate("caption")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("colgroup")
                .addAllAttrs(attrGroups.cellalign).addAllAttrs("span width")
                .addAll("col")
        )
        rootDtdElement.add(
            dtd.getOrCreate("thead")
                .addAllAttrs(attrGroups.cellalign)
                .addAll("tr")
        )
        rootDtdElement.add(
            dtd.getOrCreate("tbody")
                .addAllAttrs(attrGroups.cellalign)
                .addAll("tr")
        )
        rootDtdElement.add(
            dtd.getOrCreate("tfoot")
                .addAllAttrs(attrGroups.cellalign)
                .addAll("tr")
        )
        rootDtdElement.add(
            dtd.getOrCreate("tr")
                .addAllAttrs(attrGroups.cellalign)
                .addAll("td th")
        )
        if (!strict) tmp.addAllAttrs("bgcolor")
        rootDtdElement.add(
            dtd.getOrCreate("td")
                .addAllAttrs(attrGroups.cellalign).addAllAttrs("abbr axis headers scope rowspan colspan")
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("th")
                .addAllAttrs(attrGroups.cellalign).addAllAttrs("abbr axis headers scope rowspan colspan")
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("address")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms).addAll(if (strict) "" else "p")
        )
        rootDtdElement.add(
            dtd.getOrCreate("fieldset")
                .addAllAttrs(attrGroups.attrs)
                .addAll("legend").addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("legend")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("accesskey").addAllAttrsIf("align", !strict)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("tt")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("i")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("b")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("big")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("small")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("em")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("strong")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("dfn")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("code")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("samp")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("kbd")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("var")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("cite")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("abbr")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("acronym")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("sub")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("sup")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("q")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("cite")
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("span")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("bdo")
                .addAllAttrs(attrGroups.coreAttrs).addAllAttrs("lang dir")
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("a")
                .addAllAttrs(attrGroups.attrs)
                .addAllAttrs("charset type name href hreflang rel rev accesskey shape coords tabindex onfocus onblur")
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("object")
                .addAllAttrs(attrGroups.attrs)
                .addAllAttrs("declare classid codebase data type codetype archive standby height width usemap name tabindex")
                .addAll("param").addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("map")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("name")
                .addAll("area").addAll(blockElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("select")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("name size multiple disabled tabindex onfocus onblur onchange")
                .addAll("option optgroup")
        )
        rootDtdElement.add(
            dtd.getOrCreate("optgroup")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("disabled label")
                .addAll("option")
        )
        rootDtdElement.add(
            dtd.getOrCreate("option")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("selected disabled label value")
                .addAll("%DATA")
        )
        rootDtdElement.add(
            dtd.getOrCreate("textarea")
                .addAllAttrs(attrGroups.attrs)
                .addAllAttrs("name rows cols disabled readonly tabindex accesskey onfocus onblur onselect onchange")
                .addAll("%DATA")
        )
        rootDtdElement.add(
            dtd.getOrCreate("label")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("for accesskey onfocus onblur")
                .addAll(inlineElms) // - label by TexyHtml::$prohibits
        )
        rootDtdElement.add(
            dtd.getOrCreate("button")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("name value type disabled tabindex accesskey onfocus onblur")
                .addAll(blockInlineElms) // - a input select textarea label button form fieldset, by TexyHtml::$prohibits
        )
        rootDtdElement.add(
            dtd.getOrCreate("ins")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("cite datetime") // @ 0, // special case
        )
        rootDtdElement.add(
            dtd.getOrCreate("del")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("cite datetime") // @ 0, // special case
        )

        // empty elements
        rootDtdElement.add(
            dtd.getOrCreate("img")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("src alt longdesc name height width usemap ismap") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("hr")
                .addAllAttrs(attrGroups.attrs).addAllAttrsIf("align noshade size width", !strict) // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("br")
                .addAllAttrs(attrGroups.coreAttrs).addAllAttrsIf("clear", !strict) // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("input")
                .addAllAttrs(attrGroups.attrs)
                .addAllAttrs("type name value checked disabled readonly size maxlength src alt usemap ismap tabindex accesskey onfocus onblur onselect onchange accept") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("meta")
                .addAllAttrs(attrGroups.i18n).addAllAttrs("http-equiv name content scheme") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("area")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("shape coords href nohref alt tabindex accesskey onfocus onblur") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("base")
                .addAllAttrs(if (strict) "href" else "href target") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("col")
                .addAllAttrs(attrGroups.cellalign).addAllAttrs("span width") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("link")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("charset href hreflang type rel rev media") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("param")
                .addAllAttrs("id name value valuetype type") // @ FALSE,
        )

        // special "base content"
        rootDtdElement.add(
            dtd.getOrCreate("%BASE") // @ NULL,
                .addAll("html head body script").addAll(blockInlineElms)
        )


        // Added from TexyHtml.php:
        /** Empty elements.  */
        emptyElements.addAll(dtd.getOrCreateElements("img hr br input meta area base col link param basefont frame isindex wbr embed"))
        /** @var array  %inline; elements; replaced elements + br have value '1'
         */
        /*inlineElements.addAll("ins del tt i b big small em strong dfn code samp kbd var cite"
                + " abbr acronym sub sup q span bdo a object img br script map input select textarea label button"
                + " u s strike font applet basefont", // transitional
                + " embed wbr nobr canvas" // proprietary
        );/ **/
        /** @var array  elements with optional end tag in HTML
         */
        optionalEndElements.addAll(dtd.getOrCreateElements("body head html colgroup dd dt li option p tbody td tfoot th thead tr"))
        /** @see http://www.w3.org/TR/xhtml1/prohibitions.html
         */
        dtd.prohibit("a", "a button")
        dtd.prohibit("img", "pre")
        dtd.prohibit("object", "pre")
        dtd.prohibit("big", "pre")
        dtd.prohibit("small", "pre")
        dtd.prohibit("sub", "pre")
        dtd.prohibit("sup", "pre")
        dtd.prohibit("input", "button")
        dtd.prohibit("select", "button")
        dtd.prohibit("textarea", "button")
        dtd.prohibit("label", "button label")
        dtd.prohibit("button", "button")
        dtd.prohibit("form", "button form")
        dtd.prohibit("fieldset", "button")
        dtd.prohibit("iframe", "button")
        dtd.prohibit("isindex", "button")
        if (strict) return


        // ========== LOOSE DTD ============

        // transitional
        rootDtdElement.add(
            dtd.getOrCreate("dir")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("compact")
                .addAll("li")
        )
        rootDtdElement.add(
            dtd.getOrCreate("menu")
                .addAllAttrs(attrGroups.attrs).addAllAttrs("compact")
                .addAll("li") // it"s inline-li, ignored
        )
        rootDtdElement.add(
            dtd.getOrCreate("center")
                .addAllAttrs(attrGroups.attrs)
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("iframe")
                .addAllAttrs(attrGroups.coreAttrs)
                .addAllAttrs("longdesc name src frameborder marginwidth marginheight scrolling align height width")
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("noframes")
                .addAllAttrs(attrGroups.attrs)
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("u")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("s")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("strike")
                .addAllAttrs(attrGroups.attrs)
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("font")
                .addAllAttrs(attrGroups.coreAttrs).addAllAttrs(attrGroups.i18n).addAllAttrs("size color face")
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("applet")
                .addAllAttrs(attrGroups.coreAttrs).addAllAttrs("codebase archive code object alt name width height align hspace vspace")
                .addAll("param").addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("basefont")
                .addAllAttrs("id size color face") // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("isindex")
                .addAllAttrs(attrGroups.coreAttrs).addAllAttrs(attrGroups.i18n).addAllAttrs("prompt") // @ FALSE,
        )

        // proprietary
        rootDtdElement.add(
            dtd.getOrCreate("marquee") // @ Texy::ALL,
                .addAll(blockInlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("nobr") // No attrs.
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("canvas") // @ Texy::ALL,
                .addAll(inlineElms)
        )
        rootDtdElement.add(
            dtd.getOrCreate("embed") // @ Texy::ALL,
            // @ FALSE,
        )
        rootDtdElement.add(
            dtd.getOrCreate("wbr") // No attrs.
            // @ FALSE,
        )

        // Transitional modified.
        rootDtdElement.getElement("a")!!.addAllAttrs("target")
        rootDtdElement.getElement("area")!!.addAllAttrs("target")
        rootDtdElement.getElement("body")!!.addAllAttrs("background bgcolor text link vlink alink")
        rootDtdElement.getElement("form")!!.addAllAttrs("target")
        rootDtdElement.getElement("img")!!.addAllAttrs("align border hspace vspace")
        rootDtdElement.getElement("input")!!.addAllAttrs("align")
        rootDtdElement.getElement("link")!!.addAllAttrs("target")
        rootDtdElement.getElement("object")!!.addAllAttrs("align border hspace vspace")
        rootDtdElement.getElement("script")!!.addAllAttrs("language")
        rootDtdElement.getElement("table")!!.addAllAttrs("align bgcolor")
        rootDtdElement.getElement("td")!!.addAllAttrs("nowrap bgcolor width height")
        rootDtdElement.getElement("th")!!.addAllAttrs("nowrap bgcolor width height")

        // missing: FRAMESET, FRAME, BGSOUND, XMP, ...
    } // init()

    val inlineElements: Set<DtdElement?>
        get() = inlineElms

    fun getOptionalEndElements(): Set<DtdElement?> {
        return optionalEndElements
    }
}
