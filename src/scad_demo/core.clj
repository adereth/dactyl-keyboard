(ns scad-demo.core
  (:use [scad-clj.scad])
  (:use [scad-clj.model]))

(def primitives
  (union
   (union)
   (->> (sphere 50))))

(spit "post-demo.scad"
      (write-scad primitives))