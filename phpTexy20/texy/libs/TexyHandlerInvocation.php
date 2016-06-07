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
 * Around advice handlers.
 *
 * @author     David Grudl
 * @copyright  Copyright (c) 2004, 2009 David Grudl
 * @package    Texy
 */
final class TexyHandlerInvocation extends TexyObject
{
	/** @var array of callbacks */
	private $handlers;

	/** @var int  callback counter */
	private $pos;

	/** @var array */
	private $args;

	/** @var TexyParser */
	private $parser;
	public function getParser(){ return $this->parser; }
	public function getTexy()	{		return $this->parser->getTexy(); }



	/**
	 * @param  array    array of callbacks
	 * @param  TexyParser
	 * @param  array    arguments
	 */
	public function __construct($handlers, TexyParser $parser, $args)
	{
		$this->handlers = $handlers;
		$this->pos = count($handlers);
		$this->parser = $parser;
		array_unshift($args, $this);
		$this->args = $args;
	}



  // @ Calling proceed() repeatedly goes thru handlers from the recently added to the older ones.
  // @ Successive proceed() calls are probably done from within the handlers, if they decide it's needed.
	/**
	 * @param  mixed
	 * @return mixed
	 */
	public function proceed()
	{
		if ($this->pos === 0) {
			throw new InvalidStateException('No more handlers.');
		}

    // @ Allows to call proceed() with custom params (?).
		if (func_num_args()) {
			$this->args = func_get_args();
			array_unshift($this->args, $this);
		}

		$this->pos--;
		$res = call_user_func_array($this->handlers[$this->pos], $this->args);
		if ($res === NULL) {
			throw new UnexpectedValueException("Invalid value returned from handler '" . print_r($this->handlers[$this->pos], TRUE) . "'.");
		}
		return $res;
	}



	/** PHP garbage collector helper. */
	public function free(){
		$this->handlers = $this->parser = $this->args = NULL;
	}

}
