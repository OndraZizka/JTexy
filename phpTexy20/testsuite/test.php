<?php

error_reporting(E_ALL | E_STRICT);

require_once 'texy.php';


// paths
$dir = dirname(__FILE__);
$inputDir = "$dir/input/";
$outputDir = "$dir/output/";
$refDir = "$dir/ref/";


@mkdir($outputDir);


echo TEXY_VERSION, "\n";
echo PHP_VERSION, "\n";




function process($s, $basename = null)
{

	$texy = new Texy();

    // testing configuration - can be overridden by .conf file
	$texy->encoding = 'windows-1250';
	$texy->linkModule->root         = 'xxx/';
	$texy->linkModule->imageOnClick = 'return !popup(this.href)';
	$texy->imageModule->root        = '../images/';
	$texy->imageModule->linkedRoot    = '../images/big/';
	$texy->imageModule->leftClass   = 'left';
	$texy->imageModule->rightClass  = '';
	$texy->htmlOutputModule->baseIndent  = 1;
	$texy->htmlOutputModule->lineWrap    = 180;
	$texy->allowed['phrase/ins'] = TRUE;
	$texy->allowed['phrase/del'] = TRUE;
	$texy->allowed['phrase/sup'] = TRUE;
	$texy->allowed['phrase/sub'] = TRUE;
	$texy->allowed['phrase/cite'] = TRUE;
	$texy->htmlModule->passComment = TRUE;
	$texy->typographyModule->locale = 'cs';
	$texy->horizLineModule->classes['*'] = 'hidden';

	$conf = "input/$basename.conf";
	if (is_file($conf)) require($conf);

	// just for init test
	$texy->process('');

	// processing
	$html = $texy->process($s);
	$text = $texy->toText();

	$texy->free();

	return $html . "\n\n################\n\n" . $text . "\n\n################\n\n" . $s;
}



// processing
foreach (glob("$inputDir/*.texy") as $fileName)
{
	$basename = basename($fileName, '.texy');
	$outputFile = "$outputDir/$basename.html";
	$refFile = "$refDir$basename.html";

	echo "\n$basename";

	$f = fopen($outputFile, 'wb');
	if (!$f) die("Cannot create $outputFile"); // skip existed file

	$s = process(file_get_contents($fileName), $basename);

	// delete correct or write modified file
	if ($s === file_get_contents($refFile))  {
		fwrite($f, $s);
		fclose($f);
		//@unlink($outputFile);
		echo " *OK*";

	} else {
		fwrite($f, $s);
		fclose($f);
		echo " *MODIFIED*";
	}
}

echo "\n\n*FINISHED*";
