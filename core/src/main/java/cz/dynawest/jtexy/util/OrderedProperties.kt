package cz.dynawest.jtexy.util

import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import java.io.*
import java.util.*

/**
 * Completely hide Properties's internal HashMap with our OrderedHashMap.
 */
class OrderedProperties : Properties() {
    private var properties = LinkedHashMap<Any, Any>()
    override fun getProperty(key: String): String {
        return properties[key] as String
    }

    override fun getProperty(key: String, defaultValue: String): String {
        val `val` = this.getProperty(key)
        return `val` ?: defaultValue
    }

    override fun list(out: PrintStream) {
        throw UnsupportedOperationException()
    }

    override fun list(out: PrintWriter) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun load(reader: Reader) {
        properties = LinkedHashMap()
        val br = BufferedReader(reader)
        try {
            var line: String
            while (null != br.readLine().also { line = it }) {
                line = line.trim { it <= ' ' }
                // Empty or comment -> skip.
                if ("" == line || '#' == line[0]) {
                    continue
                }
                val delim = StringUtils.indexOfAny(line, "=:")
                var name: String
                var value: String
                if (delim == -1) {
                    name = line
                    value = ""
                } else {
                    name = line.substring(0, delim).trim { it <= ' ' }
                    value = StringEscapeUtils.unescapeJava(line.substring(delim + 1).trim { it <= ' ' })
                }
                properties[name] = value
            }
        } finally {
            br.close()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun load(inStream: InputStream) {
        load(InputStreamReader(inStream))
    }

    @Synchronized
    @Throws(IOException::class, InvalidPropertiesFormatException::class)
    override fun loadFromXML(`in`: InputStream) {
        throw UnsupportedOperationException()
    }

    override fun propertyNames(): Enumeration<*> {
        return keys()
    }

    @Synchronized
    override fun save(out: OutputStream, comments: String) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    override fun setProperty(key: String, value: String): Any {
        return put(key, value)!!
    }

    @Throws(IOException::class)
    override fun store(writer: Writer, comments: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun store(out: OutputStream, comments: String) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun storeToXML(os: OutputStream, comment: String) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun storeToXML(os: OutputStream, comment: String, encoding: String) {
        throw UnsupportedOperationException()
    }

    override fun stringPropertyNames(): Set<String> {
        return keys
    }

    // Ordered-map backed.
    override fun values(): Collection<*> {
        return properties.values
    }

    override fun size(): Int {
        return properties.size
    }

    override fun remove(key: Any): Any? {
        return properties.remove(key)
    }

    override fun putAll(m: Map<*, *>?) {
        properties.putAll(m)
    }

    override fun put(key: Any, value: Any): Any? {
        return properties.put(key, value)
    }

    override fun keySet(): Set<*> {
        return properties.keys
    }

    override fun isEmpty(): Boolean {
        return properties.isEmpty()
    }

    override fun get(key: Any): Any? {
        return properties[key]
    }

    override fun entrySet(): Set<*> {
        return properties.entries
    }

    override fun containsValue(value: Any): Boolean {
        return properties.containsValue(value)
    }

    override fun containsKey(key: Any): Boolean {
        return properties.containsKey(key)
    }

    override fun clear() {
        properties.clear()
    }

    operator fun iterator(): Iterator<*> {
        return properties.entries.iterator()
    }

    @Synchronized
    override fun toString(): String {
        return this.javaClass.name + " " + Arrays.toString(properties.values.toTypedArray())
    }

    @Synchronized
    override fun hashCode(): Int {
        return properties.hashCode()
    }

    @Synchronized
    override fun equals(o: Any?): Boolean {
        return o is OrderedProperties && properties == o
    }

    @Synchronized
    override fun keys(): Enumeration<Any> {
        return Collections.enumeration(properties.keys)
    }

    @Synchronized
    override fun elements(): Enumeration<Any> {
        return Collections.enumeration(properties.values)
    }

    @Synchronized
    override fun contains(value: Any): Boolean {
        return properties.containsValue(value)
    }

    @Synchronized
    override fun clone(): Any {
        throw UnsupportedOperationException()
    }
}
