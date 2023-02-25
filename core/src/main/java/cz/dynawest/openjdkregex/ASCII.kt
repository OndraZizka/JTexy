/*
 * Copyright 1999-2000 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * Utility class that implements the standard C ctype functionality.
 *
 * @author Hong Zhang
 */
internal object ASCII {
    const val UPPER = 0x00000100
    const val LOWER = 0x00000200
    const val DIGIT = 0x00000400
    const val SPACE = 0x00000800
    const val PUNCT = 0x00001000
    const val CNTRL = 0x00002000
    const val BLANK = 0x00004000
    const val HEX = 0x00008000
    const val UNDER = 0x00010000
    const val ASCII = 0x0000FF00
    const val ALPHA = UPPER or LOWER
    const val ALNUM = UPPER or LOWER or DIGIT
    const val GRAPH = PUNCT or UPPER or LOWER or DIGIT
    const val WORD = UPPER or LOWER or UNDER or DIGIT
    const val XDIGIT = HEX
    private val ctype = intArrayOf(
        CNTRL,  /* 00 (NUL) */
        CNTRL,  /* 01 (SOH) */
        CNTRL,  /* 02 (STX) */
        CNTRL,  /* 03 (ETX) */
        CNTRL,  /* 04 (EOT) */
        CNTRL,  /* 05 (ENQ) */
        CNTRL,  /* 06 (ACK) */
        CNTRL,  /* 07 (BEL) */
        CNTRL,  /* 08 (BS)  */
        SPACE + CNTRL + BLANK,  /* 09 (HT)  */
        SPACE + CNTRL,  /* 0A (LF)  */
        SPACE + CNTRL,  /* 0B (VT)  */
        SPACE + CNTRL,  /* 0C (FF)  */
        SPACE + CNTRL,  /* 0D (CR)  */
        CNTRL,  /* 0E (SI)  */
        CNTRL,  /* 0F (SO)  */
        CNTRL,  /* 10 (DLE) */
        CNTRL,  /* 11 (DC1) */
        CNTRL,  /* 12 (DC2) */
        CNTRL,  /* 13 (DC3) */
        CNTRL,  /* 14 (DC4) */
        CNTRL,  /* 15 (NAK) */
        CNTRL,  /* 16 (SYN) */
        CNTRL,  /* 17 (ETB) */
        CNTRL,  /* 18 (CAN) */
        CNTRL,  /* 19 (EM)  */
        CNTRL,  /* 1A (SUB) */
        CNTRL,  /* 1B (ESC) */
        CNTRL,  /* 1C (FS)  */
        CNTRL,  /* 1D (GS)  */
        CNTRL,  /* 1E (RS)  */
        CNTRL,  /* 1F (US)  */
        SPACE + BLANK,  /* 20 SPACE */
        PUNCT,  /* 21 !     */
        PUNCT,  /* 22 "     */
        PUNCT,  /* 23 #     */
        PUNCT,  /* 24 $     */
        PUNCT,  /* 25 %     */
        PUNCT,  /* 26 &     */
        PUNCT,  /* 27 '     */
        PUNCT,  /* 28 (     */
        PUNCT,  /* 29 )     */
        PUNCT,  /* 2A *     */
        PUNCT,  /* 2B +     */
        PUNCT,  /* 2C ,     */
        PUNCT,  /* 2D -     */
        PUNCT,  /* 2E .     */
        PUNCT,  /* 2F /     */
        DIGIT + HEX + 0,  /* 30 0     */
        DIGIT + HEX + 1,  /* 31 1     */
        DIGIT + HEX + 2,  /* 32 2     */
        DIGIT + HEX + 3,  /* 33 3     */
        DIGIT + HEX + 4,  /* 34 4     */
        DIGIT + HEX + 5,  /* 35 5     */
        DIGIT + HEX + 6,  /* 36 6     */
        DIGIT + HEX + 7,  /* 37 7     */
        DIGIT + HEX + 8,  /* 38 8     */
        DIGIT + HEX + 9,  /* 39 9     */
        PUNCT,  /* 3A :     */
        PUNCT,  /* 3B ;     */
        PUNCT,  /* 3C <     */
        PUNCT,  /* 3D =     */
        PUNCT,  /* 3E >     */
        PUNCT,  /* 3F ?     */
        PUNCT,  /* 40 @     */
        UPPER + HEX + 10,  /* 41 A     */
        UPPER + HEX + 11,  /* 42 B     */
        UPPER + HEX + 12,  /* 43 C     */
        UPPER + HEX + 13,  /* 44 D     */
        UPPER + HEX + 14,  /* 45 E     */
        UPPER + HEX + 15,  /* 46 F     */
        UPPER + 16,  /* 47 G     */
        UPPER + 17,  /* 48 H     */
        UPPER + 18,  /* 49 I     */
        UPPER + 19,  /* 4A J     */
        UPPER + 20,  /* 4B K     */
        UPPER + 21,  /* 4C L     */
        UPPER + 22,  /* 4D M     */
        UPPER + 23,  /* 4E N     */
        UPPER + 24,  /* 4F O     */
        UPPER + 25,  /* 50 P     */
        UPPER + 26,  /* 51 Q     */
        UPPER + 27,  /* 52 R     */
        UPPER + 28,  /* 53 S     */
        UPPER + 29,  /* 54 T     */
        UPPER + 30,  /* 55 U     */
        UPPER + 31,  /* 56 V     */
        UPPER + 32,  /* 57 W     */
        UPPER + 33,  /* 58 X     */
        UPPER + 34,  /* 59 Y     */
        UPPER + 35,  /* 5A Z     */
        PUNCT,  /* 5B [     */
        PUNCT,  /* 5C \     */
        PUNCT,  /* 5D ]     */
        PUNCT,  /* 5E ^     */
        PUNCT or UNDER,  /* 5F _     */
        PUNCT,  /* 60 `     */
        LOWER + HEX + 10,  /* 61 a     */
        LOWER + HEX + 11,  /* 62 b     */
        LOWER + HEX + 12,  /* 63 c     */
        LOWER + HEX + 13,  /* 64 d     */
        LOWER + HEX + 14,  /* 65 e     */
        LOWER + HEX + 15,  /* 66 f     */
        LOWER + 16,  /* 67 g     */
        LOWER + 17,  /* 68 h     */
        LOWER + 18,  /* 69 i     */
        LOWER + 19,  /* 6A j     */
        LOWER + 20,  /* 6B k     */
        LOWER + 21,  /* 6C l     */
        LOWER + 22,  /* 6D m     */
        LOWER + 23,  /* 6E n     */
        LOWER + 24,  /* 6F o     */
        LOWER + 25,  /* 70 p     */
        LOWER + 26,  /* 71 q     */
        LOWER + 27,  /* 72 r     */
        LOWER + 28,  /* 73 s     */
        LOWER + 29,  /* 74 t     */
        LOWER + 30,  /* 75 u     */
        LOWER + 31,  /* 76 v     */
        LOWER + 32,  /* 77 w     */
        LOWER + 33,  /* 78 x     */
        LOWER + 34,  /* 79 y     */
        LOWER + 35,  /* 7A z     */
        PUNCT,  /* 7B {     */
        PUNCT,  /* 7C |     */
        PUNCT,  /* 7D }     */
        PUNCT,  /* 7E ~     */
        CNTRL
    )

