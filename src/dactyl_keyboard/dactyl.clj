(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]))


(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

(def nrows 4)
(def ncols 5)

(def α (/ π 12))                        ; curvature of the columns
(def β (/ π 36))                        ; curvature of the rows
(def centerrow (- nrows 3))             ; controls front-back tilt
(def centercol 3)                       ; controls left-right tilt / tenting (higher number is more tenting)
(def orthographic-x (> nrows 5))        ; for larger number of rows don't curve them in as much
; (def orthographic-x true)             ; controls curvature of rows
(def maltron-style false)                ; use fixed angles for columns
(def maltron-angles [(deg2rad 10) (deg2rad 10) 0 0 0 (deg2rad -15) (deg2rad -15)])  ; starting point: http://patentimages.storage.googleapis.com/EP0219944A2/imgf0002.png 

(defn column-offset [column] (cond
  (= column 2) [0 2.82 -4.5]
  (>= column 4) [0 -12 5.64]           ; original [0 -5.8 5.64]
  :else [0 0 0]))

(def thumb-offsets [6 -3 7])

(def keyboard-z-offset  9)              ; controls height; original=24

(def extra-width 2.5)                   ; extra space between the base of keys; original= 2
(def extra-height 1.0)                  ; origin= 1/2


;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def lastrow (dec nrows))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 14.4) ;; Was 14.1, then 14.25
(def keyswitch-width 14.4)

(def sa-profile-key-height 12.7)

(def plate-thickness 4)
(def mount-width (+ keyswitch-width 3))
(def mount-height (+ keyswitch-height 3))

(def single-plate
  (let [top-wall (->> (cube (+ keyswitch-width 3) 1.5 plate-thickness)
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube 1.5 (+ keyswitch-height 3) plate-thickness)
                       (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ π 2) [1 0 0])
                      (translate [(+ (/ keyswitch-width 2)) 0 1])
                      (hull (->> (cube 1.5 2.75 plate-thickness)
                                 (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                             0
                                             (/ plate-thickness 2)]))))
        plate-half (union top-wall left-wall (with-fn 100 side-nub))]
    (union plate-half
           (->> plate-half
                (mirror [1 0 0])
                (mirror [0 1 0])))))

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-double-length 37.5)
(def sa-cap {1 (let [bl2 (/ 18.5 2)
                     m (/ 17 2)
                     key-cap (hull (->> (polygon [[bl2 bl2] [bl2 (- bl2)] [(- bl2) (- bl2)] [(- bl2) bl2]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 0.05]))
                                   (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 6]))
                                   (->> (polygon [[6 6] [6 -6] [-6 -6] [-6 6]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 12])))]
                 (->> key-cap
                      (translate [0 0 (+ 5 plate-thickness)])
                      (color [220/255 163/255 163/255 1])))
             2 (let [bl2 (/ sa-double-length 2)
                     bw2 (/ 18.25 2)
                     key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 0.05]))
                                   (->> (polygon [[6 16] [6 -16] [-6 -16] [-6 16]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 12])))]
                 (->> key-cap
                      (translate [0 0 (+ 5 plate-thickness)])
                      (color [127/255 159/255 127/255 1])))
             1.5 (let [bl2 (/ 18.25 2)
                       bw2 (/ 28 2)
                       key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 0.05]))
                                     (->> (polygon [[11 6] [-11 6] [-11 -6] [11 -6]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 12])))]
                   (->> key-cap
                        (translate [0 0 (+ 5 plate-thickness)])
                        (color [240/255 223/255 175/255 1])))})

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def columns (range 0 ncols))
(def rows (range 0 nrows))

(def cap-top-height (+ plate-thickness sa-profile-key-height))
(def row-radius (+ (/ (/ (+ mount-height extra-height) 2)
                      (Math/sin (/ α 2)))
                   cap-top-height))
(def column-radius (+ (/ (/ (+ mount-width extra-width) 2)
                         (Math/sin (/ β 2)))
                      cap-top-height))
(def column-x-delta (+ -1 (- (* column-radius (Math/sin β)))))
(def column-base-angle (* β (- centercol 2)))

