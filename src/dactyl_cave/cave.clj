(ns dactyl-cave.cave
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  (:use [unicode-math.core])
  (:use [dactyl-cave.key]))

(defn key-place [column row shape]
  (let [α (/ π 12)
        row-radius (+ (/ (/ pillar-depth 2)
                         (Math/sin (/ α 2)))
                      full-height)
        row-placed-shape (->> shape
                              (translate [0 0 (- row-radius)])
                              (rotate (* α (- 2 row)) [1 0 0])
                              (translate [0 0 row-radius]))
        β (/ π 36)
        column-radius (+ (/ (/ (+ pillar-width 127/90) 2)
                            (Math/sin (/ β 2)))
                         full-height)
        column-offset (condp = column
                        2 [0 127/45 -254/45]
                        4 [0 (/ pillar-depth -3) 254/45]
                        5 [0 (/ pillar-depth -4) 254/45]
                        [0 0 0])
        column-angle (if (<= column 4)
                       (* β (- 2 column))
                       (* β -3.25))
        placed-shape (->> row-placed-shape
                          (translate [0 0 (- column-radius)])
                          (rotate column-angle [0 1 0])
                          (translate [0 0 column-radius])
                          (translate column-offset))]
    (translate [0 0 127/18]
               (rotate (/ π 12) [0 1 0]
                       placed-shape))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Limits
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def bottom-limit
  (->> (cube (* pillar-width 17.75)
             (* pillar-depth 17)
             508/9)
       (translate [(+ (/ pillar-width 2) 127/45)
                   0 -254/9])))

(def back-limit
  (->> (cube (* pillar-width 9)
             (* pillar-depth 2)
             254/3)
       (translate [pillar-width
                   (+ (* pillar-depth 4.1))
                   254/9])))

(def front-right-limit
     (->> (cube (* pillar-width 2)
              (* pillar-depth 2)
              254/3)
        (translate [(+ (* pillar-width 4.125))
                    (+ (* pillar-depth -3.25))])))

(def front-left-limit
  (->> (cube (* pillar-width 2.5)
             (* pillar-depth 2)
             254/3)
       (translate [(+ (* pillar-width -3))
                   (+ (* pillar-depth -3))
                   254/9])))

(def front-limit
  (->> (cube (* pillar-width 9)
             (* pillar-depth 2)
             254/3)
       (translate [(* pillar-width 1/2) (+ (* pillar-depth -4.25)) 254/9])))

 (* (/ 25.4 90) pillar-depth (- 3.1 -3.2))


(def left-limit
   (->> (cube (* pillar-width 1)
              (* pillar-depth 8)
              254/3)
        (translate [(+ (* pillar-depth -3.25)) 0 254/9])))

(def right-limit
   (->> (cube (* pillar-width )
                (* pillar-depth 8)
                1016/9)
        (translate [(+ (* pillar-depth 5.5)) 0 254/9]))  )

(*  (/ 25.4 90) (- (- (* pillar-depth 5.4) (* pillar-width 1/2))
                   (+ (* pillar-depth -3.25) (* pillar-width 1/2))
                   ))

(def limits
   (union
   #_front-right-limit
   front-left-limit
   front-limit
   left-limit
   right-limit
   bottom-limit
   back-limit))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Base
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def main-sphere
  (let [radius (/ (/ pillar-depth 2)
                  (Math/sin (/ (/ π 36) 2)))]
    (->> (sphere radius)
         (translate [(* pillar-width 2.5) 0 (+ radius 127/90)])))  )

(def base-cube
  (->> (cube (* pillar-width 7.75)
             (* pillar-depth 7)
             508/9)
       (translate [(+ (/ pillar-width 2) 2921/450)
                   0 254/9])))

(def base
  (difference
   base-cube
   main-sphere
   limits))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Walls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(def wall-sphere
  (let [radius (/ (/ pillar-depth 2)
                  (Math/sin (/ (/ π 36) 2)))]
    (->> (sphere radius)
         (scale [1 2/3 1])
         (translate [(* pillar-width 2.5) 0 (+ radius 5 (* pillar-depth ))]))))

(def wall-sphere
  (let [radius (/ (/ pillar-depth 2)
                  (Math/sin (/ (/ π 36) 2)))]
    (->> (sphere radius)
         (scale [1 2/3 1])
         (translate [0 0 radius])
         (translate [0 0 127/18])
         (rotate (/ π 12) [0 1 0])
         (translate [0 0 (* pillar-depth 3/4)]))))