    fun getType(ch: Int): Int {
        return if (ch and -0x80 == 0) ctype[ch] else 0
    }

    fun isType(ch: Int, type: Int): Boolean {
        return getType(ch) and type != 0
    }

    fun isAscii(ch: Int): Boolean {
        return ch and -0x80 == 0
    }

    fun isAlpha(ch: Int): Boolean {
        return isType(ch, ALPHA)
    }

    fun isDigit(ch: Int): Boolean {
        return ch - '0'.code or '9'.code - ch >= 0
    }

    fun isAlnum(ch: Int): Boolean {
        return isType(ch, ALNUM)
    }

    fun isGraph(ch: Int): Boolean {
        return isType(ch, GRAPH)
    }

    fun isPrint(ch: Int): Boolean {
        return ch - 0x20 or 0x7E - ch >= 0
    }

    fun isPunct(ch: Int): Boolean {
        return isType(ch, PUNCT)
    }

    fun isSpace(ch: Int): Boolean {
        return isType(ch, SPACE)
    }

    fun isHexDigit(ch: Int): Boolean {
        return isType(ch, HEX)
    }

    fun isOctDigit(ch: Int): Boolean {
        return ch - '0'.code or '7'.code - ch >= 0
    }

    fun isCntrl(ch: Int): Boolean {
        return isType(ch, CNTRL)
    }

    fun isLower(ch: Int): Boolean {
        return ch - 'a'.code or 'z'.code - ch >= 0
    }

    fun isUpper(ch: Int): Boolean {
        return ch - 'A'.code or 'Z'.code - ch >= 0
    }

    fun isWord(ch: Int): Boolean {
        return isType(ch, WORD)
    }

    fun toDigit(ch: Int): Int {
        return ctype[ch and 0x7F] and 0x3F
    }

    fun toLower(ch: Int): Int {
        return if (isUpper(ch)) ch + 0x20 else ch
    }

    fun toUpper(ch: Int): Int {
        return if (isLower(ch)) ch - 0x20 else ch
    }
}
