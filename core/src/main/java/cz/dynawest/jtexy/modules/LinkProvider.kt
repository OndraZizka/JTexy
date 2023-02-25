package cz.dynawest.jtexy.modules

/**
 * Interface for inter-module callbacks in LinkProcessListener.
 */
interface LinkProvider {
    fun getLink(key: String?): TexyLink?
}
