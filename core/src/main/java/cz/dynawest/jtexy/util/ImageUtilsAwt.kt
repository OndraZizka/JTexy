package cz.dynawest.jtexy.util

import java.awt.Image
import java.awt.Toolkit
import java.io.*
import javax.swing.JPanel

/**
 *
 * @author Ondrej Zizka
 */
class ImageUtilsAwt(file: File) : JPanel() {

    private var image: Image  = Toolkit.getDefaultToolkit().getImage(file.path)
    init {
        rightSize()
    }

    private fun rightSize() {
        val width = image.getWidth(this)
        val height = image.getHeight(this)
        if (width == -1 || height == -1) {
            return
        }
        println("Image width:  $width") ///
        println("Image height: $height")///
    }

    override fun imageUpdate(
        img: Image, infoflags: Int, x: Int, y: Int,
        width: Int, height: Int
    ): Boolean {
        if (infoflags and ERROR != 0) {
            println("Error loading image!")
            System.exit(-1)
        }
        if (infoflags and WIDTH != 0
            && infoflags and HEIGHT != 0
        ) {
            rightSize()
        }
        if (infoflags and SOMEBITS != 0) {
            repaint()
        }
        if (infoflags and ALLBITS != 0) {
            rightSize()
            return false
        }
        return true
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val file = "logo.png"
            ImageUtilsAwt(File(file))
        }
    }
}
