# The Dactyl-ManuForm Keyboard
This is a fork of the [Dactyl](https://github.com/adereth/dactyl-keyboard), a parameterized, split-hand, concave, columnar, ergonomic keyboard.

The main change is that the thumb cluster was adapted from the [ManuForm keyboard](https://github.com/jeffgran/ManuForm) ([geekhack](https://geekhack.org/index.php?topic=46015.0)). The walls were changed to just drop to the floor. The keyboard is paramaterized to allow either 4, 5, or 6 main rows. I plan to try the four-row version. See the following model files:

* [4 rows](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-4-rows.stl)
* [5 rows](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-5-rows.stl)
* [6 rows](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-6-rows.stl)

**This is still a work in progress.** 

I plan to use a Teensy 2++ in each half. Then, I have enough channels to wire straight to each key switch. Diodes are not needed. Then, I can connect them with a serial connection, and adapt the QMK firmware for the [Let's split keyboard](https://github.com/qmk/qmk_firmware/tree/master/keyboards/lets_split) that implements this approach. Each half can be master.

This doesn't have a bottom. I may or may not add one. I have many test prints to try.

## Assembly

### Generating a Design

**Setting up the Clojure environment**
* [Install the Clojure runtime](https://clojure.org)
* [Install the Leiningen project manager](http://leiningen.org/)
* [Install OpenSCAD](http://www.openscad.org/)

**Generating the design**
* Run `lein repl`
* Load the file `(load-file "src/dactyl_keyboard/dactyl.clj")`
* This will regenerate the `things/*.scad` files
* Use OpenSCAD to open a `.scad` file.
* Make changes to design, repeat `load-file`, OpenSCAD will watch for changes and rerender.
* When done, use OpenSCAD to export STL files

**Tips**
* [Some other ways to evaluate the clojure design file](http://stackoverflow.com/a/28213489)
* [Example designing with clojure](http://adereth.github.io/blog/2014/04/09/3d-printing-with-clojure/)


### Printing
Pregenerated STL files are available in the [things/](things/) directory.

### Wiring
Masks for the flexible PCBs I used are available for the [left](resources/pcb-left.svg) and [right](resources/pcb-right.svg) side.

A [very rough guide for the brave is here](guide/README.org#wiring) - It will be improved over time (**TODO**)!

## License

Copyright © 2015 Matthew Adereth

The source code for generating the models (everything excluding the [things/](things/) and [resources/](resources/) directories is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE).  The generated models and PCB designs are distributed under the [Creative Commons Attribution-NonCommercial-ShareAlike License Version 3.0](LICENSE-models).
