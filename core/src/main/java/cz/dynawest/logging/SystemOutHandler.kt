package cz.dynawest.logging

import java.util.logging.ConsoleHandler

/**
 *
 * @author j
 */
class SystemOutHandler : ConsoleHandler() {
    init {
        //sealed = false;
        setOutputStream(System.out)
        //sealed = true;
    }
}
