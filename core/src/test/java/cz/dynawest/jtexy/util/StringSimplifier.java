package cz.dynawest.jtexy.util;

import cz.dynawest.openjdkregex.Pattern;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;


/**
 *  Converts r̀r̂r̃r̈rʼŕřt̀t̂ẗţỳỹẙyʼy̎ýÿŷp̂p̈s̀s̃s̈s̊sʼs̸śŝŞşšd̂d̃d̈ďdʼḑf̈f̸g̀g̃g̈gʼģq‌​́ĝǧḧĥj̈jʼḱk̂k̈k̸ǩl̂l̃l̈Łłẅẍc̃c̈c̊cʼc̸Çççćĉčv̂v̈vʼv̸b́b̧ǹn̂n̈n̊nʼńņňñm̀m̂m̃m̈‌​m̊m̌ǵß
 *  to       rrrrtyyyyysssssssssddddffgggqqqqqhjjjkkkllllccccccccvvvvbbnnnnnnnns
 *  etc.
 * 
 *  @author Andreas Petersson
 *  @see http://stackoverflow.com/questions/1453171/n-n-n-or-remove-diacritical-marks-from-unicode-cha
 */
public class StringSimplifier {

	public static final char DEFAULT_REPLACE_CHAR = '-';
	public static final String DEFAULT_REPLACE = String.valueOf(DEFAULT_REPLACE_CHAR);
	private static final Map<String, String> NONDIACRITICS = new HashMap();
    static{
		//remove crap strings with no sematics
		NONDIACRITICS.put(".", "");
		NONDIACRITICS.put("\"", "");
		NONDIACRITICS.put("'", "");
			//keep relevant characters as seperation
		NONDIACRITICS.put(" ", DEFAULT_REPLACE);
		NONDIACRITICS.put("]", DEFAULT_REPLACE);
		NONDIACRITICS.put("[", DEFAULT_REPLACE);
		NONDIACRITICS.put(");", DEFAULT_REPLACE);
		NONDIACRITICS.put("(", DEFAULT_REPLACE);
		NONDIACRITICS.put("=", DEFAULT_REPLACE);
		NONDIACRITICS.put("!", DEFAULT_REPLACE);
		NONDIACRITICS.put("/", DEFAULT_REPLACE);
		NONDIACRITICS.put("\\", DEFAULT_REPLACE);
		NONDIACRITICS.put("&", DEFAULT_REPLACE);
		NONDIACRITICS.put(",", DEFAULT_REPLACE);
		NONDIACRITICS.put("?", DEFAULT_REPLACE);
		NONDIACRITICS.put("°", DEFAULT_REPLACE); //remove ?? is diacritic?
		NONDIACRITICS.put("|", DEFAULT_REPLACE);
		NONDIACRITICS.put("<", DEFAULT_REPLACE);
		NONDIACRITICS.put(">", DEFAULT_REPLACE);
		NONDIACRITICS.put(";", DEFAULT_REPLACE);
		NONDIACRITICS.put(":", DEFAULT_REPLACE);
		NONDIACRITICS.put("_", DEFAULT_REPLACE);
		NONDIACRITICS.put("#", DEFAULT_REPLACE);
		NONDIACRITICS.put("~", DEFAULT_REPLACE);
		NONDIACRITICS.put("+", DEFAULT_REPLACE);
		NONDIACRITICS.put("*", DEFAULT_REPLACE);
			//replace non-diacritics as their equivalent chars
		NONDIACRITICS.put("\u0141", "l"); // BiaLystock
		NONDIACRITICS.put("\u0142", "l"); // Bialystock
		NONDIACRITICS.put("ß", "ss");
		NONDIACRITICS.put("æ", "ae");
		NONDIACRITICS.put("ø", "o");
		NONDIACRITICS.put("©", "c");
		NONDIACRITICS.put("\u00D0", "d"); // all Ð ð from http://de.wikipedia.org/wiki/%C3%90
		NONDIACRITICS.put("\u00F0", "d");
		NONDIACRITICS.put("\u0110", "d");
		NONDIACRITICS.put("\u0111", "d");
		NONDIACRITICS.put("\u0189", "d");
		NONDIACRITICS.put("\u0256", "d");
		NONDIACRITICS.put("\u00DE", "th"); // thorn Þ 
		NONDIACRITICS.put("\u00FE", "th"); // thorn þ
    }

	public static String simplifyString(String orig) {
		String str = orig;
		if (str == null) {
			return null;
		}
		str = stripDiacritics(str);
		str = stripNonDiacritics(str);
		/*if( str.length() == 0 ) {
			// Ugly special case to work around non-existing empty strings in oracle. 
            // Store original crapstring as simplified.
			// Would return empty string if oracle could store it.
			return orig;
		}*/
		return str.toLowerCase();
	}

	private static String stripNonDiacritics(String orig) {
		StringBuilder ret = new StringBuilder();
		String lastchar = null;
		for (int i = 0; i < orig.length(); i++) {
			String source = orig.substring(i, i + 1);
			String replace = NONDIACRITICS.get(source);
			String toReplace = replace == null ? String.valueOf(source) : replace;
			if (DEFAULT_REPLACE.equals(lastchar) && DEFAULT_REPLACE.equals(toReplace)) {
				toReplace = "";
			} else {
				lastchar = toReplace;
			}
			ret.append(toReplace);
		}
		if (ret.length() > 0 && DEFAULT_REPLACE_CHAR == ret.charAt(ret.length() - 1)) {
			ret.deleteCharAt(ret.length() - 1);
		}
		return ret.toString();
	}

	/*
	Special regexp char ranges relevant for simplification -> see http://docstore.mik.ua/orelly/perl/prog3/ch05_04.htm	
	InCombiningDiacriticalMarks: special marks that are part of "normal" ä, ö, î etc..
    IsSk: Symbol, Modifier see http://www.fileformat.info/info/unicode/category/Sk/list.htm
    IsLm: Letter, Modifier see http://www.fileformat.info/info/unicode/category/Lm/list.htm
	 */
	public static final Pattern DIACRITICS_AND_FRIENDS 
		= Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");


	private static String stripDiacritics(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
		return str;
	}

}// class
