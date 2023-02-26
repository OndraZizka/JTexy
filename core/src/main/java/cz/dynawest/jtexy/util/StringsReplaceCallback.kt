package cz.dynawest.jtexy.util

/**
 *
 * @author Ondrej Zizka
 */
fun interface StringsReplaceCallback {
    fun replace(groups: Array<String>): String
}
