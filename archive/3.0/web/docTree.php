<?php

$dir = 'repository';
dirTree($dir);

function dirTree($dir) {
	echo '<dir name="' . $dir . '">';
	$dh  = opendir($dir);
	while (false !== ($filename = readdir($dh)))
    	$files[] = $filename;
    sort($files);
	chdir($dir);
	foreach ($files as $file) {
		if (strpos($file, '.') !== 0) {
			if (is_dir($file)) {
				dirTree($file);
			}
			else {
				echo '<file name="' . $file . '"/>';
			}
		}
	}
	chdir('..');
	echo '</dir>';
}

?>
