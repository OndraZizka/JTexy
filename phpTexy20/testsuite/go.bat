set php=C:\PHP\versions\php-5.2.6-Win32\php.exe
set diff=diff.exe


"%php%" test.php
start "" %diff% output ref
