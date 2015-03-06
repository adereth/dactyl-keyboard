(ns dactyl-cave.key
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  (:use [unicode-math.core]))


(def tw 13.969999999999999)     ;; Top width
(def smh 0.98044)   ;; Side margin height
(def pw 0.8128)     ;; Peg width
(def ph 3.5001199999999995)   ;; Peg height
(def pgh 5.00888)  ;; Peg gap height

(def keyswitch-height (+ smh ph pgh ph smh))
(def keyswitch-width (+ pw tw pw))
(def plate-height 254/45)

(defn- flip-path [points] (map (partial map -) points))

(def keyswitch-plate-hole-shape
  (polygon [[0.8128 0] [0.8128 0.98044] [0.0 0.98044] [0.0 4.48056] [0.8128 4.48056] [0.8128 9.48944] [0.0 9.48944] [0.0 12.98956] [0.8128 12.98956] [0.8128 13.969999999999999] [14.7828 13.969999999999999] [14.7828 12.98956] [15.5956 12.98956] [15.5956 9.48944] [14.7828 9.48944] [14.7828 4.48056] [15.5956 4.48056] [15.5956 0.98044] [14.7828 0.98044] [14.7828 0]]))

(def keyswitch-plate-hole
  (->> keyswitch-plate-hole-shape
       (extrude-linear {:height plate-height :twist 0 :convexity 0})
       (translate (map #(/ (- %) 2) [keyswitch-width keyswitch-height 0]))
       (translate [0 0 1])))

(def hole-height 127/18)

(def pillar-width (+ keyswitch-width 127/45))
(def pillar-height (+ hole-height (/ plate-height 2)))
(def pillar-depth (+ keyswitch-height 127/30))

(def keyswitch-full-hole
  (->>
   (union
    keyswitch-plate-hole
    (->> (cube (/ ph 2) pillar-depth (* plate-height 2))
         (translate [(* tw -1/4) 0 0]))
    (->> (cube (/ ph 2) pillar-depth (* plate-height 2))
         (translate [(* tw 1/4) 0 0]))    
    (translate
     [0 0 (/ hole-height -2)]
     (cube keyswitch-width
           keyswitch-height
           hole-height)))
   (translate [0 0 hole-height])))

(def keyswitch-bottom-hole
  (->>
   (union
    keyswitch-plate-hole
    (->> (cube (/ ph 2) (/ pillar-depth 2) (* plate-height 2))
         (translate [(* tw -1/4) (/ pillar-depth -2) 0]))
    (->> (cube (/ ph 2) (/ pillar-depth 2) (* plate-height 2))
         (translate [(* tw 1/4) (/ pillar-depth -2) 0]))    
    (translate
     [0 0 (/ hole-height -2)]
     (cube keyswitch-width
           keyswitch-height
           hole-height)))
   (translate [0 0 hole-height])))

(def full-pillar
  (->> (cube pillar-width  pillar-depth
             pillar-height)
       (translate [0 0 (/ pillar-height 2)])))

(def pillar
  (difference
   full-pillar
   keyswitch-full-hole))

(def key-height 127/10)

(def pillar-with-fake-key
  (union pillar
         (->> (cube (+ -0 pillar-width) (+ -0 pillar-depth) key-height)
              (translate [0 0 (+ (/ key-height 2) pillar-height 127/450)]))))

(def full-height (+ pillar-height key-height 127/450))
