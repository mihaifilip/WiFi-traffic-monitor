#!/bin/bash

### main logic ###
case "$1" in
	connect)
		sudo service network-manager stop
		if [[ $# -eq 2 ]]; then
			# open system
			sudo iwconfig wlan0 essid $2
			sudo dhclient wlan0
		else
			if [[ $# -eq 4 ]]; then
				# WEP
				if [ $2 == "WEP" ]; then
					sudo iwconfig wlan0 essid $3 password $4
					sudo dhclient wlan0
				# WPA
				elif [ $2 == "WPA" ]; then
					sudo rm temp.conf
					sudo wpa_passphrase $3 $4 > temp.conf
					sudo wpa_supplicant -B -iwlan0 -ctemp.conf -Dwext -Dnl80211
					sudo dhclient wlan0
				else
					echo "Invalid format"
				fi
			fi	
		fi
		;;
	disconnect)
		sudo ifconfig wlan0 down
		sudo ifconfig wlan0 up
		kill $(ps aux | grep 'wpa_supplicant -B -iwlan0' | awk 'NR==1 {print $2}')
		sudo service network-manager start
		;;
	*)
		echo $"Usage: $0 {connect [SSID] | connect [ENCRYPTION] [SSID] [PASSWORD] | disconnect}"
		exit 1
esac

exit 0
