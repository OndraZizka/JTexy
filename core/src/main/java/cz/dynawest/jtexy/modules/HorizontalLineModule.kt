package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.parsers.TexyParser
import cz.dynawest.jtexy.util.MatchWithOffset
import org.dom4j.Node
import org.dom4j.dom.DOMElement

/**
 *
 * @author Ondrej Zizka
 */
class HorizontalLineModule : TexyModule() {
    // --- Module meta-info --- //
    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if (hrPH.name == name) hrPH else null
    }

    override val eventListeners: List<TexyEventListener<TexyEvent>> = listOf(horizLineListener as TexyEventListener<TexyEvent>)

    companion object {
        /** Horizontal line pattern handler.  */
        protected var hrPH: PatternHandler = object : PatternHandler {
            override val name = "horizline"


            @Throws(TexyException::class)
            override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, regexpInfo: RegexpInfo): Node?
            {
                val mod = TexyModifier(groups[2].match!!)
                val ev = HorizontalLineEvent(parser, null, mod)
                return parser.texy.invokeAroundHandlers(ev)
            }
        }

        /**
         *
         */
        val horizLineListener: HorizontalLineEventListener = object : HorizontalLineEventListener {
            override val eventClass: Class<HorizontalLineEvent>
                get() = HorizontalLineEvent::class.java

            override fun onEvent(event: HorizontalLineEvent): Node {
                val elm = DOMElement("hr")
                event.modifier?.decorate(elm)
                // TBD: Optional HR classes, see Texy.
                return elm
            }
        }
    }
}
