package DSA.GreedyAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JobSequencingProblem {
    public static void main(String[] args) {

        //         // [ [1, 2, 100] , [2, 1, 19] , [3, 2, 27] , [4, 1, 25] , [5, 1, 15] ]
        int[][] jobs = {
                {1, 1, 100},
                {2, 2, 90},
                {3, 3, 80},
                {4, 1, 70}
        };
//        int[][] jobs = {
//            {1, 4, 20},
//            {2, 1, 10},
//            {3, 1, 40},
//            {4, 1, 30}
//        };



        int maxProfit = jobScheduling(jobs);
        System.out.println("Maximum profit from job scheduling: " + maxProfit);
    }

    private static int jobScheduling(int[][] jobs) {

        List<JobDetails> jobDetailsList = new ArrayList<>();
        int maxDeadline = 0;

        for (int[] job : jobs) {
            jobDetailsList.add(new JobDetails(job[0], job[1], job[2]));
            maxDeadline = Math.max(maxDeadline, job[1]);
        }

        Collections.sort(jobDetailsList);

        int[] slots = new int[maxDeadline + 1]; // To keep track of filled slots
        Arrays.fill(slots, -1);

        int profit=0;
        for(JobDetails jobDetails : jobDetailsList){
            System.out.println("slots--"+ Arrays.toString(slots));
            int currentDeadline = jobDetails.deadline;
            while( currentDeadline >0 && slots[currentDeadline] == 0 ){
                currentDeadline--;
                System.out.println("in while --"+ jobDetails);
            }
            if(currentDeadline >0 && slots[currentDeadline] ==-1){
                profit+=jobDetails.profit;
                slots[currentDeadline] = 0;
                System.out.println("in if --"+ jobDetails);
            }
            else
                return profit;

        }

        return profit;
    }

    /*
     this above method is correct because it properly handles the scheduling of jobs based on their deadlines and profits.
     It uses a greedy approach to select the jobs with the highest profit that can fit within their deadlines.
     we are greedy i the sense that most import is profit, then deadline, then jobId.
     so sort according to profit and then we will be greedy with deadline, we will try to do the job as last as possible
     so that if there comes a job with less no of days for deadline I can accommodate that job
     using this approach we, Maximum profit for job is assigned the last possible slot and
     if that slot is unavailable, I will move to the last possible slot within the deadline
    */

    private static int jobSchedulingIncorrect(int[][] jobs) {

        List<JobDetails> jobDetailsList = new ArrayList<>();
        int maxTasks = 0;

        for (int[] job : jobs) {
            jobDetailsList.add(new JobDetails(job[0], job[1], job[2]));
            maxTasks = Math.max(maxTasks, job[1]);
        }

        Collections.sort(jobDetailsList);
        System.out.println("Sorted Jobs: " + jobDetailsList);
        System.out.println(maxTasks);

        int maxDeadline = 0;
        int currTasks =0;
        int profit = 0;

        for(JobDetails job : jobDetailsList) {
            if(currTasks < maxTasks && job.deadline >= maxDeadline && currTasks < job.deadline) {
                System.out.println("Selected Job: " + job);
                profit+= job.profit;
                currTasks++;
                maxDeadline = Math.max(maxDeadline, job.deadline);
            }

        }
        return profit;
    }

    // why incorrect?
    /* This method is incorrect because it does not properly handle the scheduling of jobs
     based on their deadlines and profits. It simply checks if the current job can be added
     to the schedule without considering the previous jobs' deadlines and profits.
     It also does not maintain a proper record of which slots are filled, leading to incorrect profit calculations.
   */

    public static class JobDetails implements Comparable<JobDetails> {
        public int jobId;
        public int deadline;
        public int profit;

        public JobDetails(int jobId, int deadline, int profit) {
            this.jobId = jobId;
            this.deadline = deadline;
            this.profit = profit;
        }

        @Override
        public String toString() {
            return "JobDetails{" +
                    "jobId=" + jobId +
                    ", deadline=" + deadline +
                    ", profit=" + profit +
                    '}';
        }

        @Override
        public int compareTo(JobDetails o) {
            if(this.profit != o.profit) {
                return Integer.compare(o.profit, this.profit); // Sort by profit in descending order
            } else if (this.deadline != o.deadline) {
                return Integer.compare(this.deadline, o.deadline); // Sort by deadline in ascending order
            }
            return Integer.compare(this.jobId, o.jobId); // Sort by jobId in ascending order
        }
    }




}
