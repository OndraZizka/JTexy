package cz.dynawest.jtexy.events

import cz.dynawest.jtexy.JTexy

/**
 * At the end of processing; currently used by HtmlOutputModule.
 *
 * @author Ondrej Zizka
 */
class PostProcessEvent(texy: JTexy?, text: String?) : TexyEvent(text)
