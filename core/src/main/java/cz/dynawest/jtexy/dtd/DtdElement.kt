package cz.dynawest.jtexy.dtd

/**
 * DTD descriptor.
 * $dtd[element][0] - allowed attributes (as array keys)
 * $dtd[element][1] - allowed content for an element (content model) (as array keys)
 * - array of allowed elements (as keys)
 * - FALSE - empty element
 * - 0 - special case for ins & del
 *
 * @author Ondrej Zizka
 *
 * @ It would be great to work directly with DTD, if there was an library...
 */
class DtdElement
/**
 * Does not set DTD. Use for cases like set.contains( new DtdElement("foo") );
 */
(
    val name: String
) {
    private val elements_: MutableMap<String, DtdElement> = HashMap()

    private val attrs: MutableSet<DtdAttr> = HashSet()
    var dtd: Dtd? = null
    fun getElement(name: String): DtdElement? {
        return elements_[name]
    }

    fun add(e: DtdElement): DtdElement {
        elements_[e!!.name] = e
        return this
    }

    fun add(a: DtdAttr): DtdElement {
        attrs.add(a)
        return this
    }

    fun addAll(c: Collection<DtdElement>): DtdElement {
        for (dtdElement in c!!) {
            add(dtdElement)
        }
        return this
    }

    fun addAllAttrs(c: Collection<DtdAttr>): DtdElement {
        attrs.addAll(c!!)
        return this
    }

    fun addAll(nameStr: String): DtdElement {
        for (dtdElement in dtd!!.getOrCreateElements(nameStr)) {
            add(dtdElement)
        }
        return this
    }

    fun removeAll(nameStr: String): DtdElement {
        for (name in nameStr.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            elements_.remove(name)
        }
        return this
    }

    fun addAllAttrs(nameStr: String): DtdElement {
        attrs.addAll(Dtd.createAttributes(nameStr))
        return this
    }

    fun addAllAttrsIf(nameStr: String, cond: Boolean): DtdElement {
        if (cond) attrs.addAll(Dtd.createAttributes(nameStr))
        return this
    }

    fun hasChildren(): Boolean {
        return !elements_.isEmpty()
    }

    fun hasNoChildren(): Boolean {
        return elements_.isEmpty()
    }

    fun getElements(): Set<DtdElement> {
        return HashSet(elements_.values)
    }

    fun getAttrs(): Set<DtdAttr?> {
        return attrs
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 97 * hash + if (name != null) name.hashCode() else 0
        return hash
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DtdElement
        return if (if (name == null) other.name != null else name != other.name) {
            false
        } else true
    }

    override fun toString(): String {
        return "DtdElement{ " + name + ", " + elements_.size + " elms, " + attrs.size + "attrs }"
    }
}
