package cz.dynawest.jtexy.modules

/**
 * Interface for inter-module callbacks in LinkProcessListener.
 */
fun interface LinkProvider {
    fun getLink(key: String): TexyLink?
}
