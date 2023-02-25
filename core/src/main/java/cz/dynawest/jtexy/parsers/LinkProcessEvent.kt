
package cz.dynawest.jtexy.parsers;


import cz.dynawest.jtexy.modules.LinkProvider;
import cz.dynawest.jtexy.modules.TexyLink;
import cz.dynawest.jtexy.modules.TexyModifier;
import java.io.Serializable;


/**
 *
 * @author Ondrej Zizka
 */
public class LinkProcessEvent extends AroundEvent implements Serializable
{

	// In.
	public final String dest;
	public final String label;
	public final String modStr;
	public final LinkProvider linkProvider;

	// Out.
	public TexyLink link;
	public TexyLink getLink() {		return link;	}
	public void setLink(TexyLink link) {		this.link = link;	}




	public LinkProcessEvent(
					TexyParser parser,
					TexyModifier modifier,
					String modStr,
					String dest,
					String label,
					LinkProvider linkProvider
	) {
		super(parser, null, modifier);
		this.dest = dest;
		this.label = label;
		this.modStr = modStr;
		this.linkProvider = linkProvider;
	}
  

}
