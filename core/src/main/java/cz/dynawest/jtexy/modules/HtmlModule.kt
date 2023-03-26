package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.events.TexyEventListener
import java.util.logging.Logger

/**
 *
 * @author Ondrej Zizka
 */
class HtmlModule : TexyModule() {
    private val somePH: PatternHandler? = null
    override val eventListeners: List<TexyEventListener<TexyEvent>>
        get() = emptyList()


    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if (somePH?.name == name) {
            somePH
        } else {
            null
        }
    }

    companion object {
        private val log = Logger.getLogger(HtmlModule::class.java.name)
    }
}