(defn key-place [column row shape]
  (let [row-placed-shape (->> shape
                              (translate [0 0 (- row-radius)])
                              (rotate (* α (- centerrow row)) [1 0 0])      
                              (translate [0 0 row-radius]))
        column-angle (* β (- centercol column))   
        placed-shape (->> row-placed-shape
                          (translate [0 0 (- column-radius)])
                          (rotate column-angle [0 1 0])
                          (translate [0 0 column-radius])
                          (translate (column-offset column)))
        column-z-delta (* column-radius (- 1 (Math/cos column-angle)))
        placed-shape-ortho (->> row-placed-shape
                                (rotate (if maltron-style (+ column-base-angle (nth maltron-angles column)) column-angle) [0 1 0])
                                (translate [(- (* (- column centercol) column-x-delta)) 0 column-z-delta])
                                (translate (column-offset column)))]
    (->> (if (or maltron-style orthographic-x) placed-shape-ortho placed-shape)
         (rotate (/ π 12) [0 1 0])
         (translate [0 0 keyboard-z-offset]))))

(def key-holes
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> single-plate
                (key-place column row)))))

(def caps
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> (sa-cap (if (= column 5) 1 1))
                (key-place column row)))))

(defn rotate-around-x [angle position] 
  (mmul 
   [[1 0 0]
    [0 (Math/cos angle) (- (Math/sin angle))]
    [0 (Math/sin angle)    (Math/cos angle)]]
   position))

(defn rotate-around-y [angle position] 
  (mmul 
   [[(Math/cos angle)     0 (Math/sin angle)]
    [0                    1 0]
    [(- (Math/sin angle)) 0 (Math/cos angle)]]
   position))

(defn key-position [column row position]
  (let [row-position (->> position
                          (map + [0 0 (- row-radius)])
                          (rotate-around-x (* α (- centerrow row)))      
                          (map + [0 0 row-radius]))
        column-angle (* β (- centercol column))   
        placed-position (->> row-position
                             (map + [0 0 (- column-radius)])
                             (rotate-around-y column-angle)
                             (map + [0 0 column-radius])
                             (map + (column-offset column)))
        column-z-delta (* column-radius (- 1 (Math/cos column-angle)))
        placed-position-ortho (->> row-position
                                   (rotate-around-y (if maltron-style (+ column-base-angle (nth maltron-angles column)) column-angle))
                                   (map + [(- (* (- column centercol) column-x-delta)) 0 column-z-delta])
                                   (map + (column-offset column)))]
    (->> (if (or maltron-style orthographic-x) placed-position-ortho placed-position)
         (rotate-around-y (/ π 12))
         (map + [0 0 keyboard-z-offset]))))

; (pr (rotate-around-y π [10 0 1]))
(pr (key-position 1 cornerrow [(/ mount-width 2) (- (/ mount-height 2)) 0]))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

(def web-thickness 3.5)
(def post-size 0.1)
(def web-post (->> (cube post-size post-size web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      plate-thickness)])))

(def post-adj (/ post-size 2))
(def web-post-tr (translate [(- (/ mount-width 2) post-adj) (- (/ mount-height 2) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height 2) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ mount-width 2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(def connectors
  (apply union
         (concat
          ;; Row connections
          (for [column (range 0 (dec ncols))
                row (range 0 lastrow)]
            (triangle-hulls
             (key-place (inc column) row web-post-tl)
             (key-place column row web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place column row web-post-br)))

          ;; Column connections
          (for [column columns
                row (range 0 cornerrow)]
            (triangle-hulls
             (key-place column row web-post-bl)
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tl)
             (key-place column (inc row) web-post-tr)))

          ;; Diagonal connections
          (for [column (range 0 (dec ncols))
                row (range 0 cornerrow)]
            (triangle-hulls
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place (inc column) (inc row) web-post-tl))))))

;;;;;;;;;;;;
;; Thumbs ;;
;;;;;;;;;;;;

(def thumborigin 
  (map + (key-position 1 cornerrow [(/ mount-width 2) (- (/ mount-height 2)) 0])
         thumb-offsets))
; (pr thumborigin)

(defn thumb-tr-place [shape]
  (->> shape
      ;  (rotate (deg2rad  10) [1 0 0])
      ;  (rotate (deg2rad -23) [0 1 0])
      ;  (rotate (deg2rad  -3) [0 0 1])
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-12 -16 3])
       ))
(defn thumb-tl-place [shape]
  (->> shape
      ;  (rotate (deg2rad  10) [1 0 0])
      ;  (rotate (deg2rad -23) [0 1 0])
      ;  (rotate (deg2rad  -3) [0 0 1])
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-32 -15 -2])))
(defn thumb-mr-place [shape]
  (->> shape
       (rotate (deg2rad  -6) [1 0 0])
       (rotate (deg2rad -34) [0 1 0])
       (rotate (deg2rad  48) [0 0 1])
       (translate thumborigin)
       (translate [-29 -40 -13])
       ))
