package cz.dynawest.jtexy.parsers

/**
 * Informational interface to tell listeners apart.
 *
 * @author Ondrej Zizka
 */
interface AroundEventListener<T : AroundEvent?> : TexyEventListener<T> // interface
