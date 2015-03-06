(ns dactyl-cave.alternathumb
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  (:use [unicode-math.core])
  (:require [dactyl-cave.key :as key])
  (:require [dactyl-cave.cave :as cave]))

(defn- scoop [angle radius [x y :as direction] shape]
  (->> shape
       (translate [0 0 radius])))

(defn thumb-x+x-column [shape]
  (let [α (/ π 12)
        radius (/ (/ key/pillar-depth 2)
                  (Math/sin (/ α 2)))
        spin-shape (->> shape
                        (translate [0 0 (+ (- key/full-height)
                                           (- radius))]))]
    (translate
     [0 0 (+ radius key/full-height)]
     (union
      spin-shape

      (->> spin-shape
           (rotate (- α) [1 0 0]))))))

(defn thumb-2x-column [shape]
  (let [α (/ π 12)
        radius (/ (/ key/pillar-depth 2)
                  (Math/sin (/ α 2)))
        spin-shape (->> shape
                        (translate [0 0 (+ (- key/full-height)
                                           (- radius))]))]
    (translate
     [0 0 (+ radius key/full-height)]
     (union
      (->> spin-shape
           (rotate (* α -1/2) [1 0 0]))))))

(defn thumb-2x-row [shape]
  (let [α (/ π 12)
        radius (/ (/ key/pillar-depth 2)
                  (Math/sin (/ α 2)))
        spin-shape (->> shape
                        (translate [0 0 (+ (- key/full-height)
                                           (- radius))]))]
    (translate
     [0 0 (+ radius key/full-height)]
     (union
      (->> spin-shape
           (rotate (* α 1) [1 0 0]))))))


(defn spin-thumb [column shape]
  (let [β (/ π 36)
        radius (/ (/ (+ key/pillar-width 5) 2)
                  (Math/sin (/ β 2)))]
    (->>
     (translate
      [0 0 (- (- radius key/full-height))]
      (->> shape
           (translate [0 0 (- radius key/full-height)])
           (rotate (* column β) [0 1 0])))
     (translate [key/pillar-width 0 0])
     (rotate (/ π 12) [0 0 1])
     #_(rotate (/ π -12) [0 1 0])
     #_(rotate (/ π 6) [0 0 1])
     (translate [-7 -47 35]))))

(defn thumb-layout [shape]
  (union
   (spin-thumb 2 (thumb-x+x-column shape))
   (spin-thumb 1 (thumb-x+x-column shape))
   (spin-thumb 0 (thumb-2x-column shape))
   (spin-thumb 1/2 (thumb-2x-row shape))))

(defn support [shape]
  (hull
   shape
   (extrude-linear {:height 10 :twist 0 :convexity 0}
                   (project (hull shape)))))

(defn thumb-support [shape]
  (union
   (support (union
             (spin-thumb 2 (thumb-x+x-column shape))
             (spin-thumb 1 (thumb-x+x-column shape))
             (spin-thumb 0 (thumb-2x-column shape))))
   (support (union
             (spin-thumb 0 (thumb-2x-column shape))
             (spin-thumb 1/2 (thumb-2x-row shape))))
   
))

(def bottom
  (translate [0 0 -100]
             (cube 2000 2000 200))
  )


#_(def thumb-base
  (difference
   (hull
    (thumb-layout (translate [0 0 (/ key/pillar-height -2)]
                             (scale [1 1 1/10] key/full-pillar)))
    (extrude-linear {:height 10 :twist 0 :convexity 0}
                    (project (hull (thumb-layout key/full-pillar)))))
   bottom
   (thumb-layout key/keyswitch-full-hole)))

(def thumb-base
  (union
   (thumb-support (scale [1 1 1/10] key/full-pillar))
   #_(->> (cube 150 150 50)
        (translate [150 75 25])))

  )

(defn move-to-corner [shape]
  (translate [-265 -215 0] shape))

(def thumb-cluster
  (difference
   (translate [0 0 -20]
              (difference
               (union
                (thumb-layout key/pillar)
                thumb-base)
               (thumb-layout key/keyswitch-full-hole)))
   bottom))

(spit "alternathumb.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         #_thumb-cluster
                         (union
                          (mirror [1 0 0] (move-to-corner thumb-cluster))
                          #_(->> (move-to-corner thumb-cluster)
                               (mirror [1 0 0]))
                          #_cave/base
                          #_cave/fingers
                          )
                         
                         #_(mirror [1 0 0]
                                 (difference
                                  (move-to-corner thumb-cluster)
                                  cave/base
                                  ))
                         )))

