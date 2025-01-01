# Android weather data app
This project was built to gather data from different weather stations that I have and get that presented in an Android app on my phone. I also added some data about sun rise and sun set based on my location.
## Setup

To get data from my commercial weather stations I use an Realtek RTL2832 based DVB dongle in combination with the rtl_433 lib.
On my computer I also installed Mosquitto as an MQTT broker that my Android app can connect to and receive weather data.


## Links
- Mosquitto MQTT broker https://mosquitto.org/
- Lib for getting weather data https://github.com/merbanan/rtl_433