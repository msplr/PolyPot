# PolyPot Control Module PCB

## Issues

- Button gpio 23 does not allow wakeup from deepsleep -> connect to IO15
- Soil moisture sensor pinout is wrong. FIX THE CABLE BEFORE CONNECTING!
- IO12 is not connected to SENSOR_ENn PMOS (typo in signal name)
- IO12 must not be pulled up during boot -> use IO25
- R1 and R11 need to be ~120 Ohm to have higher LED brightness
- Footprint of buttons RESET and PROG is too small

## Images
![Board Top](https://raw.githubusercontent.com/nuft/PolyPot/master/module/pcb/img/top.jpg)
![Board Bottom](https://raw.githubusercontent.com/nuft/PolyPot/master/module/pcb/img/bottom.jpg)
![Board Dimensions](https://raw.githubusercontent.com/nuft/PolyPot/master/module/pcb/img/PolyPot_PCB_dimensions.png)
