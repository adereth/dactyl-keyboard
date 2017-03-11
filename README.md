# The Dactyl-ManuForm Keyboard
This is a fork of the [Dactyl](https://github.com/adereth/dactyl-keyboard), a parameterized, split-hand, concave, columnar, ergonomic keyboard.

<img src="https://imgur.com/a/3DYTE"/>

The main change is that the thumb cluster was adapted from the [ManuForm keyboard](https://github.com/jeffgran/ManuForm) ([geekhack](https://geekhack.org/index.php?topic=46015.0)). The walls were changed to just drop to the floor. The keyboard is paramaterized to allow the following: 

* Rows: 4 - 6 
* Columns: 5 and up
* Row curvature
* Column curvature
* Row tilt (tenting)
* Column tilt
* Column offsets

I plan to try the 4x6 version or maybe the 4x5 version. The default has more tenting than the Dactyl. See the following model files for configurations that may be most common:

* [4x5](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-4x5.stl)
* [4x6](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-4x6.stl)
* [5x6](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-5x6.stl)
* [6x6](https://github.com/tshort/dactyl-keyboard/blob/master/things/right-6x6.stl)

**This is still a work in progress.** I have not started on wiring, and I don't know if everything fits.

I plan to use a Teensy 2++ in each half. Then, I have enough channels to wire straight to each key switch. Diodes are not needed. Then, I can connect them with a serial connection, and adapt the QMK firmware for the [Let's split keyboard](https://github.com/qmk/qmk_firmware/tree/master/keyboards/lets_split) that implements this approach. Each half can be master.

This doesn't have a bottom. It has hex holes designed for 9- or 10-mm long M3 female-female spacers. Then, I can use a M3 wafer-head screws to connect a bottom plate. If wires aren't dangling, a bottom plate may not be needed.

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
