# PolyPot control module source code

## Deploying the code

Copy all necessary modules to the folder `/micropython-esp32/ports/esp32/modules/` in the micropython-esp32 repository.
Then rebuild the firmware:
```
make
make erase
make deploy
```
