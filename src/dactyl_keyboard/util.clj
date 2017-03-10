(ns dactyl-keyboard.util
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :refer :all]))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (/ height 2)])))

(defn bottom-hull [p]
  (hull p (bottom 1 p)))
