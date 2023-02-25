
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.RegexpPatterns;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.events.TexyEvent;
import cz.dynawest.jtexy.parsers.BeforeParseEvent;
import cz.dynawest.jtexy.parsers.BeforeParseListener;
import cz.dynawest.jtexy.parsers.LinkProcessEvent;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.JTexyStringUtils;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.jtexy.util.StringsReplaceCallback;
import cz.dynawest.jtexy.util.UrlChecker;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;

/**
 *
 * @author Ondrej Zizka
 */
public class ImageModule extends TexyModule
{
    private static final Logger log = Logger.getLogger( ImageModule.class.getName() );


    // --- Module meta-info --- //

    @Override protected PatternHandler getPatternHandlerByName(String name) {
        if( imagePH.getName().equals(name) )
            return imagePH;
        return null;
    }

    @Override public TexyEventListener[] getEventListeners() {
        return new TexyEventListener[]{ beforeParse, imageListener };
    }



    // --- Settings --- //

    /** Prefix to be prepended to all images src="...". */
    public String urlPrefix = "";

    /** Default images alt="...". */
    public String defaultAlt = "";

    public String leftClass;
    public String rightClass;

    public String fileRoot;

    public String onLoadJS;



    /**
     * Image pattern handler - 
     * [* small.jpg 80x13 | small-over.jpg | big.jpg .(alternative text)[class]{style}>]:LINK.
     */
    protected PatternHandler imagePH = new PatternHandler() {
        @Override public final String getName(){ return "imagePattern"; }

        @Override public final Node handle( TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern) throws TexyException
        {
            //    [1] => URLs
            //    [2] => .(title)[class]{style}<>
            //    [3] => * < >
            //    [4] => url | [ref] | [*image*]
            for (MatchWithOffset match : groups) {
                log.finest("  "+match.toString()); ///
            }

            String urls    = groups.get(1).match.trim();
            String align   = groups.get(3).match;
            String modStr  = StringUtils.defaultString(groups.get(2).match) + align.trim();
                             TexyModifier mod = new TexyModifier( modStr );
            String linkStr = groups.get(4).match;

            ImageInfo img = createImage(urls);
            if( null == img )
                return null;

            TexyLink link = null;
            if( ! "".equals( linkStr )){
                if( ":".equals(linkStr) ){
                    link = new TexyLink( img.linkedUrl != null ? img.linkedUrl : img.url);
                    link.raw = ":";
                    link.type = TexyLink.Type.IMAGE;
                }
                else {
                    // @ Fire a processLinkEvent instead of calling LinkModule directly.
                    LinkProcessEvent linkEvent = new LinkProcessEvent(
                                    parser, mod, modStr, urls, null, LINK_PROVIDER);
                    parser.getTexy().invokeAroundHandlers(linkEvent);
                    link = linkEvent.getLink();
                }
            }

            ImageEvent ev = new ImageEvent(parser, img, link, mod);
            return parser.getTexy().invokeAroundHandlers(ev);
        }
    };





    /**
     *  BeforeParseListener
     */
    public final BeforeParseListener beforeParse = new BeforeParseListener() {

        @Override public Class getEventClass() { return BeforeParseEvent.class; }

        @Override
        public Node onEvent(TexyEvent event_) throws TexyException {
            BeforeParseEvent event = (BeforeParseEvent) event_;

            // [*image*]: urls .(title)[class]{style}
            /*$text = preg_replace_callback(
                '#^\[\*([^\n]+)\*\]:\ +(.+)\ *'.TEXY_MODIFIER.'?\s*()$#mUu',
                array($this, 'patternReferenceDef'),
                $text
            );*/

            // Since patternReferenceDef() returns "", callback is not necessary,
            // but once written this way...
            final String REGEX = "(?mUu)^\\[\\*([^\\n]+)\\*\\]:\\ +(.+)\\ *"+RegexpPatterns.TEXY_MODIFIER+"?\\s*()$";
            StringsReplaceCallback cbProcessReference = new StringsReplaceCallback() {
                @Override public String replace(String[] groups) {
                    return patternReferenceDef( groups ); // Needs $this.
                }
            };
            String res = JTexyStringUtils.replaceWithCallback(event.getText(), REGEX, cbProcessReference);

            event.setText(res);
            return null;
        }

        //@Override public String beforeParse(JTexy texy, String text, boolean singleLine) {}
    };




