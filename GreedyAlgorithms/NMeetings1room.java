import java.util.Arrays;

/**
 * NMeetings1room class to find the maximum number of meetings that can be accommodated in a single room.
 * Meetings are represented by their start and end times.
 */

// The idea is to use greedy approach to always choose the meeting whose

// start time is greater than the end time of the previously selected meeting and

// end time is the smallest among all the remaining meetings.
// This is because, smaller the end time, sooner the meeting will end and the meeting room will become available for the next meeting.

// So, we can sort the meetings according to their end time so that we always choose the meeting which has minimum end time.

public class NMeetings1room {
    public static void main(String[] args) {
        NMeetings1room obj = new NMeetings1room();
        int[] start = {0,3,1,5,5,8};
        int[] end = {5,4,2,9,7,9};
        System.out.println(obj.maxMeetings(start, end)); // Expected output: 4
    }
    public int maxMeetings(int[] start, int[] end) {
        //your code goes here
        int maxMeetings = 0;

        int n = start.length;
        MeetingDetails[] meetings = new MeetingDetails[n];
        for(int i = 0; i < n; i++) {
            meetings[i] = new MeetingDetails();
            meetings[i].startTime = start[i];
            meetings[i].endTime = end[i];
            meetings[i].position = i;
        }
        // Sort meetings by end time, then by start time
        Arrays.sort(meetings);
        int lastEndTime = -1;
        for(MeetingDetails meetingDetails :meetings){
            if(meetingDetails.startTime > lastEndTime) {
                // If the meeting starts after the last selected meeting ends, select it
                maxMeetings++;
                lastEndTime = meetingDetails.endTime;
            }
        }
        return maxMeetings;


    }

    public class MeetingDetails implements Comparable<MeetingDetails> {
        public int startTime;
        public int endTime;
        public int position;


        @Override
        public int compareTo(MeetingDetails o) {
            // Sort by start time first, then by end time
            if (this.endTime != o.endTime) {
                return this.endTime - o.endTime;
            } else {
                return this.startTime - o.startTime;
            }
        }

        @Override
        public String toString() {
            return "MeetingDetails{" +
                    "startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", position=" + position +
                    '}';
        }
    }
}
