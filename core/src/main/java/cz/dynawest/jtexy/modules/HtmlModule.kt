package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.parsers.TexyEventListener
import java.util.logging.Logger

/**
 *
 * @author Ondrej Zizka
 */
class HtmlModule : TexyModule() {
    private val somePH: PatternHandler? = null
    override val eventListeners: Array<TexyEventListener<*>>
        // -- Module meta-info -- //
        get() = arrayOf()

    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if (somePH.getName() == name) {
            somePH
        } else {
            null
        }
    }

    companion object {
        private val log = Logger.getLogger(HtmlModule::class.java.name)
    }
}
