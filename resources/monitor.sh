#!/bin/bash

capture() {
	sudo tcpdump -i wlan0 -n > dump.fis
}

stop() {
	sudo pkill tcpdump
	sudo pkill monitor.sh
}

### main logic ###
case "$1" in
	init)
		sudo ifconfig wlan0 down
		sudo iwconfig wlan0 mode monitor
		sudo ifconfig wlan0 up
		;;
	start)
		if [[ $# -eq 1 ]]; then
			for i in {1..13}; do
				echo "change to channel $i for 2 seconds"
				sudo iwconfig wlan0 chan $i
				sleep 2
			done
		fi 
    
		if [[ $# -eq 2 ]]; then
			echo "change to channel $2"
			sudo iwconfig wlan0 chan $2
		fi
	
		if [[ $# -eq 14 ]]; then
			while true; do
				sudo iwconfig wlan0 chan 1
				echo "change to channel 1 for $2 seconds"
				sleep "$2"
				sudo iwconfig wlan0 chan 2
				echo "change to channel 2 for $3 seconds"
				sleep "$3"
				sudo iwconfig wlan0 chan 3
				echo "change to channel 3 for $4 seconds"
				sleep "$4"
				sudo iwconfig wlan0 chan 4
				echo "change to channel 4 for $5 seconds"
				sleep "$5"
				sudo iwconfig wlan0 chan 5
				echo "change to channel 5 for $6 seconds"
				sleep "$6"
				sudo iwconfig wlan0 chan 6
				echo "change to channel 6 for $7 seconds"
				sleep "$7"
				sudo iwconfig wlan0 chan 7
				echo "change to channel 7 for $8 seconds"
				sleep "$8"
				sudo iwconfig wlan0 chan 8
				echo "change to channel 8 for $9 seconds"
				sleep "$9"
				sudo iwconfig wlan0 chan 9
				echo "change to channel 9 for ${10} seconds"
				sleep "${10}"
				sudo iwconfig wlan0 chan 10
				echo "change to channel 10 for ${11} seconds"
				sleep "${11}"
				sudo iwconfig wlan0 chan 11
				echo "change to channel 11 for ${12} seconds"
				sleep "${12}"
				sudo iwconfig wlan0 chan 12
				echo "change to channel 12 for ${13} seconds"
				sleep "${13}"
				sudo iwconfig wlan0 chan 13
				echo "change to channel 13 for ${14} seconds"
				sleep "${14}"
			done
		fi
		;;
	stop)
		stop
		;;
	reset)
		sudo ifconfig wlan0 down
		sudo iwconfig wlan0 mode managed
		sudo ifconfig wlan0 up
		;;
	capture)
		capture
		;;
	*)
		echo $"Usage: $0 {start|reset}"
		exit 1
esac

exit 0

