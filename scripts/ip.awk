BEGIN {
	FS = ": ";
}

$1 == ifindex {
	print $2;
}
