package cz.dynawest.jtexy.parsers

/**
 * Informational interface to tell listeners apart.
 *
 * @author Ondrej Zizka
 */
interface AroundEventListener<in T : AroundEvent> : TexyEventListener<T>
