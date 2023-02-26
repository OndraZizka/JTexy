package cz.dynawest.jtexy.dtd

import org.apache.commons.lang.StringUtils

/**
 * Simplified DTD. Contains map of elements String -> DtdElement, and a root element.
 *
 * @author Ondrej Zizka.
 */
class Dtd(name: String) {
    val rootElement: DtdElement
    private val elementsMap: MutableMap<String, DtdElement> = HashMap()

    /** @see http://www.w3.org/TR/xhtml1/prohibitions.html
     */
    var prohibits: MutableMap<DtdElement, Set<DtdElement>> = HashMap()

    init {
        rootElement = DtdElement(name)
    }

    fun put(elm: DtdElement): Dtd {
        elementsMap[elm.name] = elm
        return this
    }

    operator fun get(name: String): DtdElement? {
        return elementsMap[name]
    }

    operator fun contains(name: String): Boolean {
        return elementsMap.containsKey(name)
    }

    fun getOrCreate(name: String): DtdElement {

        // Exists?
        var elm = elementsMap[name]
        if (null != elm) return elm

        // New
        elm = DtdElement(name)
        elm.dtd = this
        elementsMap[name] = elm
        return elm
    }

    /**
     * See e.g. @see http://www.w3.org/TR/xhtml1/prohibitions.html
     */
    fun prohibit(parent: String, childrenStr: String) {
        prohibits[this[parent]!!] = HashSet(getOrCreateElements(childrenStr))
    }

    /**
     * @returns  true if given child is prohibited in given parent.
     */
    fun isProhibited(parent: DtdElement, child: DtdElement): Boolean {
        val probibitedElms = prohibits[parent] ?: return true
        return probibitedElms.contains(child)
    }

    /**
     * @returns  List of prohibitions, or null.
     */
    fun getProbibitionsOf(parent: DtdElement): Set<DtdElement>? {
        //if( prohibs == null )  return Collections.EMPTY_SET;
        return prohibits[parent]
    }

    /**
     * Convenience.
     * @returns  list of elements of names given in nameStr bound to this DTD.
     */
    fun getOrCreateElements(nameStr: String): List<DtdElement> {
        val names = StringUtils.split(nameStr, " ")
        val list: MutableList<DtdElement> = ArrayList(names.size)
        for (name in names) {
            list.add(getOrCreate(name))
        }
        return list
    }

    companion object {
        /**
         * Convenience.
         * @returns  list of attributes of names given in nameStr.
         */
        fun createAttributes(nameStr: String): List<DtdAttr> {
            val names = StringUtils.split(nameStr, " ")
            val list: MutableList<DtdAttr> = ArrayList(names.size)
            for (name in names) {
                list.add(DtdAttr(name))
            }
            return list
        }
    }
}