(defn thumb-ml-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -34) [0 1 0])
       (rotate (deg2rad  40) [0 0 1])
       (translate thumborigin)
       (translate [-51 -25 -12])))
(defn thumb-br-place [shape]
  (->> shape
       (rotate (deg2rad -16) [1 0 0])
       (rotate (deg2rad -33) [0 1 0])
       (rotate (deg2rad  54) [0 0 1])
       (translate thumborigin)
       (translate [-37.8 -55.3 -25.3])
       ))
(defn thumb-bl-place [shape]
  (->> shape
       (rotate (deg2rad  -4) [1 0 0])
       (rotate (deg2rad -35) [0 1 0])
       (rotate (deg2rad  52) [0 0 1])
       (translate thumborigin)
       (translate [-56.3 -43.3 -23.5])
       ))

(defn thumb-1x-layout [shape]
  (union
   (thumb-mr-place shape)
   (thumb-ml-place shape)
   (thumb-br-place shape)
   (thumb-bl-place shape)))

(defn thumb-15x-layout [shape]
  (union
   (thumb-tr-place shape)
   (thumb-tl-place shape)))

(def larger-plate
  (let [plate-height (/ (- sa-double-length mount-height) 3)
        top-plate (->> (cube mount-width plate-height web-thickness)
                       (translate [0 (/ (+ plate-height mount-height) 2)
                                   (- plate-thickness (/ web-thickness 2))]))
        ]
    (union top-plate (mirror [0 1 0] top-plate))))

(def thumbcaps
  (union
   (thumb-1x-layout (sa-cap 1))
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] (sa-cap 1.5)))))


(def thumb
  (union
   (thumb-1x-layout single-plate)
   (thumb-15x-layout single-plate)
   (thumb-15x-layout larger-plate)
   ))

(def thumb-post-tr (translate [(- (/ mount-width 2) post-adj)  (- (/ mount-height  1.15) post-adj) 0] web-post))
(def thumb-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height  1.15) post-adj) 0] web-post))
(def thumb-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -1.15) post-adj) 0] web-post))
(def thumb-post-br (translate [(- (/ mount-width 2) post-adj)  (+ (/ mount-height -1.15) post-adj) 0] web-post))

(def thumb-connectors
  (union
      (triangle-hulls    ; top two
             (thumb-tl-place thumb-post-tr)
             (thumb-tl-place thumb-post-br)
             (thumb-tr-place thumb-post-tl)
             (thumb-tr-place thumb-post-bl))
      (triangle-hulls    ; bottom two on the right
             (thumb-br-place web-post-tr)
             (thumb-br-place web-post-br)
             (thumb-mr-place web-post-tl)
             (thumb-mr-place web-post-bl))
      (triangle-hulls    ; bottom two on the left
             (thumb-bl-place web-post-tr)
             (thumb-bl-place web-post-br)
             (thumb-ml-place web-post-tl)
             (thumb-ml-place web-post-bl))
      (triangle-hulls    ; centers of the bottom four
             (thumb-br-place web-post-tl)
             (thumb-bl-place web-post-bl)
             (thumb-br-place web-post-tr)
             (thumb-bl-place web-post-br)
             (thumb-mr-place web-post-tl)
             (thumb-ml-place web-post-bl)
             (thumb-mr-place web-post-tr)
             (thumb-ml-place web-post-br))
      (triangle-hulls    ; top two to the middle two, starting on the left
             (thumb-tl-place thumb-post-tl)
             (thumb-ml-place web-post-tr)
             (thumb-tl-place thumb-post-bl)
             (thumb-ml-place web-post-br)
             (thumb-tl-place thumb-post-br)
             (thumb-mr-place web-post-tr)
             (thumb-tr-place thumb-post-bl)
             (thumb-mr-place web-post-br)
             (thumb-tr-place thumb-post-br)) 
      (triangle-hulls    ; top two to the main keyboard, starting on the left
             (thumb-tl-place thumb-post-tl)
             (key-place 0 cornerrow web-post-bl)
             (thumb-tl-place thumb-post-tr)
             (key-place 0 cornerrow web-post-br)
             (thumb-tr-place thumb-post-tl)
             (key-place 1 cornerrow web-post-bl)
             (thumb-tr-place thumb-post-tr)
             (key-place 1 cornerrow web-post-br)
             (key-place 2 lastrow web-post-tl)
             (key-place 2 lastrow web-post-bl)
             (thumb-tr-place thumb-post-tr)
             (key-place 2 lastrow web-post-bl)
             (thumb-tr-place thumb-post-br)
             (key-place 2 lastrow web-post-br)
             (key-place 3 lastrow web-post-bl)
             (key-place 2 lastrow web-post-tr)
             (key-place 3 lastrow web-post-tl)
             (key-place 3 cornerrow web-post-bl)
             (key-place 3 lastrow web-post-tr)
             (key-place 3 cornerrow web-post-br)
             (key-place 4 cornerrow web-post-bl))
      (triangle-hulls 
             (key-place 1 cornerrow web-post-br)
             (key-place 2 lastrow web-post-tl)
             (key-place 2 cornerrow web-post-bl)
             (key-place 2 lastrow web-post-tr)
             (key-place 2 cornerrow web-post-br)
             (key-place 3 cornerrow web-post-bl)
             )
      (triangle-hulls 
             (key-place 3 lastrow web-post-tr)
             (key-place 3 lastrow web-post-br)
             (key-place 3 lastrow web-post-tr)
             (key-place 4 cornerrow web-post-bl))
  ))

