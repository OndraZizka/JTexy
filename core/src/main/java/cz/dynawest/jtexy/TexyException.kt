package cz.dynawest.jtexy

/**
 *
 * @author Ondrej Zizka
 */
open class TexyException : Exception {
    constructor(arg0: Throwable?) : super(arg0)
    constructor(arg0: String?, arg1: Throwable?) : super(arg0, arg1)
    constructor(arg0: String?) : super(arg0)
    constructor()

    companion object {
        @Throws(TexyException::class)
        fun throwIfErrors(string: String?, exceptions: List<TexyException>) {
            if (exceptions.size > 0) throw create(string, exceptions)
        }

        fun create(string: String?, exceptions: List<TexyException>): TexyException {
            val sb = StringBuilder(string)
            for (ex in exceptions) {
                sb.append("\n  ").append(ex!!.message)
            }
            return TexyException(sb.toString())
        }
    }
}
