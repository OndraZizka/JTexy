package cz.dynawest.jtexy

/**
 *
 * @author Ondrej Zizka
 */
class InvalidStateException : TexyException {
    constructor()
    constructor(arg0: String?) : super(arg0)
    constructor(arg0: String?, arg1: Throwable?) : super(arg0, arg1)
    constructor(arg0: Throwable?) : super(arg0)
}
