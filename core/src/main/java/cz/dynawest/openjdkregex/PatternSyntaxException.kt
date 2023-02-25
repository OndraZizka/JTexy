/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package cz.dynawest.openjdkregex

import sun.security.action.GetPropertyAction
import java.security.AccessController

/**
 * Unchecked exception thrown to indicate a syntax error in a
 * regular-expression pattern.
 *
 * @author  unascribed
 * @since 1.4
 * @spec JSR-51
 */
class PatternSyntaxException
/**
 * Constructs a new instance of this class.
 *
 * @param  desc
 * A description of the error
 *
 * @param  regex
 * The erroneous pattern
 *
 * @param  index
 * The approximate index in the pattern of the error,
 * or <tt>-1</tt> if the index is not known
 */(private val desc: String, private val pattern: String?, private val index: Int) : IllegalArgumentException() {
    /**
     * Retrieves the error index.
     *
     * @return  The approximate index in the pattern of the error,
     * or <tt>-1</tt> if the index is not known
     */
    fun getIndex(): Int {
        return index
    }

    /**
     * Retrieves the description of the error.
     *
     * @return  The description of the error
     */
    fun getDescription(): String {
        return desc
    }

    /**
     * Retrieves the erroneous regular-expression pattern.
     *
     * @return  The erroneous pattern
     */
    fun getPattern(): String? {
        return pattern
    }

    /**
     * Returns a multi-line string containing the description of the syntax
     * error and its index, the erroneous regular-expression pattern, and a
     * visual indication of the error index within the pattern.
     *
     * @return  The full detail message
     */
    override fun getMessage(): String {
        val sb = StringBuffer()
        sb.append(desc)
        if (index >= 0) {
            sb.append(" near index ")
            sb.append(index)
        }
        sb.append(nl)
        sb.append(pattern)
        if (index >= 0) {
            sb.append(nl)
            for (i in 0 until index) sb.append(' ')
            sb.append('^')
        }
        return sb.toString()
    }

    companion object {
        private val nl = AccessController
            .doPrivileged(GetPropertyAction("line.separator"))
    }
}
