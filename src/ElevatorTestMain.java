import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;


public class ElevatorTestMain {

    private int numTrials;
    private int numFloors;
    private int numElevators;
    private int numRiders;
    private int maxCapacity;
    private int timeOut;   
    private FileWriter myFileWriter;
    private final Scanner myInput = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
    private final File OUTPUT_FILE = new File("data/elevator.log");    
    private final JFileChooser INPUT_CHOOSER = 
            new JFileChooser(System.getProperties().getProperty("user.dir"));
    
    public void testFile() {
        int response = INPUT_CHOOSER.showOpenDialog(null);
        File file = null;
        if (response == JFileChooser.APPROVE_OPTION) {
            file =INPUT_CHOOSER.getSelectedFile();
        } else {
            return;
        }
        System.out.printf("Beginning elevator tests:\n\n");
        System.out.printf("**************************\n" +
                          "*       Begin trial      *\n" +
                          "**************************\n\n");                                               
        
        ElevatorTest test = new ElevatorTest(file, timeOut);
        test.setWriter(myFileWriter);
        boolean trialResult = test.test();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (trialResult) {
            System.out.println();
            System.out.printf("***************************\n" +
                              "*        End trial        *\n" +
                              "*     TRIAL SUCCESS!!!    *\n" +
                              "*  Time to complete: %dms *\n" +
                              "***************************\n\n", test.getTestTime());
        } else {
            System.out.println();
            System.out.printf("**************************\n" +
                              "*        End trial       *\n" +
                              "*     TRIAL FAILURE!!!   *\n" +
                              "*  Timed out after: %dms *\n" +
                              "**************************\n\n", test.getTestTime());
            System.out.printf("Failed for case with riders:\n");
            List<List<Integer>> fail = test.getTestSetup();
            for (List<Integer> rider : fail) {
                System.out.printf("Rider id %d from %d to %d\n", rider.get(0), rider.get(1), rider.get(2));
            }
            System.out.println();
        }
        try {
            myFileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void testRandom() {
        System.out.printf("Beginning elevator tests:\n\n");
        int success = 0;
        int total = 0;
        int timeSoFar = 0;
        List<List<List<Integer>>> failCases = new ArrayList<List<List<Integer>>>();
        for (int i=0; i<numTrials; i++) {
            System.out.printf("**************************\n" +
                              "*     Begin trial %d\n" +
                              "**************************\n\n", i);                                               
            
            ElevatorTest test = new ElevatorTest(i, numFloors, numElevators, numRiders, maxCapacity, timeOut);
            test.setWriter(myFileWriter);
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
        write("Completed " + numTrials + " trials, succeeded " + success + " out of " + total + ", success rate "
                          + successRate + " percent, average success time " + avgTime + "ms\n");        
        for (List<List<Integer>> fail : failCases) {
            System.out.printf("Failed for case with riders:\n");
            write("Failed for case with riders:\n");
            for (List<Integer> rider : fail) {
                System.out.printf("Rider id %d from %d to %d\n", rider.get(0), rider.get(1), rider.get(2));
                write("Rider id " + rider.get(0) + " from " + rider.get(1) + " to " + rider.get(2) + "\n");
            }
            System.out.println();
            write("\n");
        }
        try {
            myFileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Writes to output file.
     */
    public void write(String string) {
        synchronized (myFileWriter) {
            try {
                myFileWriter.write(string);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public String readString(String prompt) {
        System.out.printf("%s ", prompt);
        return myInput.nextLine();
    }
    
    public static void main(String[] args) {
        ElevatorTestMain test = new ElevatorTestMain();        
        String choice = test.readString(("Please select form of test to run\n\t(1)-test with input file\n\t" +
        		"(2)-test with randomly generated riders\nchoice:"));
        if (choice.equals("1")) {
            String timeOut = test.readString("Please enter time out limit in milliseconds:");
            try {
                test.timeOut = Integer.parseInt(timeOut);
            } catch (NumberFormatException e) {
                System.exit(1);
            }
            try {
                test.myFileWriter = new FileWriter(test.OUTPUT_FILE);
            }
            catch (IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
            test.testFile();
        } else if (choice.equals("2")) {
            try {
                String numTrials = test.readString("Please enter number of trials:");
                test.numTrials = Integer.parseInt(numTrials);
                String numFloors = test.readString("Please enter number of floors:");
                test.numFloors = Integer.parseInt(numFloors);
                String numElevators = test.readString("Please enter number of elevators:");
                test.numElevators = Integer.parseInt(numElevators);
                String numRiders = test.readString("Please enter number of riders:");
                test.numRiders = Integer.parseInt(numRiders);
                String maxCapacity = test.readString("Please enter max elevator capacity:");
                test.maxCapacity = Integer.parseInt(maxCapacity);
                String timeOut = test.readString("Please enter time out limit in milliseconds:");            
                test.timeOut = Integer.parseInt(timeOut);
                try {
                    test.myFileWriter = new FileWriter(test.OUTPUT_FILE);
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.exit(1);
            }
            test.testRandom();
        }
        System.exit(0);
    }
    
}
