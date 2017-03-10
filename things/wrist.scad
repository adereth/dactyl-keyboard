union () {
  hull () {
    translate ([73/2, 27.25, 45.75]) {
      cylinder (h=1.5, r=11, center=true);
    }
    mirror ([-1, 0, 0]) {
      translate ([73/2, 27.25, 45.75]) {
        cylinder (h=1.5, r=11, center=true);
      }
    }
    mirror ([0, -1, 0]) {
      translate ([73/2, 27.25, 45.75]) {
        cylinder (h=1.5, r=11, center=true);
      }
    }
    mirror ([-1, 0, 0]) {
      mirror ([0, -1, 0]) {
        translate ([73/2, 27.25, 45.75]) {
          cylinder (h=1.5, r=11, center=true);
        }
      }
    }
  }
  hull () {
    translate ([73/2, 27.25, 45.75]) {
      cylinder (h=1.5, r=11, center=true);
    }
    translate ([0, 0, 1/2]) {
      linear_extrude (height=1, twist=0.0, convexity=0, center=true){
        projection (cut = false) {
          translate ([73/2, 27.25, 45.75]) {
            cylinder (h=1.5, r=11, center=true);
          }
        }
      }
    }
  }
  hull () {
    mirror ([-1, 0, 0]) {
      translate ([73/2, 27.25, 45.75]) {
        cylinder (h=1.5, r=11, center=true);
      }
    }
    translate ([0, 0, 1/2]) {
      linear_extrude (height=1, twist=0.0, convexity=0, center=true){
        projection (cut = false) {
          mirror ([-1, 0, 0]) {
            translate ([73/2, 27.25, 45.75]) {
              cylinder (h=1.5, r=11, center=true);
            }
          }
        }
      }
    }
  }
  hull () {
    mirror ([0, -1, 0]) {
      translate ([73/2, 27.25, 45.75]) {
        cylinder (h=1.5, r=11, center=true);
      }
    }
    translate ([0, 0, 1/2]) {
      linear_extrude (height=1, twist=0.0, convexity=0, center=true){
        projection (cut = false) {
          mirror ([0, -1, 0]) {
            translate ([73/2, 27.25, 45.75]) {
              cylinder (h=1.5, r=11, center=true);
            }
          }
        }
      }
    }
  }
  hull () {
    mirror ([-1, 0, 0]) {
      mirror ([0, -1, 0]) {
        translate ([73/2, 27.25, 45.75]) {
          cylinder (h=1.5, r=11, center=true);
        }
      }
    }
    translate ([0, 0, 1/2]) {
      linear_extrude (height=1, twist=0.0, convexity=0, center=true){
        projection (cut = false) {
          mirror ([-1, 0, 0]) {
            mirror ([0, -1, 0]) {
              translate ([73/2, 27.25, 45.75]) {
                cylinder (h=1.5, r=11, center=true);
              }
            }
          }
        }
      }
    }
  }
}
