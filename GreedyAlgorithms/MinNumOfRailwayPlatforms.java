import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* MinNumOfRailwayPlatforms class to find the minimum number of railway platforms required
 * to accommodate all trains at a railway station given their arrival and departure times.
 */

//

public class MinNumOfRailwayPlatforms {
    public static void main(String[] args) {
        MinNumOfRailwayPlatforms obj = new MinNumOfRailwayPlatforms();
        int[] arrival = {1000, 1010, 1030, 1045, 1100};
        int[] departure = {1015, 1025, 1040, 1050, 1115};
        System.out.println(obj.findPlatform(arrival, departure)); // Expected output: 2
    }

    private int findPlatform(int[] arrival, int[] departure) {
        int n= arrival.length;
        int platformCount =0;
        List<TrainDetails> trains = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            trains.add(new TrainDetails(arrival[i], departure[i], i));
        }
        Collections.sort(trains);

        int platformsNeeded = 1, maxPlatforms = 1;
        int i = 1, j = 0;

        while (i < n && j < n) {
            if (trains.get(i).arrivalTime <= trains.get(j).departureTime) {
                platformsNeeded++;
                i++;
            } else {
                platformsNeeded--;
                j++;
            }
            maxPlatforms = Math.max(maxPlatforms, platformsNeeded);
        }
        return maxPlatforms;

        /*
        APPROACH:
            1.	Sort the arrival and departure times separately.
            2.	Use two pointers to traverse both arrays.
            3.	Keep track of:
                •	platform_needed: Current platforms in use
                •	max_platforms: Maximum platforms needed at any time

        Whenever:
            •	A train arrives before the earliest departure ⇒ Increment platform_needed
            •	A train departs before or at the next arrival ⇒ Decrement platform_needed
         */
    }


        private int findPlatformInefficientMethod(int[] arrival, int[] departure) {
        int n= arrival.length;
        int platformCount =0;
        List<TrainDetails> trains = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            trains.add(new TrainDetails(arrival[i], departure[i], i));
        }
        Collections.sort(trains);
        while(!trains.isEmpty()){
            int previousDeparture = -1;
            System.out.println(trains);

//      INCORRECT CODE:

            platformCount++;
            for(int i =0; i<trains.size() ;i++){
                if(trains.get(i).arrivalTime > previousDeparture) {
                    previousDeparture = trains.get(i).departureTime;
                    trains.remove(i);
                }
            }

//      Why?

/*  1.	Concurrent Modification:
        •	You’re calling trains.remove(i) while iterating the same list with an index-based for loop.
        •	This shifts the list and skips some elements unintentionally.
    2.	Inefficient Platform Calculation:
	    •	You’re iterating through all remaining trains and removing compatible ones, simulating platforms manually, but the algorithm is O(N²), which is not efficient.
*/

            // CORRECT CODE 1:
            platformCount++;
            for(int i = 0; i < trains.size(); i++) {
                if(trains.get(i).arrivalTime > previousDeparture) {
                    previousDeparture = trains.get(i).departureTime;
                    trains.remove(i);
                    i--; // Adjust index after removal
                }
            }

            // CORRECT CODE 2:
            List<TrainDetails> toRemove = new ArrayList<>();

            for (TrainDetails train : trains) {
                if (train.arrivalTime > previousDeparture) {
                    previousDeparture = train.departureTime;
                    toRemove.add(train);
                }
            }

            trains.removeAll(toRemove);
            platformCount++;

        }

//      But:
//	•	This is still O(N²) in worst case, inefficient for large inputs.

        return platformCount;
    }

    public class TrainDetails implements Comparable<TrainDetails> {
        public int arrivalTime;
        public int departureTime;
        public int position;

        //cons
        public TrainDetails(int arrivalTime, int departureTime, int position) {
            this.arrivalTime = arrivalTime;
            this.departureTime = departureTime;
            this.position = position;
        }
        @Override
        public int compareTo(TrainDetails o) {
            // Sort by departure time first, then by arrival time
            if (this.departureTime != o.departureTime) {
                return this.departureTime - o.departureTime;
            } else {
                return this.arrivalTime - o.arrivalTime;
            }
        }

        @Override
        public String toString() {
            return "TrainDetails{" +
                    "arrivalTime=" + arrivalTime +
                    ", departureTime=" + departureTime +
                    ", position=" + position +
                    '}';
        }
    }
}
