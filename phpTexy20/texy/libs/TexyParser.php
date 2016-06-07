<?php

/**
 * Texy! - web text markup-language
 * --------------------------------
 *
 * Copyright (c) 2004, 2009 David Grudl (http://davidgrudl.com)
 *
 * This source file is subject to the GNU GPL license that is bundled
 * with this package in the file license.txt.
 *
 * For more information please see http://texy.info
 *
 * @copyright  Copyright (c) 2004, 2009 David Grudl
 * @license    GNU GENERAL PUBLIC LICENSE version 2 or 3
 * @link       http://texy.info
 * @package    Texy
 */



/**
 * Texy parser base class.
 *
 * @author     David Grudl
 * @copyright  Copyright (c) 2004, 2009 David Grudl
 * @package    Texy
 */
class TexyParser extends TexyObject
{
	/** @var Texy */
	protected $texy;
  public function getTexy(){ return $this->texy; }

	/** @var TexyHtml  */
	protected $element;

	/** @var array */
	public $patterns;
  
}




include 'TexyBlockParser.php';
include 'TexyLineParser.php';