    /**
     *  ImageEventListener
     */
    ImageEventListener imageListener = new ImageEventListener() {
        @Override public Class getEventClass(){ return ImageEvent.class; }

        @Override public Node onEvent(ImageEvent event) throws TexyException
        {
            ImageInfo img = event.img;

            TexyModifier mod = img.modifier;
            String alt = mod.title;
            mod.title = null;
            String hAlign = mod.hAlign;
            mod.hAlign = null;

            DOMElement elm = new DOMElement("img");
            mod.decorate(elm);
            elm.setAttribute("src", JTexyStringUtils.prependUrlPrefix( urlPrefix, img.url ) );

            // alt
            if( null == elm.getAttributeNode("alt") ){
                elm.setAttribute("alt", defaultAlt);
            }

            // hAlign
            if( null != hAlign ){
                String cls = null;
                if( "left".equals(hAlign) ){
                    cls = leftClass;
                }else if ( "left".equals(hAlign) ){
                    cls = rightClass;
                }
                if( cls != null )
                    elm.setAttribute("class", elm.getAttribute("class")+" "+cls);
            }


            // Width and height.

            // org.apache.poi.ss.util.ImageUtils ?
            // org.apache.sanselan.ImageParser ?
            String imgPath = fileRoot + img.url;
            File imgFile = new File(imgPath);
            if( img.width == null && img.height == null )
            if( imgFile.isFile() ){
                // TBD: Compute the dimensions if only one set.
                log.finest("Reading image from: "+imgPath);
                Dimension imgDim = getImageSizeB(imgPath);
                img.width = imgDim.width;
                img.height = imgDim.height;
                double ratio = img.width / img.height;
            }

            if( null != img.width )  elm.setAttribute("width",  "" + img.width);
            if( null != img.height)  elm.setAttribute("height", "" + img.height);

            // onmouseover 
            if( img.overUrl != null ){
                // TBD: onmoouseover
                String overSrc = JTexyStringUtils.prependUrlPrefix(urlPrefix, img.overUrl);
                elm.setAttribute("onmouseover", "this.src='" + StringEscapeUtils.escapeJavaScript(overSrc) + "'");
                elm.setAttribute("onmouseout", "this.src='" + StringEscapeUtils.escapeJavaScript(elm.getAttribute("src")) + "'");
                if( onLoadJS != null )
                    elm.setAttribute("onload",  onLoadJS.replace("%i", overSrc) );
                // TBD: $tx->summary['preload'][] = $overSrc;
            }

            allDocumentImages.add( img );

            // TBD: if ($link) return $tx->linkModule->solve(NULL, $link, $el);
            if( event.link != null ){
                // TODO: Add the additional element to the LinkEvent.
                // TBD:  Extract some method to avoid firing an event?
                //return getTexy().invokeAroundHandlers( new LinkModule.LinkEvent(null, event.link, elm))
                log.warning("Image link feature not implemented yet.");
            }

            return elm;

        }
    };// imageListener


    // TODO: Reset in BeforeParseEventListener.
    // TBD:  Refactor to have this in some ParsingContext or DocumentContext.
    private List<ImageInfo> allDocumentImages = new ArrayList();


    /** Reads image's size. */
    private static Dimension getImageSize( String filePath ){
        if( log.isLoggable(Level.FINE) )  log.fine("Reading image: " + filePath);
        try {
            File file = new File(filePath);
            BufferedImage img = ImageIO.read(file);
            if( null == img )  return null;
            return new Dimension( img.getWidth(), img.getHeight() );
        } catch( IOException ex ) {
            return null;
        }
    }

    /** Reads image's size. Might be faster. */
    private static Dimension getImageSizeB( String filePath ){
        File file = new File(filePath);
        if( !file.exists() ) return null;
        if( log.isLoggable(Level.FINE) )  log.fine("Reading image: " + filePath);

        try{
            ImageInputStream in = ImageIO.createImageInputStream( file );
            try {
                final Iterator readers = ImageIO.getImageReaders( in );
                if( !readers.hasNext() )  return null;
                ImageReader reader = (ImageReader) readers.next();
                try {
                    reader.setInput( in );
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                }
                finally {	reader.dispose(); }
            }
            finally { if( in != null )  in.close(); }
        }
        catch( Throwable ex ){ return null; }
    }






