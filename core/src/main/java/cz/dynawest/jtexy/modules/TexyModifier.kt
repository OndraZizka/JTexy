package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.JTexy
import org.apache.commons.lang.StringUtils
import org.dom4j.dom.DOMElement
import org.dom4j.tree.FlyweightAttribute
import java.util.*
import java.util.logging.*

/**
 * Modifier processor.
 *
 * Modifiers are texts like .(title)[class1 class2 #id]{color: red}>^
 * .         starts with dot
 * (...)     title or alt modifier
 * [...]     classes or ID modifier
 * {...}     inner style modifier
 * < > <> =  horizontal align modifier
 * ^ - _     vertical align modifier
 *
 * @author     Ondrej Zizka
 * @author     David Grudl
 */
class TexyModifier : Cloneable {
    var id: String? = null
    var hAlign: String? = null
    var vAlign: String? = null
    var title: String? = null
    var cite: String? = null

    /** CSS classes.  */
    var classes: MutableSet<String?> = LinkedHashSet()

    /** CSS styles  */
    var styles: MutableMap<String?, String?> = LinkedHashMap<Any?, Any?>()

    /** HTML element attributes  */
    var attrs: MutableMap<String?, String?> = LinkedHashMap<Any?, Any?>()

    /** Const  */
    constructor()
    constructor(modifierString: String?) {
        parse(modifierString)
    }

    /** Deep clone.  */
    public override fun clone(): TexyModifier {
        val ret = TexyModifier("")
        ret.id = id
        ret.hAlign = hAlign
        ret.vAlign = vAlign
        ret.title = title
        ret.cite = cite
        ret.classes = LinkedHashSet<Any?>(classes)
        ret.styles = LinkedHashMap<Any?, Any?>(styles)
        ret.attrs = LinkedHashMap<Any?, Any?>(attrs)
        return ret
    }

    /**
     * Parses the modifier string and updates the internal data accordingly.
     *
     * @param modifierString  .(title)[class]{style/attribs}<>
     */
    fun parse(modifierString: String?) {
        if (StringUtils.isEmpty(modifierString)) return
        var pos = 0
        val len = modifierString!!.length
        while (pos < len) {
            val ch = modifierString[pos]
            when (ch) {
                '[' -> {
                    val pos2 = modifierString.indexOf(']', pos + 1) + 1
                    val str = modifierString.substring(pos + 1, pos2 - 1)
                    val parts = StringUtils.split(str)
                    for (part in parts) {
                        if (part.startsWith("#") && part.length > 1) id = part.substring(1) else classes.add(part)
                    }
                    pos = pos2
                }

                '(' -> {
                    val pos2 = modifierString.indexOf(')', pos + 1) + 1
                    title = modifierString.substring(pos + 1, pos2 - 1)
                    pos = pos2
                }

                '{' -> {
                    val pos2 = modifierString.indexOf('}', pos + 1) + 1
                    val code = modifierString.substring(pos + 1, pos2 - 1)
                    val split = StringUtils.split(code, ';')
                    var i = 0
                    while (i < split.size) {
                        val elem = split[i]
                        val pair = StringUtils.split(elem, ":", 2)
                        val name = pair[0].trim { it <= ' ' }.lowercase(Locale.getDefault())
                        val `val` = if (pair.size != 2) null else pair[1].trim { it <= ' ' }
                        if (elAttrs!!.contains(name)) attrs[name] =
                            StringUtils.defaultIfEmpty(`val`, "true") else if (null != `val`) styles[name] = `val`
                        i++
                    }
                    pos = pos2
                }

                '*' -> pos++
                '^' -> {
                    vAlign = "top"
                    pos++
                }

                '-' -> {
                    vAlign = "middle"
                    pos++
                }

                '_' -> {
                    vAlign = "bottom"
                    pos++
                }

                '=' -> {
                    hAlign = "justify"
                    pos++
                }

                '>' -> {
                    hAlign = "right"
                    pos++
                }

                '<' -> {
                    if (modifierString.length > pos + 1 && modifierString[pos + 1] == '>') {
                        hAlign = "center"
                        pos += 2
                    } else {
                        hAlign = "left"
                        pos++
                    }
                }

                else -> {

                    // Malformed - log WARNING and skip to first valid char.
                    log.warning("Malformed Texy modifier at position $pos: $modifierString")
                    val sub = modifierString.substring(pos)
                    val skip = StringUtils.indexOfAny(sub, "({[^-_=><")
                    if (skip < 1) {
                        log.warning("Skipping the rest of string.")
                        return
                    }
                    pos = pos + skip
                    log.warning("Skipping to position $pos")
                }
            }
        } // while()
    } // parse()

