package cz.dynawest.jtexy.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.File;
import javax.swing.JPanel;

/**
 *
 * @author Ondrej Zizka
 */
public class ImageUtilsAwt extends JPanel {

    private static Image image;

    public ImageUtilsAwt( File file ) {
        image = Toolkit.getDefaultToolkit().getImage( file.getPath() );
        rightSize();
    }

    private void rightSize() {
        int width = image.getWidth( this );
        int height = image.getHeight( this );
        if( width == -1 || height == -1 ) {
            return;
        }
        System.out.println( "Image width: " + width );
        System.out.println( "Image height" + height );
    }

    public boolean imageUpdate( Image img, int infoflags, int x, int y,
            int width, int height ) {
        if( (infoflags & ImageObserver.ERROR) != 0 ) {
            System.out.println( "Error loading image!" );
            System.exit( -1 );
        }
        if(    (infoflags & ImageObserver.WIDTH)  != 0
            && (infoflags & ImageObserver.HEIGHT) != 0 ) {
            rightSize();
        }
        if( (infoflags & ImageObserver.SOMEBITS) != 0 ) {
            repaint();
        }
        if( (infoflags & ImageObserver.ALLBITS) != 0 ) {
            rightSize();
            return false;
        }
        return true;
    }


    public static void main( String[] args ) throws Exception {
        String file = "logo.png";
        new ImageUtilsAwt( new File(file) );
    }
}