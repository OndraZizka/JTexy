package cz.dynawest.jtexy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Completely hide Properties's internal HashMap with our OrderedHashMap.
 */
public class OrderedProperties extends Properties {

	private LinkedHashMap<Object, Object> properties = new LinkedHashMap<Object, Object>();

	public OrderedProperties() {
	}

	@Override
	public String getProperty(String key) {
		return (String) properties.get(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String val = this.getProperty(key);
		return val == null ? defaultValue : val;
	}

	@Override
	public void list(PrintStream out) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void list(PrintWriter out) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void load(Reader reader) throws IOException {
		properties = new LinkedHashMap<Object, Object>();
		BufferedReader br = new BufferedReader(reader);

		try {
			String line;
			while (null != (line = br.readLine())) {
				line = line.trim();
				// Empty or comment -> skip.
				if ("".equals(line) || '#' == line.charAt(0)) {
					continue;
				}
				int delim = StringUtils.indexOfAny(line, "=:");

				String name;
				String value;

				if (delim == -1) {
					name = line;
					value = "";
				} else {
					name = line.substring(0, delim).trim();
					value = StringEscapeUtils.unescapeJava(line.substring(delim + 1).trim());
				}

				properties.put(name, value);
			}
		} finally {
			br.close();
		}

	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		load(new InputStreamReader(inStream));
	}

	@Override
	public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<?> propertyNames() {
		return this.keys();
	}

	@Override
	public synchronized void save(OutputStream out, String comments) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		return this.put(key, value);
	}

	@Override
	public void store(Writer writer, String comments) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void store(OutputStream out, String comments) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> stringPropertyNames() {
		return this.keySet();
	}

	// Ordered-map backed.
	@Override
	public Collection values() {
		return properties.values();
	}

	@Override
	public int size() {
		return properties.size();
	}

	@Override
	public Object remove(Object key) {
		return properties.remove(key);
	}

	@Override
	public void putAll(Map m) {
		properties.putAll(m);
	}

	@Override
	public Object put(Object key, Object value) {
		return properties.put(key, value);
	}

	@Override
	public Set keySet() {
		return properties.keySet();
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	@Override
	public Object get(Object key) {
		return properties.get(key);
	}

	@Override
	public Set entrySet() {
		return properties.entrySet();
	}

	@Override
	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	@Override
	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}

	@Override
	public void clear() {
		properties.clear();
	}

	public Iterator iterator() {
		return properties.entrySet().iterator();
	}

	@Override
	public synchronized String toString() {
		return this.getClass().getName() + " " + Arrays.toString(properties.values().toArray());
	}

	@Override
	public synchronized int hashCode() {
		return properties.hashCode();
	}

	@Override
	public synchronized boolean equals(Object o) {
		return (o instanceof OrderedProperties) && properties.equals(o);
	}

	@Override
	public synchronized Enumeration<Object> keys() {
		return Collections.enumeration(properties.keySet());
	}

	@Override
	public synchronized Enumeration<Object> elements() {
		return Collections.enumeration(properties.values());
	}

	@Override
	public synchronized boolean contains(Object value) {
		return properties.containsValue(value);
	}

	@Override
	public synchronized Object clone() {
		throw new UnsupportedOperationException();
	}
}
