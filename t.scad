minkowski()
     {
     circle(1);
     for(i=[0:5])
          {
          rotate([0,0,360/5 * i])
          hull()
               {
               translate([20,0,0]) square(10);
               square(2);
               }
          }
     }