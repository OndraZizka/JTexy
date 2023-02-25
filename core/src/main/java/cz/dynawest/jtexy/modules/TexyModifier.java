
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.JTexy;
import java.util.*;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.dom4j.dom.DOMElement;
import org.dom4j.tree.FlyweightAttribute;

/**
 * Modifier processor.
 *
 * Modifiers are texts like .(title)[class1 class2 #id]{color: red}>^
 *   .         starts with dot
 *   (...)     title or alt modifier
 *   [...]     classes or ID modifier
 *   {...}     inner style modifier
 *   < > <> =  horizontal align modifier
 *   ^ - _     vertical align modifier
 *
 * @author     Ondrej Zizka
 * @author     David Grudl
 */
public final class TexyModifier implements Cloneable {
    private static final Logger log = Logger.getLogger( TexyModifier.class.getName() );
	
    
	public String id, hAlign, vAlign, title, cite;


	/** CSS classes. */
	public Set<String> classes = new LinkedHashSet<String>();

	/** CSS styles */
	public Map<String, String> styles = new LinkedHashMap();
	
	/** HTML element attributes */
	public Map<String, String> attrs = new LinkedHashMap();


	
	/** Set of properties which are regarded as HTML element attributes. */
	public static Set<String> elAttrs;

	static {

		// Create the set of allowed HTML attributes.
		String strAttrs =
			"abbr,accesskey,align,alt,archive,axis,bgcolor,cellpadding" +
			"cellspacing,char,charoff,charset,cite,classid,codebase,codetype" +
			"colspan,compact,coords,data,datetime,declare,dir,face,frame" +
			"headers,href,hreflang,hspace,ismap,lang,longdesc,name" +
			"noshade,nowrap,onblur,onclick,ondblclick,onkeydown,onkeypress" +
			"onkeyup,onmousedown,onmousemove,onmouseout,onmouseover,onmouseup,rel" +
			"rev,rowspan,rules,scope,shape,size,span,src,standby" +
			"start,summary,tabindex,target,title,type,usemap,valign" +
			"value,vspace";
		
		String[] attrs = StringUtils.split(strAttrs, ',');
		TexyModifier.elAttrs = new HashSet( Arrays.asList(attrs) );

	}

	
	/** Const */
	public TexyModifier() { }
	public TexyModifier( String modifierString ) {
		this.parse( modifierString );
	}

	

	/** Deep clone. */
	@Override
	public TexyModifier clone()
	{
		TexyModifier ret = new TexyModifier("");
		ret.id     = this.id;
		ret.hAlign = this.hAlign;
		ret.vAlign = this.vAlign;
		ret.title  = this.title;
		ret.cite   = this.cite;

		ret.classes = new LinkedHashSet(classes);
		ret.styles  = new LinkedHashMap(styles);
		ret.attrs   = new LinkedHashMap(attrs);

		return ret;
	}






