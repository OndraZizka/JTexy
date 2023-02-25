
package cz.dynawest.jtexy.parsers;


import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.util.MatchWithOffset;
import java.util.Comparator;
import java.util.List;




/**
 * Stores information about match while parsing.
 * @author Ondrej Zizka
 */
class ParserMatchInfo implements Comparable<ParserMatchInfo>
{
	public final RegexpInfo pattern;
	public final List<MatchWithOffset> groups;
	public final int offset;
	public final int priority;

	public ParserMatchInfo(RegexpInfo pattern, List<MatchWithOffset> groups, int offset, int priority) {
		this.pattern = pattern;
		this.groups = groups;
		this.offset = offset;
		this.priority = priority;
	}

	public ParserMatchInfo(RegexpInfo ri, List<MatchWithOffset> groups, int offset) {
		this(ri, groups, offset, 0);
	}

	public String toString(){
		return (pattern == null ? "(no pattern)" : pattern.name) + " @ " + this.offset + ", 0th group: "+(groups == null ? "(groups == null)" : groups.get(0) );
	}


	/**
	 * Compares two matches by offset; if equal, by priority.
	 * Null objects go last.
	 */
	@Override
	public int compareTo(ParserMatchInfo o) {
		if( null == o ) return 1;

		int diff = this.offset - o.offset;
		if( diff != 0 ) return diff;

		diff = this.priority - o.priority;
		if( diff != 0 ) return diff;

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {				return false;			}
		if (getClass() != obj.getClass()) {				return false;			}
		final ParserMatchInfo other = (ParserMatchInfo) obj;
		if (this.offset != other.offset) {				return false;			}
		if (this.priority != other.priority) {				return false;			}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.offset;
		hash = 17 * hash + this.priority;
		return hash;
	}

	
	/** Compares by offset, then priority. Null objects go last. */
	public static final Comparator<ParserMatchInfo> COMPARATOR = new Comparator<ParserMatchInfo>() {
		@Override
		public int compare(ParserMatchInfo pmi1, ParserMatchInfo pmi2)
		{
			int diff = pmi1.offset - pmi2.offset;
			if( diff != 0 ) return diff;

			diff = pmi1.priority - pmi2.priority;
			if( diff != 0 ) return diff;

			return 0;
		}
	};


}// ParserMatchInfo
