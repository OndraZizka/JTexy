package cz.dynawest.jtexy.modules;

import cz.dynawest.openjdkregex.Pattern;


/**
 * ListType types.
 * 
 * @author Martin Večeřa
 */
enum ListType {

	//             first-rexexp          ordered?  list-style-type   next-regexp
	STAR    ("*",  "\\*\\ ",               false, null,  null),
	MINUS   ("-",  "[\\u2013-](?![>-])",   false, null,  null),
	PLUS    ("+",  "\\+\\ ",               false, null,  null),
	ARABDOT ("1.", "1\\.\\ ",/* not \\d !*/true,  null,          "\\d{1,3}\\.\\ "),
	ARABPAR ("1)", "\\d{1,3}\\)\\ ",       true,  null,  null),
	ROMANDOT("I.", "I\\.\\ ",              true,  "upper-roman", "[IVX]{1,4}\\.\\ "),
	ROMANPAR("I)", "[IVX]+\\)\\ ",         true,  "upper-roman", null), // before A) !
	ALPHALO ("a)", "[a-z]\\)\\ ",          true,  "lower-alpha", null),
	ALPHAUP ("A)", "[A-Z]\\)\\ ",          true,  "upper-alpha", null);


	private final String type;
	private final String firstRegExp;
	private final boolean ordered;
	private final String listStyleType;
	private final String nextRegExp;
	private final Pattern pattern;

	private ListType(String type, String firstRegExp, boolean ordered, String listStyleType, String nextRegExp) {
		this.type = type;
		this.firstRegExp = firstRegExp;
		this.ordered = ordered;
		this.listStyleType = listStyleType;
		this.nextRegExp = nextRegExp;
		this.pattern = Pattern.compile("^" + firstRegExp, Pattern.UNICODE_CASE);
	}

    public String getNextOrFirstRegExp(){
        return this.nextRegExp != null ? this.nextRegExp : this.firstRegExp;
    }

	public Pattern getPattern() {		return pattern;	}
	public String getFirstRegExp() {		return firstRegExp;	}
	public String getListStyleType() {		return listStyleType;	}
	public String getNextRegExp() {		return nextRegExp;	}
	public boolean isOrdered() {		return ordered;	}
	public String getType() {		return type;	}

}
