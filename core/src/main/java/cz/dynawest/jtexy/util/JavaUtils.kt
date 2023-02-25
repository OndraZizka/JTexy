package cz.dynawest.jtexy.util

import java.io.*

/**
 *
 * @author Ondrej Zizka
 */
object JavaUtils {
    @Throws(ClassNotFoundException::class)
    fun getClassesForPackage(packName: String): Array<Class<*>> {
        // Get a File object for the package.
        var dir: File? = null
        val loader = Thread.currentThread().contextClassLoader ?: throw ClassNotFoundException("Can't get a class loader.")
        val path = '/'.toString() + packName.replace('.', '/')
        val resource = loader.getResource(path) ?: throw ClassNotFoundException("No resource for path: $path")
        dir = File(resource.file)
        if (!dir.exists()) throw ClassNotFoundException("Unknown package: $packName")
        val classes = ArrayList<Class<*>>()

        // Get the list of the files contained in the package.
        val files = dir.list(classExtFilter)
        for (i in files.indices) {
            val name = files[i]
            // Remove the .class extension.
            val classFQN = packName + '.' + name.substring(0, name.length - 6)
            classes.add(Class.forName(classFQN))
        }
        return classes.toTypedArray()
    }

    /**
     * *.class filter.
     */
    val classExtFilter = FilenameFilter { dir, name ->
        val isFile = File(dir.path + File.separator + name).isFile
        isFile && name.endsWith(".class")
    }

    /**
     * Returns a stack trace from the exception.
     */
    fun getStackTrace(aThrowable: Throwable): String {
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        aThrowable.printStackTrace(printWriter)
        return result.toString()
    }
}
