package cz.dynawest.jtexy.parsers

/**
 * Informational interface to tell listeners apart.
 * Aka. "normal" listener / handler.
 *
 * @author Ondrej Zizka
 */
interface BeforeAfterEventListener<in T : BeforeAfterEvent> : TexyEventListener<T>
