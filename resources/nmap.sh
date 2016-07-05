#!/bin/bash

case "$1" in
	match)
		#sudo nmap -sP -n 192.168.43.1-255
		sudo nmap -sP -n $2
		;;
	details)
		#sudo nmap -O --osscan-guess 192.168.43.121
		sudo nmap -O --osscan-guess $2
		;;
	*)
		echo $"Usage: $0 {match|details}"
		exit 1
esac

exit 0
