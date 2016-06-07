<?php

// This is used in JTexyPerfTest.java .
$text = <<<EOF
Title  .[main-title]
*****

Hello //world!//
How are you?
 I'm fine. Look at my "blog":http://www.blog.cz/. And my photo: [* img/me.png *]

Subtitle
========

/--code java
System.out.println("That's all.");
\--
EOF;


require_once '../texy/texy.php';

  $tA = array_sum( explode( ' ' , microtime() ) );

  $texy = new Texy();

  $tB = array_sum( explode( ' ' , microtime() ) );

  for( $i = 0; $i < 10000; $i++ ){
    $html = $texy->process($text);
  }

  $tC = array_sum( explode( ' ' , microtime() ) );

echo "Init: ".($tB - $tA)." s;   Run: ".($tC - $tB)." s\n";


?>
