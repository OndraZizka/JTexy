package cz.dynawest.jtexy.events

import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.modules.TexyLink
import cz.dynawest.jtexy.modules.TexyModifier
import cz.dynawest.jtexy.parsers.TexyParser
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.io.Serializable

/** Root event class. */
open class TexyEvent(open var text: String?)

/** Parser event; Carries text and parser backref. */
open class TexyParserEvent (val parser: TexyParser, text: String?) : TexyEvent(text)

open class TexyParserModEvent(parser: TexyParser, text: String?, var modifier: TexyModifier?) : TexyParserEvent(parser, text)
abstract class AroundEvent(parser: TexyParser, text: String?, modifier: TexyModifier?) : TexyParserModEvent(parser, text, modifier)


abstract class BeforeAfterEvent(parser: TexyParser, text: String?) : TexyParserEvent(parser, text)
class BeforeParseEvent(parser: TexyParser, text: String?) : BeforeAfterEvent(parser, text), Serializable
class AfterParseEvent(parser: TexyParser, var isSingleLine: Boolean, var dom: DOMElement) : BeforeAfterEvent(parser, null)

class BeforeBlockEvent(parser: TexyParser, text: String?) : BeforeAfterEvent(parser, text)
interface BeforeBlockEventListener<T : BeforeBlockEvent> : BeforeAfterEventListener<T>


/**
 * Used for TypographyModule, LongWordsModule.
 *
 * TODO: extends TexyEvent, not BeforeAfterEvent. It's not a parsing event.
 * TODO: Rename to PostLineEvent, afterall.
 */
class AfterLineEvent(parser: TexyParser, text: String) : BeforeAfterEvent(parser, text)



interface TexyEventListener<in T : TexyEvent> {
    val eventClass: Class<*>

    @Throws(TexyException::class)
    fun onEvent(event: T): Node?
}

/** Informational interface to tell listeners apart. Aka. "normal" listener / handler. */
interface BeforeAfterEventListener<in T : BeforeAfterEvent> : TexyEventListener<T>
interface BeforeParseListener : BeforeAfterEventListener<BeforeAfterEvent>
interface AfterParseListener : TexyEventListener<AfterParseEvent> {
    //public void afterParse( JTexy texy, Document doc, boolean singleLine );
}



/** Informational interface to tell listeners apart. */
interface AroundEventListener<in T : AroundEvent> : TexyEventListener<T>



class PhraseEvent(parser: TexyParser, text: String, modifier: TexyModifier, val link: TexyLink?, val phraseName: String) :
    AroundEvent(parser, text, modifier)
