package cz.dynawest.jtexy

import java.util.*

/**
 *
 * @author Ondrej Zizka
 */
class Options {
    // -- Hard-coded options -- //
    var removeSoftHyphens = false
    var tabWidth = 4
    var mergeLines = true
    var nontextParagraph = "div"
    var makeAutoLinksShorter = true
    var obfuscateEmails = true
    var useLinkDefinitions = true
    var linkRootUrl = ""
    var imageRootUrl = ""
    var imageRootDir = ""
    var forceNofollow = false
    var popupOnclick = ""

    // -- Set-event listeners -- //
    private val setListeners: MutableSet<SetPropertyListener?> = LinkedHashSet<Any?>()
    fun addSetPropertyListener(lis: SetPropertyListener?) {
        setListeners.add(lis)
    }

    fun setProperty(name: String, value: Any?): Int {
        var accepted = 0
        propsHistory.add(SetPropertyEvent(name, value))
        for (lis in setListeners) {
            if (lis!!.onSetProperty(name, value)) accepted++
        }
        return accepted
    }

    // Set-event listener interface.
    interface SetPropertyListener {
        /** Returns true if this listener accepted the property (processed it somehow).  */
        fun onSetProperty(name: String?, value: Any?): Boolean
    }

    // Set-event.
    class SetPropertyEvent(var name: String, var value: Any?)

    // -- Keep history of set properties for debug purposes. -- //
    private val propsHistory: MutableList<SetPropertyEvent?> = LinkedList<Any?>()
    val propertiesHistory: Any
        get() = Collections.unmodifiableList(propsHistory)

    fun getPropertyLastValue(name: String?): Any? {
        if (name == null) throw NullPointerException("Property name can't be null.")
        // Not likely to be used much, no need for hashmap.
        val it: ListIterator<SetPropertyEvent?> = propsHistory.listIterator(propsHistory.size)
        while (it.hasPrevious()) {
            val ev = it.previous()
            if (name == ev!!.name) return ev.value
        }
        return null
    }
}
