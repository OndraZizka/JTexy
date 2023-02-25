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
 */( //<editor-fold defaultstate="collapsed" desc="get/set">
    val name: String?
) {
    private val elements: MutableMap<String?, DtdElement?> = HashMap<Any?, Any?>()
    private val attrs: MutableSet<DtdAttr?> = HashSet<Any?>()
    var dtd: Dtd? = null
    fun getElement(name: String?): DtdElement? {
        return elements[name]
    }

    fun add(e: DtdElement?): DtdElement {
        elements[e!!.name] = e
        return this
    }

    fun add(a: DtdAttr?): DtdElement {
        attrs.add(a)
        return this
    }

    fun addAll(c: Collection<DtdElement?>?): DtdElement {
        for (dtdElement in c!!) {
            add(dtdElement)
        }
        return this
    }

    fun addAllAttrs(c: Collection<DtdAttr?>?): DtdElement {
        attrs.addAll(c!!)
        return this
    }

    fun addAll(nameStr: String?): DtdElement {
        for (dtdElement in dtd!!.getOrCreateElements(nameStr)) {
            add(dtdElement)
        }
        return this
    }

    fun removeAll(nameStr: String): DtdElement {
        for (name in nameStr.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            elements.remove(name)
        }
        return this
    }

    fun addAllAttrs(nameStr: String?): DtdElement {
        attrs.addAll(Dtd.Companion.createAttributes(nameStr))
        return this
    }

    fun addAllAttrsIf(nameStr: String?, cond: Boolean): DtdElement {
        if (cond) attrs.addAll(Dtd.Companion.createAttributes(nameStr))
        return this
    }

    fun hasChildren(): Boolean {
        return !elements.isEmpty()
    }

    fun hasNoChildren(): Boolean {
        return elements.isEmpty()
    }

    fun getElements(): Set<DtdElement?> {
        return HashSet<Any?>(elements.values)
    }

    fun getAttrs(): Set<DtdAttr?> {
        return attrs
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="equals/hash">
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
        return "DtdElement{ " + name + ", " + elements.size + " elms, " + attrs.size + "attrs }"
    }

    companion object {
        //</editor-fold>
        // Unused. TODO - good for anything?
        private val ALL: Set<DtdElement> = object : MutableSet<DtdElement?> {
            override fun size(): Int {
                throw UnsupportedOperationException("ALL constant only supports contains().")
            }

            override fun isEmpty(): Boolean {
                return false
            }

            override operator fun contains(o: Any): Boolean {
                return o is DtdElement
            }

            override fun iterator(): MutableIterator<DtdElement> {
                throw co()
            }

            override fun toArray(): Array<Any> {
                throw co()
            }

            override fun <T> toArray(a: Array<T>): Array<T> {
                throw co()
            }

            override fun add(e: DtdElement): Boolean {
                throw ro()
            }

            override fun remove(o: Any): Boolean {
                throw ro()
            }

            override fun containsAll(c: Collection<*>?): Boolean {
                return true
            }

            override fun addAll(c: Collection<DtdElement>): Boolean {
                throw ro()
            }

            override fun retainAll(c: Collection<*>?): Boolean {
                throw ro()
            }

            override fun removeAll(c: Collection<*>?): Boolean {
                throw ro()
            }

            override fun clear() {
                ro()
            }

            private fun ro(): UnsupportedOperationException {
                return UnsupportedOperationException("Read only.")
            }

            private fun co(): UnsupportedOperationException {
                return UnsupportedOperationException("ALL constant only supports contains().")
            }
        } // ALL
    }
}
