BEGIN {
	FS = ": ";
}

/eth0/ {
	print $1;
}