    // References - predefined images. //


    // References map 
    private Map<String, ImageInfo> refs = new HashMap();
    protected void setRef(String key, ImageInfo img){
        this.refs.put( key.toLowerCase(), img);
    }
    protected ImageInfo getRef( String key ){
        ImageInfo img = this.refs.get(key.toLowerCase());
        if( null == img )  return null;
        return img.clone(); // Cloning because we will change it.
    }

    /**  Returns reference using this module's link reference map. */
    protected final LinkProvider LINK_PROVIDER = new LinkProvider() {
        @Override public TexyLink getLink( String key ) {
            ImageInfo img = getRef(key);
            if( null == img ) return null;
            TexyLink link = new TexyLink( img.linkedUrl == null ? img.url : img.linkedUrl );
            link.modifier = img.modifier;
            return link;
        }
    };






    /**
     * Converts image reference string, using module's reference definitions.
     * Used as a callback in beforeParse.
     * Callback for: [*image*]: urls .(title)[class]{style}.
     */
    private String patternReferenceDef( String[] groups ) {
        //    [1] => [* (reference) *]
        //    [2] => urls
        //    [3] => .(title)[class]{style}<>
        String ref = groups.length > 1     ?  groups[1] : null;
        String urls = groups.length > 2    ?  groups[2] : null;
        String modStr = groups.length > 3  ?  groups[3] : null;
        TexyModifier mod = new TexyModifier(modStr);

        ImageInfo img = this.getRef(ref);
        if( null == img )
            img = ImageModule.createImage(ref);
        img.modifier = mod;
        this.setRef(ref, img);
        return "";
    }





    /**
     * Image information.  Texy: part of factoryImage().
     */
    public static ImageInfo createImage( String def ){
        /* Moved elsewhere to allow this method to be static.
        ImageInfo img = getRef(def);
        if( null != img )
            return img;
         */

        if( StringUtils.isBlank(def) )
            return null;

        String[] parts = StringUtils.split(def, '|');
        ImageInfo img = new ImageInfo();

        // Dimensions?     "bla/bla.png 50x50"
        Pattern pat = Pattern.compile("(?U)^(.*) (\\d+|\\?) *(X|x) *(\\d+|\\?) *()$");
        Matcher mat = pat.matcher(parts[0]);
        if( mat.matches() ){
            img.url = mat.group(1);
            img.asMax = "X".equals( mat.group(3) );
            String i = mat.group(2);
            img.width = "?".equals( i ) ? null : NumberUtils.createInteger(i);
            i = mat.group(4);
            img.height = "?".equals( i ) ? null : NumberUtils.createInteger(i);
        }else{
            img.url = def.trim();
        }

        if( ! UrlChecker.checkURL(img.url, UrlChecker.Type.IMAGE))
            img.url = null;

        // onmouseover image
        if( parts.length > 1 ){
            String tmp = parts[1].trim();
            if( ! tmp.equals("") && UrlChecker.checkURL(tmp, UrlChecker.Type.ANCHOR) )
                img.overUrl = tmp;
        }

        // linked image
        if( parts.length > 2 ){
            String tmp = parts[2].trim();
            if( ! tmp.equals("") && UrlChecker.checkURL(tmp, UrlChecker.Type.ANCHOR) )
                img.linkedUrl = tmp;
        }

        return img;
    }



    /** Image information. */
    public static class ImageInfo implements Cloneable {

        protected String url;
        protected String overUrl;
        protected String linkedUrl;

        protected boolean asMax;
        protected Integer width;
        protected Integer height;

        protected TexyModifier modifier = new TexyModifier();
        protected String refName;

        public ImageInfo() {	}
        public ImageInfo(String url, boolean asMax, Integer width, Integer height) {
            this.url = url;
            this.asMax = asMax;
            this.width = width;
            this.height = height;
        }

        @Override protected ImageInfo clone(){
            return new ImageInfo(url, asMax, width, height);
        }
    }

	
}
