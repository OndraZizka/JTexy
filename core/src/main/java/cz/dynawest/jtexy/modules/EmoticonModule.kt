package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.BeforeParseEvent;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.jtexy.util.SimpleImageSize;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;

/**
 * Emoticon module.
 *
 * @author Ondrej Zizka
 */
public class EmoticonModule extends TexyModule
{
    
    private boolean enabled = false;


	/** Supported emoticons and image files */
	public static final Map<String,String> ICONS = new HashMap();
    static{
		ICONS.put(":-)", "smile.gif");
		ICONS.put(":-(", "sad.gif");
		ICONS.put(";-)", "wink.gif");
		ICONS.put(":-D", "biggrin.gif");
		ICONS.put("8-O", "eek.gif");
		ICONS.put("8-)", "cool.gif");
		ICONS.put(":-?", "confused.gif");
		ICONS.put(":-x", "mad.gif");
		ICONS.put(":-P", "razz.gif");
		ICONS.put(":-|", "neutral.gif");
    }

	/** CSS class for emoticons */
	private String cssClass;

	/** Root of relative images (default value is texy.imageModule.root) */
	private String root;

	/** Physical location of images on server (default value is texy.imageModule.fileRoot) */
	private String fileRoot;

    private RegexpInfo regexpInfo;



    // "Register" listeners.
    @Override public TexyEventListener[] getEventListeners() {
        return new TexyEventListener[]{

            // BeforeParseEvent. TODO: Move to some void init(JTexy texy) callback.
            new TexyEventListener<BeforeParseEvent>() {
                @Override public Class getEventClass() { return BeforeParseEvent.class; }
                @Override public Node onEvent(BeforeParseEvent event) throws TexyException {
                    if( ! EmoticonModule.this.enabled )  return null;
                    EmoticonModule.this.regexpInfo = EmoticonModule.this.createRegexpInfo();
                    EmoticonModule.this.getTexy().addPattern( regexpInfo );
                    return null;
                }
            },

        };
    }

    
    /**
     *  Just one pattern handler - emoticon.
     */
    @Override protected PatternHandler getPatternHandlerByName(String name) {
        return new PatternHandler() {
            @Override public String getName() { return "emoticon"; }

            @Override
            public Node handle( TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern ) throws TexyException {
                String raw = groups.get(1).match;
                return createEmotionElement( findEmoticon( raw ), raw );
            }
        };
    }


    /**
     *  Finds the emoticon as a prefix.
     */
    private String findEmoticon(String text) {
        // For each emoticon...
        for( String key : ICONS.keySet() ) {
            if( text.startsWith( key ) )  return key;
        }
        assert false;
        return text; // Shouldn't happen.
    }


    /**
     *  RegexpInfo. Created upon init.
     */
    private RegexpInfo createRegexpInfo() throws TexyException{
        if( ICONS.isEmpty() )
            throw new TexyException("List of icons is empty.");

        StringBuilder regex = new StringBuilder("(?<=^|[\\x00-\\x20])(");
        for( String emo : ICONS.keySet() ){
            regex.append( emo ).append("+|");
        }
        regex.delete( regex.length() -3, regex.length() -1 ); // Cut last +|
        regex.append(")");

        return RegexpInfo.fromRegexp( regex.toString(), RegexpInfo.Type.LINE, "emoticon" );
	}



    /**
     *  Creates the emoticon element.
     */
    public DOMElement createEmotionElement( String emoticon, String raw )
    {
        String iconFileName = ICONS.get(emoticon);
        DOMElement el = new DOMElement("img");
        el.setAttribute("src", new File(this.root, iconFileName).getPath());
        el.setAttribute("alt", raw);
        el.setAttribute("class", cssClass);

        // Actual file - check size.
        File file = new File(this.fileRoot, iconFileName);
        if( file.exists() ) try {
            SimpleImageSize size = new SimpleImageSize( file );
            el.setAttribute("width", "" + size.getWidth());
            el.setAttribute("height", "" + size.getHeight());
        } catch( /*IO*/Exception ex ) {  }

        //this.jtexy.summary.getImages().add( el.getAttribute("src") );
        return el;
    }

}