	/**
	 * Parses the modifier string and updates the internal data accordingly.
	 * 
	 * @param modifierString  .(title)[class]{style/attribs}<>
	 */
	public void parse( String modifierString )
	{
		if( StringUtils.isEmpty(modifierString) )
			return;

		int pos = 0;
		int len = modifierString.length();

		while( pos < len ){

			char ch = modifierString.charAt( pos );

			switch( ch ){

				// #id and classes
				case '[': {
					int pos2 = modifierString.indexOf(']', pos+1) + 1;
					String str = modifierString.substring( pos+1, pos2-1 );
                    String[] parts = StringUtils.split(str);
                    for( String part : parts ) {
                        if(part.startsWith("#") && part.length() > 1)
                            this.id = part.substring(1);
                        else
                            this.classes.add(part);
                    }
					pos = pos2;
					break;
				}

				// Title
				case '(': {
					int pos2 = modifierString.indexOf(')', pos+1) + 1;
					this.title = modifierString.substring( pos+1, pos2-1 );
					pos = pos2;
					break;
				}

				// Style & attributes
				case '{': {
					int pos2 = modifierString.indexOf('}', pos+1) + 1;
					String code = modifierString.substring( pos+1, pos2-1 );

					String[] split = StringUtils.split( code, ';' );
					for (int i = 0; i < split.length; i++) {
						String elem = split[i];
						String[] pair = StringUtils.split(elem, ":", 2);
						String name = pair[0].trim().toLowerCase();
						String val = pair.length != 2 ? null : pair[1].trim();

						if( TexyModifier.elAttrs.contains(name) )
							this.attrs.put(name, StringUtils.defaultIfEmpty(val, "true"));
                        else if( null != val )
							this.styles.put(name, val);
					}

					pos = pos2;
					break;
				}

				// Alignment.
                case '*': pos++; break;  // Image without align. [*...*]
				case '^': this.vAlign = "top";     pos++; break;
				case '-': this.vAlign = "middle";  pos++; break;
				case '_': this.vAlign = "bottom";  pos++; break;
				case '=': this.hAlign = "justify"; pos++; break;
				case '>': this.hAlign = "right";   pos++; break;
				// @ elseif (substr($mod, $p, 2) === '<>': this.hAlign = "center"; $p+=2; }
				case '<':{
					if( modifierString.length() > pos+1 && modifierString.charAt(pos+1) == '>' ){
						this.hAlign = "center";
						pos += 2;
					}else{
						this.hAlign = "left"; pos++;
					}
					break;
				}
                    
                    
                default: {
                    // Malformed - log WARNING and skip to first valid char.
                    log.warning("Malformed Texy modifier at position "+pos+": " + modifierString);
                    String sub = modifierString.substring(pos);
                    int skip = StringUtils.indexOfAny(sub, "({[^-_=><");
                    if( skip < 1 ){
                        log.warning("Skipping the rest of string.");
                        return;
                    }
                    pos = pos + skip;
                    log.warning("Skipping to position " + pos);
                    break;
                }

			}// switch( ch );
		
		}// while()

	}// parse()



	/**
	 * Decorates HTML element according to this modifier.
	 * @param elm
	 */
	public void decorate( JTexy texy, DOMElement elm ){
		decorate(elm);
	}

    
	/**
	 * Decorates HTML element according to this modifier.
	 */
	public void decorate( DOMElement elm ){
		
        /* Various */
        if( ! StringUtils.isBlank(this.id))      elm.add( new FlyweightAttribute("id",     this.id) );
		if( ! StringUtils.isBlank(this.hAlign))  elm.add( new FlyweightAttribute("halign", this.hAlign) );
		if( ! StringUtils.isBlank(this.vAlign))  elm.add( new FlyweightAttribute("valign", this.vAlign) );
		if( ! StringUtils.isBlank(this.title))   elm.add( new FlyweightAttribute("title",  this.title) );
		if( ! StringUtils.isBlank(this.cite))    elm.add( new FlyweightAttribute("cite",   this.cite) );

        /* CSS classes. */
        if( ! this.classes.isEmpty() )  
            elm.add( new FlyweightAttribute("class", StringUtils.join(this.classes, ' ') ) );

        /* CSS styles. */
        if( ! this.styles.isEmpty() )  
    		elm.add( new FlyweightAttribute("style", TexyModifier.mapToCSS(this.styles) ) );

        /* HTML element attributes. */
        for (Map.Entry<String, String> attr : this.attrs.entrySet()) {
            elm.add( new FlyweightAttribute( attr.getKey(), attr.getValue()) );
        }
        
	}

    
    /**
     * @param   String -> String map of CSS rules.
     * @return  "key1:val1; key2:val2"
     */
    private static String mapToCSS(Map<String, String> styles) {
        if( styles.size() == 0 )  return "";
        StringBuilder sb = new StringBuilder( styles.size() * 20);
        for (Map.Entry<String, String> rule : styles.entrySet()) {
            sb.append( rule.getKey() ).append(":").append( rule.getValue() ).append(";");
        }
        return sb.substring(0, sb.length()-1);
    }

    @Override
    public String toString() {
        return "TexyModifier{" + "id=" + id + ", hAlign=" + hAlign + ", vAlign=" + vAlign + 
                ", title=" + title + ", cite=" + cite + ", classes='" + StringUtils.join(this.classes, ' ') + 
                "', styles " + styles.size() + ", attrs " + attrs.size() + '}';
    }
    

}
