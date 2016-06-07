<?php
require_once('texy.php');

header("Content-Type: text/plain");

$text = <<<EOF
Odkazy
------

- 1. odkaz <a href="http://creativecommons.org/license/">pokus</a>
- 2. odkaz <a href=[*obrazek3*] title="ahoj" >pokus</a>
- 3. odkaz <a href='aa'></a> prázdný.
- 4. odkaz <a>test</a> prázdný

This <strong>example</strong> shows how <a href=http://www.texy.info title='Texy! excellent text-to-html converter & formatter'>Texy!</a> handle HTML tags.

- 41. Referenční odkaz [*obrazek1*]:[la trine2]
- 42. Referenční odkaz [*obrazek2*]:[/trine/]x
[*obrazek3*]::
"test .(kuk)":http://www.moje.cz
"La Trine":[*obrazek2*]
EOF;

$text = "Beží **Žižka** k //Táboru//, nese `pytel` zázvoru.";

$text = "Look at my \"blog\":http://www.blog.cz/.";

$text = <<<EOF
Nadpisy
*******

Nadpis 1
========

Nadpis 2
========

Nadpis 3
--------

EOF;


  $texy = new Texy();
  for( $i = 0; $i < 1000; $i++ ){
    $html = $texy->process($text);
  }
  print $html;
?>
