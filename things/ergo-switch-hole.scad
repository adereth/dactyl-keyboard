union () {
  union () {
    translate ([0, 7.95, 2]) {
      cube ([17.4, 1.5, 4], center=true);
    }
    translate ([7.95, 0, 2]) {
      cube ([1.5, 17.4, 4], center=true);
    }
    hull () {
      translate ([7.95, 0, 2]) {
        cube ([1.5, 2.75, 4], center=true);
      }
      translate ([7.2, 0, 1]) {
        rotate (a=90.0, v=[1, 0, 0]) {
          cylinder ($fn=30, h=2.75, r=1, center=true);
        }
      }
    }
  }
  mirror ([0, 1, 0]) {
    mirror ([1, 0, 0]) {
      union () {
        translate ([0, 7.95, 2]) {
          cube ([17.4, 1.5, 4], center=true);
        }
        translate ([7.95, 0, 2]) {
          cube ([1.5, 17.4, 4], center=true);
        }
        hull () {
          translate ([7.95, 0, 2]) {
            cube ([1.5, 2.75, 4], center=true);
          }
          translate ([7.2, 0, 1]) {
            rotate (a=90.0, v=[1, 0, 0]) {
              cylinder ($fn=30, h=2.75, r=1, center=true);
            }
          }
        }
      }
    }
  }
}
