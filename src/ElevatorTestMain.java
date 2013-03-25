import java.util.ArrayList;
import java.util.List;


public class ElevatorTestMain {

    private static final int numTrials = 1;
    private static final int numFloors = 3;
    private static final int numElevators = 1;
    private static final int numRiders = 3;
    private static final int timeOut = 200;
    
    public static void main(String[] args) {
        System.out.printf("Beginning elevator tests:\n\n");
        int success = 0;
        int total = 0;
        int timeSoFar = 0;
        List<List<List<Integer>>> failCases = new ArrayList<List<List<Integer>>>();
        for (int i=0; i<numTrials; i++) {
            System.out.printf("**************************\n" +
                              "*     Begin trial %d\n" +
                              "**************************\n\n", i);            		                          
            
            ElevatorTest test = new ElevatorTest(i, numFloors, numElevators, numRiders, timeOut);
            boolean trialResult = test.test();
            if (trialResult) {
                success++;
                timeSoFar += test.getTestTime();
            } else {
                failCases.add(test.getTestSetup());
            }
            total++;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (trialResult) {
                System.out.println();
                System.out.printf("***************************\n" +
                                  "* End trial %d\n" +
                                  "* TRIAL SUCCESS!!!\n" +
                                  "* Time to complete: %dms\n" +
                                  "***************************\n\n", i, test.getTestTime());
            } else {
                System.out.println();
                System.out.printf("**************************\n" +
                                  "* End trial %d\n" +
                                  "* TRIAL FAILURE!!!\n" +
                                  "* Timed out after: %dms\n" +
                                  "**************************\n\n", i, test.getTestTime());
            }
        }
        long avgTime;
        if (success == 0) {
            avgTime = -1;
        } else {
            avgTime = timeSoFar / success;
        }
        int successRate = 100 * success / total;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Completed %d trials, succeeded %d out of %d, success rate %d percent, average success time %dms\n", 
                          numTrials, success, total, successRate, avgTime);
        for (List<List<Integer>> fail : failCases) {
            System.out.printf("Failed for case with riders:\n");
            for (List<Integer> rider : fail) {
                System.out.printf("Rider id %d from %d to %d\n", rider.get(0), rider.get(1), rider.get(2));
            }
            System.out.println();
        }
        System.exit(0);
    }
    
}