(def wall-thickness 127/30)

(def back-wall
  (difference
   (translate [0 (- wall-thickness) 0] back-limit)
   back-limit
   right-limit
   left-limit
   bottom-limit
   wall-sphere))

(def walls
  (difference
   (union
    (translate [0 (- wall-thickness) 0] back-limit)
    (translate [(- wall-thickness) 0 0] right-limit)
    (translate [0 wall-thickness 0] front-limit)
    (translate [wall-thickness 0 0] left-limit)    
    )
   wall-sphere
   limits))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Wire holes
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def teensy-center [(* -1.6 pillar-width)
                    (* 2.8 pillar-depth)
                    254/45])

(def teensy-tray-slot
  (->> (cube (* 1.125 pillar-width)
             40
             508/45)
       (translate teensy-center)))

(def hole-destination
  (->> (cube 5.7 5.7 5.7)
       (translate [(first teensy-center)
                   (second teensy-center)
                   2.8 #_3.1])))

(defn bottom-cube [column row]
  (->> (cube 6 6 6)
       (key-place column row)
       (project)
       (extrude-linear {:height 5.7 :twist 0 :convexity 0})
       (translate [0 0 2.8])))

(defn wire-hole [column row]
  (union
   (hull
    (key-place column row (cube 6 6 keyswitch-height))
    (bottom-cube column row))
   (hull
    hole-destination
    (bottom-cube column row))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Full Model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def fingers
  (let [all-key-coords (for [column (range 0 6)
                         row (range 0 5)
                         ;; Removing bottom left key
                         :when (or (not= column 0)
                                   (not= row 4))]
                         [column row])
        middle-key-coords (for [column (range 0 6)
                                row (range 1 4)
                                ;; Removing bottom left key
                                :when (or (not= column 0)
                                          (not= row 4))]
                            [column row])
        top-key-coords (for [column (range 0 6)]
                         [column 0])
        bottom-key-coords (conj (for [column (range 1 6)]
                                  [column 4])
                                [0 3])        

        ]
    (difference
     (union base
            #_walls
            (apply union
                   (map #(key-place (first %) (second %)
                                    (->> (cube pillar-width  pillar-depth
                                               (* 3 pillar-height))
                                         (translate [0 0 (/ pillar-height -2)])))
                        all-key-coords)))
     (apply union
            (concat
             (map #(key-place (first %) (second %) keyswitch-full-hole)
                  middle-key-coords)
             (map #(key-place (first %) (second %) keyswitch-bottom-hole)
                  top-key-coords)
             (map #(key-place (first %) (second %) (mirror [0 -1 0] keyswitch-bottom-hole))
                  bottom-key-coords)
             ))
     limits
     teensy-tray-slot)))


(def wire-network
  (union
   (wire-hole 0 0)
   (wire-hole 1 0)
   (wire-hole 2 0)
   (wire-hole 3 0)
   (wire-hole 4 0)
   (wire-hole 5 0)
   (wire-hole 0 1)
   (wire-hole 0 2)
   (wire-hole 0 3)
   (wire-hole 0 4)   
   (wire-hole 1 4)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Actual Output
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(spit "key.scad"
      (write-scad (difference
                   pillar
                   )))

(spit "key.scad"
      (write-scad (difference
                   (union
                    #_walls
                    #_wall-sphere
                    #_fingers
                    (difference fingers wire-network)
                    #_(mirror [-1 0 0]
                              (difference fingers wire-network))
                    )
                   #_(cube 400 800 800)
                   )))

#_(spit "key.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         (difference
                          (union
                           #_walls
                           #_wall-sphere
                           #_fingers
                           (difference fingers wire-network)
                           #_(mirror [-1 0 0]
                                     (difference fingers wire-network))
                           )
                          #_(cube 400 800 800)
                          ))))


#_(spit "key.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         (difference
                          fingers
                          wire-hole-1
                          wire-hole-2))))

#_(spit "key.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         (differe
                          #_wall
                          #_base
                          #_rim
                          #_(mirror [1 0 0] fingers)
                          fingers
                          wire-hole-1))))

#_(spit "key.scad"
      (write-scad (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
                         (union
                          fingers
                          (->> fingers
                               project
                               (extrude-linear {:height 1 :twist 0 :convexity 0})
                               (scale [1.5 1.15 1])
                               )
                          )
                         )))

