/*  -------------------------------------------------------------------------------
 	This code is licensed under the Apache License, Version 2.0 (the "License");
 	You may not use this file except in compliance with the License.
 	You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software distributed under
    the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
	limitations under the License.
    -------------------------------------------------------------------------------
 */
package cz.dynawest.jtexy.util

import java.io.*

/*
 *	A Java class to determine image width, height and MIME types for a number of image file formats without loading the whole image data.
 *
 *	@author  Jaimon Mathew <http://www.jaimon.co.uk>
 */
class SimpleImageSize {
    var height = 0
        private set
    var width = 0
        private set
    var mimeType: String? = null
        private set

    constructor(file: File?) {
        val `is`: InputStream = FileInputStream(file)
        try {
            processStream(`is`)
        } finally {
            `is`.close()
        }
    }

    constructor(`is`: InputStream) {
        processStream(`is`)
    }

    constructor(bytes: ByteArray?) {
        val `is`: InputStream = ByteArrayInputStream(bytes)
        try {
            processStream(`is`)
        } finally {
            `is`.close()
        }
    }

    @Throws(IOException::class)
    private fun processStream(`is`: InputStream) {
        val c1 = `is`.read()
        val c2 = `is`.read()
        var c3 = `is`.read()
        mimeType = null
        height = -1
        width = height
        if (c1 == 'G'.code && c2 == 'I'.code && c3 == 'F'.code) { // GIF
            `is`.skip(3)
            width = readInt(`is`, 2, false)
            height = readInt(`is`, 2, false)
            mimeType = "image/gif"
        } else if (c1 == 0xFF && c2 == 0xD8) { // JPG
            while (c3 == 255) {
                val marker = `is`.read()
                val len = readInt(`is`, 2, true)
                if (marker == 192 || marker == 193 || marker == 194) {
                    `is`.skip(1)
                    height = readInt(`is`, 2, true)
                    width = readInt(`is`, 2, true)
                    mimeType = "image/jpeg"
                    break
                }
                `is`.skip((len - 2).toLong())
                c3 = `is`.read()
            }
        } else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
            `is`.skip(15)
            width = readInt(`is`, 2, true)
            `is`.skip(2)
            height = readInt(`is`, 2, true)
            mimeType = "image/png"
        } else if (c1 == 66 && c2 == 77) { // BMP
            `is`.skip(15)
            width = readInt(`is`, 2, false)
            `is`.skip(2)
            height = readInt(`is`, 2, false)
            mimeType = "image/bmp"
        } else {
            val c4 = `is`.read()
            if (c1 == 'M'.code && c2 == 'M'.code && c3 == 0 && c4 == 42 || c1 == 'I'.code && c2 == 'I'.code && c3 == 42 && c4 == 0) { //TIFF
                val bigEndian = c1 == 'M'.code
                var ifd = 0
                val entries: Int
                ifd = readInt(`is`, 4, bigEndian)
                `is`.skip((ifd - 8).toLong())
                entries = readInt(`is`, 2, bigEndian)
                for (i in 1..entries) {
                    val tag = readInt(`is`, 2, bigEndian)
                    val fieldType = readInt(`is`, 2, bigEndian)
                    val count = readInt(`is`, 4, bigEndian).toLong()
                    var valOffset: Int
                    if (fieldType == 3 || fieldType == 8) {
                        valOffset = readInt(`is`, 2, bigEndian)
                        `is`.skip(2)
                    } else {
                        valOffset = readInt(`is`, 4, bigEndian)
                    }
                    if (tag == 256) {
                        width = valOffset
                    } else if (tag == 257) {
                        height = valOffset
                    }
                    if (width != -1 && height != -1) {
                        mimeType = "image/tiff"
                        break
                    }
                }
            }
        }
        if (mimeType == null) {
            throw IOException("Unsupported image type")
        }
    }

    @Throws(IOException::class)
    private fun readInt(`is`: InputStream, noOfBytes: Int, bigEndian: Boolean): Int {
        var ret = 0
        var sv = if (bigEndian) (noOfBytes - 1) * 8 else 0
        val cnt = if (bigEndian) -8 else 8
        for (i in 0 until noOfBytes) {
            ret = ret or (`is`.read() shl sv)
            sv += cnt
        }
        return ret
    }

    override fun toString(): String {
        return "MIME $mimeType, w$width h$height"
    }
}
