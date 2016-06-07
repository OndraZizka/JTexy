<?php

/**
 * Parser for single line structures.
 */
class TexyLineParser extends TexyParser
{
	/** @var bool */
	public $again;



	/**
	 * @param  Texy
	 * @param  TexyHtml
	 */
	public function __construct(Texy $texy, TexyHtml $element)
	{
		$this->texy = $texy;
		$this->element = $element;
		$this->patterns = $texy->getLinePatterns();
	}



	/**
	 * @param  string
	 * @return void
	 */
	public function parse($text)
	{
		$tx = $this->texy;

		// initialization
		$pl = $this->patterns;
		if (!$pl) {
			// nothing to do
			$this->element->insert(NULL, $text);
			return;
		}

		$offset = 0;
		$names = array_keys($pl);
		$arrMatches = $arrOffset = array();
		foreach ($names as $name) $arrOffset[$name] = -1;


		// parse loop
		do {
			$min = NULL;
			$minOffset = strlen($text);

			foreach ($names as $index => $name)
			{
				if ($arrOffset[$name] < $offset) {
					$delta = ($arrOffset[$name] === -2) ? 1 : 0;

					if (preg_match($pl[$name]['pattern'],
							$text,
							$arrMatches[$name],
							PREG_OFFSET_CAPTURE,
							$offset + $delta)
					) {
						$m = & $arrMatches[$name];
						if (!strlen($m[0][0])) continue;
						$arrOffset[$name] = $m[0][1];
            // Throws off offsets and transforms to 1-dim array.
						foreach ($m as $keyx => $value) $m[$keyx] = $value[0];

					} else {
						// try next time
						continue;
					}
				} // if

				if ($arrOffset[$name] < $minOffset) {
					$minOffset = $arrOffset[$name];
					$min = $name;
				}
			} // foreach

			if ($min === NULL) break;

			$px = $pl[$min];
			$offset = $start = $arrOffset[$min];

      // Call the handler of the minimal match.
			$this->again = FALSE;
			$res = call_user_func_array(
				$px['handler'],
				array($this, $arrMatches[$min], $min)
			);

      // @ Convert DOM fragment to a HTML snippet.
			if ($res instanceof TexyHtml) {
				$res = $res->toString($tx);
			} elseif ($res === FALSE) {
        // @DavidGrudl porušuje svoji vlastní zásadu: http://bit.ly/1iGf7Q , $arrOffset[$min] = -2; #Texy #JTexy
				$arrOffset[$min] = -2;
				continue;
			}

      // In-place replacement in the string.
      // @ StringUtils.overlay( ... );
			$len = strlen($arrMatches[$min][0]);
			$text = substr_replace(
				$text,
				(string) $res,
				$start,
				$len
			);

			// @ Adjust all patterns' offset.
			$delta = strlen($res) - $len;
			foreach ($names as $name) {
				// @ If this pattern's offset is before the left-most match, reset it back to start.
				if ($arrOffset[$name] < $start + $len) $arrOffset[$name] = -1;
				// @ Otherwise, set it after the matched region.
				else $arrOffset[$name] += $delta;
			}

			if ($this->again) {
				$arrOffset[$min] = -2;
			} else {
				$arrOffset[$min] = -1;
				$offset += strlen($res);
			}

		} while (1);

    // @ This puts a Protected-HTML snippet into the DOM (parsing it first to a DOM?).
		$this->element->insert(NULL, $text);
	}

}
?>
