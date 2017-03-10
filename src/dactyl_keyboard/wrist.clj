(ns dactyl-keyboard.wrist
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.util :refer :all]))

(def pad-corner-r 11)
(def pad-width 95)
(def pad-length 76.5)

(def stand-height 45)
(def stand-thickness 1.5)

(def bumper-diameter 9.6)
(def bumper-radius (/ bumper-diameter 2))

(def corner-circle (->> (cylinder pad-corner-r stand-thickness)
                        (translate [(- (/ pad-width 2) pad-corner-r)
                                    (- (/ pad-length 2) pad-corner-r)
                                    (+ stand-height (/ stand-thickness 2))])))

(def corner {:back-right corner-circle
             :back-left (->> corner-circle (mirror [-1 0 0]))
             :front-right (->> corner-circle (mirror [0 -1 0]))
             :front-left (->> corner-circle (mirror [0 -1 0]) (mirror [-1 0 0]))             })

(def wrist-rest
  (union (hull (corner :back-right)
               (corner :back-left)
               (corner :front-right)
               (corner :front-left))
         (bottom-hull (corner :back-right))
         (bottom-hull (corner :back-left))
         (bottom-hull (corner :front-right))
         (bottom-hull (corner :front-left))))

(spit "things/wrist.scad"
      (write-scad wrist-rest))