;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(def wall-offset -15)
(def wall-thickness 2)     ; originally 5

(defn wall-brace [place1 dx1 dy1 post1 place2 dx2 dy2 post2]
  (union
    (hull
      (place1 post1)
      (place1 (translate [0 0 wall-offset] post1))
      (place1 (translate [(* dx1 wall-thickness) (* dy1 wall-thickness) -4] post1))
      (place1 (translate [(* dx1 wall-thickness) (* dy1 wall-thickness) wall-offset] post1))
      (place2 post2)
      (place2 (translate [0 0 wall-offset] post2))
      (place2 (translate [(* dx2 wall-thickness) (* dy2 wall-thickness) -4] post2))
      (place2 (translate [(* dx2 wall-thickness) (* dy2 wall-thickness) wall-offset] post2)))
    (bottom-hull
      (place1 (translate [(* dx1 wall-thickness) (* dy1 wall-thickness) wall-offset] post1))
      (place1 (translate [0         0         wall-offset] post1))
      (place1 (translate [(* dx1 wall-thickness) (* dy1 wall-thickness) wall-offset] post1))
      (place2 (translate [0         0         wall-offset] post2))
      (place2 (translate [(* dx2 wall-thickness) (* dy2 wall-thickness) wall-offset] post2)))))

(defn key-wall-brace [x1 y1 dx1 dy1 post1 x2 y2 dx2 dy2 post2] 
  (wall-brace (partial key-place x1 y1) dx1 dy1 post1 
              (partial key-place x2 y2) dx2 dy2 post2))

