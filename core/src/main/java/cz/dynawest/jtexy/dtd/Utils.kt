package cz.dynawest.jtexy.dtd

/**
 * DTD util methods.
 *
 * @author Ondrej Zizka
 */
@Deprecated("Moved to Dtd.")
object Utils {
    private fun createElements(dtd: Dtd, nameStr: String): List<DtdElement> {
        return dtd.getOrCreateElements(nameStr)
    }
}
