(ns dactyl-cave.thumb
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  (:use [unicode-math.core])
  (:require [dactyl-cave.key :as key])
  (:require [dactyl-cave.cave :as cave]))

(defn thumb-place [column row shape]
  (let [α (/ π 12)
        row-radius (+ (/ (/ key/pillar-depth 2)
                         (Math/sin (/ α 2)))
                      key/full-height)
        β (/ π 36)
        column-radius (+ (/ (/ (+ key/pillar-width 5) 2)
                            (Math/sin (/ β 2)))
                         key/full-height)]
    (->> shape
         (translate [0 0 (- row-radius)])
         (rotate (* α row) [1 0 0])
         (translate [0 0 row-radius])
         (translate [0 0 (- column-radius)])
         (rotate (* column β) [0 1 0])
         (translate [0 0 column-radius])
         (translate [key/pillar-width 0 0])
         (rotate (/ π 12) [0 1 0])
         (rotate (* π (- 1/4 1/16)) [0 0 1])
         (rotate (/ π 12) [1 1 0])
         (translate [254/45 127/15 1778/45]))))

(defn thumb-2x-column [shape]
  (thumb-place 0 -1/2 shape))

(defn thumb-2x+1-column [shape]
  (union (thumb-place 1 -1/2 shape)
         (thumb-place 1 1 shape)))

(defn thumb-1x-column [shape]
  (union (thumb-place 2 -1 shape)
         (thumb-place 2 0 shape)
         (thumb-place 2 1 shape)))

(defn thumb-layout [shape]
  (union
   (thumb-2x-column shape)
   (thumb-2x+1-column shape)
   (thumb-1x-column shape)))

(defn support [shape]
  (hull
   shape
   (extrude-linear {:height 127/45 :twist 0 :convexity 0}
                   (project (hull shape)))))

(defn thumb-support [shape]
  (let [column-supports
        (union
         (support (thumb-2x-column shape))
         (support (thumb-2x+1-column shape))
         (support (thumb-1x-column shape)))]
    (union column-supports
           (support column-supports))))
(fn [])
(def bottom
  (translate [0 0 -254/9] (cube 5080/9 5080/9 508/9)))

(def thumb-base
  (thumb-support (scale [1 1 1/10] key/full-pillar)))

#_(defn move-to-corner [shape]
  (translate [-6731/90 -5461/90 0] shape))

(defn move-to-corner [shape]
  (translate [(+ -6731/90 10) (- -5461/90 10) 0] shape))

(/ -6731 90.0) -74.78888888888889
(double -5461/90) -60.67777777777778

(def thumb-cluster
  (difference
   (translate [0 0 -254/45]
              (difference
               (union
                (thumb-layout key/pillar)
                thumb-base)
               (thumb-layout key/keyswitch-full-hole)))
   bottom))

(def connection-stems
  (difference
   (hull (union
          (->> (cylinder 127/90 508/9)
               (translate [-0 -2413/45 0]))
          (->> (cylinder 127/90 508/9)
               (translate [-127/3 -0 0]))
          (->> (cylinder 127/90 508/9)
               (translate [-2159/30 -127/5 0]))
          (->> (cylinder 127/90 508/9)
               (translate [-508/9 -381/5 0]))))
   bottom
   cave/main-sphere
   (translate [0 0 -254/45]
              (thumb-layout key/keyswitch-full-hole))))

(def wire-network
  (apply union
         (for [[column row] [[0 -1/2]
                             [1 -1/2]
                             [1 1]
                             [2 -1]
                             [2 0]
                             [2 1]]]
           (let [middle-hole (->> (thumb-place column row (cube 6 6 6))
                                  (translate [0 0 -127/9])
                                  move-to-corner)]
             #_(thumb-place column row (sphere 127/9))
             (union (hull (->> (cube 254/45 254/45 key/keyswitch-height)
                               (thumb-place column row)
                               move-to-corner)
                          middle-hole)
                    (hull middle-hole (cave/bottom-cube 0 4))
                    (hull (cave/bottom-cube 0 4) cave/hole-destination))))))


(spit "thumb.scad"
      (write-scad (difference
                   (union
                    (move-to-corner thumb-cluster)
                    connection-stems
                    
                    #_cave/base
                    #_cave/fingers)
                   cave/base
                   wire-network)))

#_(spit "thumb.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         (mirror [-1 0 0]
                                 (difference
                                  (union
                                   (move-to-corner thumb-cluster)
                                   connection-stems
                                   
                                   #_cave/base
                                   #_cave/fingers)
                                  cave/base
                                  wire-network)))))

#_(spit "thumb.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         (mirror [-1 0 0]
                                 (difference
                                  (union
                                   (move-to-corner thumb-cluster)
                                   connection-stems
                                   
                                   #_cave/base
                                   #_cave/fingers)
                                  cave/base
                                  wire-network)))))




(spit "one-piece.scad"
      (write-scad
       (mirror [-1 0 0]
               (union (difference cave/fingers cave/wire-network)
                      (difference
                       (union
                        (move-to-corner thumb-cluster)
                        connection-stems)
                       cave/base
                       wire-network)))))
