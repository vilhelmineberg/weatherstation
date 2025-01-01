# Android weather data app
This project was built to gather data from different weather stations that I have and get that presented in an Android app on my phone. I also added some data about sun rise and sun set based on my location.
## Setup

To get data from my different commercial weather stations that i have, I use an Realtek RTL2832 based DVB dongle and I also use the rtl_433 lib to receive the data from my weather stations.
On my computer I also installed Mosquitto as an MQTT broker that my Android app can connect to add receive topics.


## Links
Mosquitto MQTT broker https://mosquitto.org/
Lib for getting weather data https://github.com/merbanan/rtl_433