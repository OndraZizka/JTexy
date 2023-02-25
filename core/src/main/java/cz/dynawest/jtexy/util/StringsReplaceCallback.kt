package cz.dynawest.jtexy.util

/**
 *
 * @author Ondrej Zizka
 */
interface StringsReplaceCallback {
    fun replace(groups: Array<String?>): String
}