    /**
     * Decorates HTML element according to this modifier.
     * @param elm
     */
    fun decorate(texy: JTexy?, elm: DOMElement) {
        decorate(elm)
    }

    /**
     * Decorates HTML element according to this modifier.
     */
    fun decorate(elm: DOMElement) {

        /* Various */
        if (!StringUtils.isBlank(id)) elm.add(FlyweightAttribute("id", id))
        if (!StringUtils.isBlank(hAlign)) elm.add(FlyweightAttribute("halign", hAlign))
        if (!StringUtils.isBlank(vAlign)) elm.add(FlyweightAttribute("valign", vAlign))
        if (!StringUtils.isBlank(title)) elm.add(FlyweightAttribute("title", title))
        if (!StringUtils.isBlank(cite)) elm.add(FlyweightAttribute("cite", cite))

        /* CSS classes. */if (!classes.isEmpty()) elm.add(FlyweightAttribute("class", StringUtils.join(classes, ' ')))

        /* CSS styles. */if (!styles.isEmpty()) elm.add(FlyweightAttribute("style", mapToCSS(styles)))

        /* HTML element attributes. */for ((key, value) in attrs) {
            elm.add(FlyweightAttribute(key, value))
        }
    }

    override fun toString(): String {
        return "TexyModifier{" + "id=" + id + ", hAlign=" + hAlign + ", vAlign=" + vAlign +
                ", title=" + title + ", cite=" + cite + ", classes='" + StringUtils.join(classes, ' ') +
                "', styles " + styles.size + ", attrs " + attrs.size + '}'
    }

    companion object {
        private val log = Logger.getLogger(TexyModifier::class.java.name)

        /** Set of properties which are regarded as HTML element attributes.  */
        var elAttrs: Set<String?>? = null

        init {

            // Create the set of allowed HTML attributes.
            val strAttrs = "abbr,accesskey,align,alt,archive,axis,bgcolor,cellpadding" +
                    "cellspacing,char,charoff,charset,cite,classid,codebase,codetype" +
                    "colspan,compact,coords,data,datetime,declare,dir,face,frame" +
                    "headers,href,hreflang,hspace,ismap,lang,longdesc,name" +
                    "noshade,nowrap,onblur,onclick,ondblclick,onkeydown,onkeypress" +
                    "onkeyup,onmousedown,onmousemove,onmouseout,onmouseover,onmouseup,rel" +
                    "rev,rowspan,rules,scope,shape,size,span,src,standby" +
                    "start,summary,tabindex,target,title,type,usemap,valign" +
                    "value,vspace"
            val attrs = StringUtils.split(strAttrs, ',')
            elAttrs = HashSet<Any?>(Arrays.asList(*attrs))
        }

        /**
         * @param   String -> String map of CSS rules.
         * @return  "key1:val1; key2:val2"
         */
        private fun mapToCSS(styles: Map<String?, String?>): String {
            if (styles.size == 0) return ""
            val sb = StringBuilder(styles.size * 20)
            for ((key, value) in styles) {
                sb.append(key).append(":").append(value).append(";")
            }
            return sb.substring(0, sb.length - 1)
        }
    }
}
