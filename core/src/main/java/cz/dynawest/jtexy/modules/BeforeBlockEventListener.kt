package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.parsers.BeforeAfterEventListener

/**
 *
 * @author Ondrej Zizka
 */
interface BeforeBlockEventListener<T : BeforeBlockEvent?> : BeforeAfterEventListener<T>
