import network

ap = network.WLAN(network.AP_IF)
ap.active(True)
ap.config(essid="PolyPot", password="PotSetup", authmode=network.AUTH_WPA_WPA2_PSK)