(def case-walls
  (union
   ; back wall
   (for [x (range 0 ncols)] (key-wall-brace x 0 0 1 web-post-tl x       0 0 1 web-post-tr))
   (for [x (range 1 ncols)] (key-wall-brace x 0 0 1 web-post-tl (dec x) 0 0 1 web-post-tr))
   (key-wall-brace 0 0 0 1 web-post-tl 0 0 -1 0 web-post-tl)
   (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 1 0 web-post-tr)
   ; right wall
   (for [y (range 0 lastrow)] (key-wall-brace lastcol y 1 0 web-post-tr lastcol y       1 0 web-post-br))
   (for [y (range 1 lastrow)] (key-wall-brace lastcol (dec y) 1 0 web-post-br lastcol y 1 0 web-post-tr))
   (key-wall-brace lastcol cornerrow 0 -1 web-post-br lastcol cornerrow 1 0 web-post-br)
   ; left wall
   (for [y (range 0 lastrow)] (key-wall-brace 0 y -1 0 web-post-tl 0 y       -1 0 web-post-bl))
   (for [y (range 1 lastrow)] (key-wall-brace 0 (dec y) -1 0 web-post-bl 0 y -1 0 web-post-tl))
   ; front wall
   (key-wall-brace 0 0 0 1 web-post-tl 0 0 -1 0 web-post-tl)
   (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 1 0 web-post-tr)
   (key-wall-brace 3 lastrow   0 -1 web-post-bl 3 lastrow 0.5 -1 web-post-br)
   (key-wall-brace 3 lastrow 0.5 -1 web-post-br 4 cornerrow 1 -1 web-post-bl)
   (for [x (range 4 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl x       cornerrow 0 -1 web-post-br))
   (for [x (range 5 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl (dec x) cornerrow 0 -1 web-post-br))
   ; thumb walls
   (wall-brace thumb-mr-place  0 -1 web-post-br thumb-tr-place  0 -1 thumb-post-br)
   (wall-brace thumb-mr-place  0 -1 web-post-br thumb-mr-place  0 -1 web-post-bl)
   (wall-brace thumb-br-place  0 -1 web-post-br thumb-br-place  0 -1 web-post-bl)
   (wall-brace thumb-ml-place  0  1 web-post-tr thumb-ml-place  0  1 web-post-tl)
   (wall-brace thumb-bl-place  0  1 web-post-tr thumb-bl-place  0  1 web-post-tl)
   (wall-brace thumb-br-place -1  0 web-post-tl thumb-br-place -1  0 web-post-bl)
   (wall-brace thumb-bl-place -1  0 web-post-tl thumb-bl-place -1  0 web-post-bl)
   ; thumb corners
   (wall-brace thumb-br-place -1  0 web-post-bl thumb-br-place  0 -1 web-post-bl)
   (wall-brace thumb-bl-place -1  0 web-post-tl thumb-bl-place  0  1 web-post-tl)
   ; thumb tweeners
   (wall-brace thumb-mr-place  0 -1 web-post-bl thumb-br-place  0 -1 web-post-br)
   (wall-brace thumb-ml-place  0  1 web-post-tl thumb-bl-place  0  1 web-post-tr)
   (wall-brace thumb-bl-place -1  0 web-post-bl thumb-br-place -1  0 web-post-tl)
   (wall-brace thumb-tr-place  0 -1 thumb-post-br (partial key-place 3 lastrow)  0 -1 web-post-bl)
   ; clunky bit on the top left thumb connection
   (wall-brace thumb-ml-place  0  1 web-post-tr thumb-tl-place -2.5  0 thumb-post-tl)
   (wall-brace thumb-tl-place -1  0 thumb-post-tl (partial key-place 0 cornerrow) -1  0 web-post-bl)
   ;; another incomplete try to fix this
   ;  (bottom-hull (thumb-tl-place thumb-post-tl)
   ;               (thumb-tl-place (translate [-7 0 0] thumb-post-tl))
   ;               (thumb-ml-place web-post-tr)
   ;               (thumb-ml-place (translate [0 5 -4] web-post-tr)))
  ))

(defn on-wall-place [column depth shape]
  (translate [0 0 (- depth)]
    (key-place column 0
      (->> shape
           (rotate (+ (* β (- centercol column)) (/ π 12)) [0 -1 0])      
           (rotate (* α centerrow) [-1 0 0])      
           (translate [0 (/ mount-height 2) wall-offset])
           ))))

(def rj9-vertical-offset (- (last (key-position 0 0 [0 (/ mount-height 2) 0])) 35))
(def rj9-cube   (cube 14.78 13 22.38))
(def rj9-space  (on-wall-place 1 rj9-vertical-offset rj9-cube))
(def rj9-holder (on-wall-place 1 rj9-vertical-offset 
                  (difference rj9-cube
                              (union (translate [0 2 0] (cube 10.78  9 18.38))
                                     (translate [0 0 5] (cube 10.78 13  5))))))

(def teensy-vertical-offset (+ rj9-vertical-offset 10))
(def teensy-width 20)  
(def teensy-height 12)
(def teensy-length 33)
(def teensy2-length 53)
(def teensy-pcb-thickness 1.6) 
(def teensy-offset-height 5)
(def teensy-holder-length 68)
(def teensy-holder-offset (- 0 (/ teensy-holder-length 2)))
(def teensy-holder-top-length 14)
(def teensy-holder-top-offset (- 1 (/ teensy-holder-top-length 2)))

(def teensy-holder 
    (on-wall-place 0 teensy-vertical-offset
      (translate [-5 0 0] 
        (union 
          (->> (cube 3 teensy-holder-length (+ 6 teensy-width))
               (translate [-1.5 teensy-holder-offset 0]))
          (->> (cube teensy-pcb-thickness teensy-holder-length 3)
               (translate [(/ teensy-pcb-thickness 2) teensy-holder-offset (- -1.5 (/ teensy-width 2))]))
          (->> (cube 4 teensy-holder-length 4)
               (translate [(+ 2 teensy-pcb-thickness) teensy-holder-offset (-  -1 (/ teensy-width 2))]))
          (->> (cube teensy-pcb-thickness teensy-holder-top-length 3)
               (translate [(/ teensy-pcb-thickness 2) teensy-holder-top-offset (+ 1.5 (/ teensy-width 2))]))
          (->> (cube 4 teensy-holder-top-length 4)
               (translate [(+ 2 teensy-pcb-thickness) teensy-holder-top-offset (+ 1 (/ teensy-width 2))]))
           ))))

(def usb-cutout
  (let [hole-height 7.5
        side-radius (/ hole-height 2)
        hole-width 12
        side-cylinder (->> (cylinder side-radius teensy-length)
                           (with-fn 20)
                           (translate [(/ (- hole-width hole-height) 2) 0 0]))]
    (->> (hull side-cylinder
               (mirror [-1 0 0] side-cylinder))
         (rotate (/ π 2) [1 0 0])
         (rotate (/ π 2) [0 1 0])
         (on-wall-place 0 teensy-vertical-offset))))

(def usb-cutout
  (on-wall-place 0 teensy-vertical-offset (->> (cube 9 30 12)
                                               (translate [-1 10 0]))))

(defn screw-insert [column row bottom-radius top-radius height] 
  (let [position (key-position column row [0 0 0])
        column-offset (/ mount-width 2)
        row-offset    (/ mount-height 2)
        shift-right   (= column lastcol)
        shift-left    (= column 0)
        shift-up      (and (not (or shift-right shift-left)) (= row 0))
        shift-down    (and (not (or shift-right shift-left)) (= row lastrow))
        is-vertical   (or shift-left shift-right)
        col-angle     (+ (* β (- centercol column)) (/ π 12))
        row-angle     (* α (- row centerrow))]
    (->> (union (cylinder [bottom-radius top-radius] height)
                (translate [0 0 (/ height 2)] (sphere top-radius)))
         (translate [(first position) (second position) (/ height 2)])
         (translate [(* (if shift-right 1 (if shift-left -1 0)) column-offset)
                     (* (if shift-up    1 (if shift-down -1 0)) row-offset)
                     0])
         (translate [(* wall-offset (Math/sin col-angle))
                     (* wall-offset (Math/sin row-angle))
                     0]))))

(defn screw-insert-shapes [bottom-radius top-radius height]
  (union (screw-insert 0 0         bottom-radius top-radius height)
         (screw-insert 0 cornerrow bottom-radius top-radius height)
         (screw-insert 3 lastrow   bottom-radius top-radius height)
         (screw-insert 3 0         bottom-radius top-radius height)
        ;  (screw-insert lastcol (dec cornerrow) radius height)
         ))
(def screw-insert-height 3.8)
(def screw-insert-bottom-radius (/ 5.31 2))
(def screw-insert-top-radius (/ 5.1 2))
(def screw-insert-holes  (screw-insert-shapes screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))
(def screw-insert-outers (screw-insert-shapes (+ screw-insert-bottom-radius 1.6) (+ screw-insert-top-radius 1.6) (+ screw-insert-height 1.6)))


(spit "things/right.scad"
      (write-scad (difference 
                   (union
                    key-holes
                    connectors
                    thumb
                    thumb-connectors
                    (difference (union case-walls 
                                       screw-insert-outers 
                                       (if (= nrows 4) teensy-holder)) 
                                rj9-space 
                                usb-cutout 
                                screw-insert-holes)
                    rj9-holder
                    ; thumbcaps
                    ; caps
                    )
                   (translate [0 0 -20] (cube 350 350 40)) 
                  ;  (translate [0 0 -50] (cube 5 5 20)) 
                  )))
                   

(spit "things/test.scad"
      (write-scad (intersection (translate [29 -5 0] (cube 30 30 30))
                   (difference (union case-walls screw-insert-outers) 
                               screw-insert-holes)
                   )))

; (spit "things/test.scad"
;       (write-scad screw-insert-holes))

; (spit "things/test-half.scad"
;       (write-scad (difference 
;                    (union
;                     key-holes
;                     connectors
;                     thumb
;                     thumb-connectors
;                     (difference (union case-walls screw-insert-outers) 
;                                 ; rj9-space 
;                                 usb-cutout 
;                                 screw-insert-holes)
;                     ; rj9-holder
;                     ; (if (= nrows 4) teensy-holder)
;                    )
;                    (translate [0 0 -5] (cube 350 350 40)) 
;                   )))