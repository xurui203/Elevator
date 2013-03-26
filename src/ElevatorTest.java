import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class ElevatorTest {

    private int numFloors;
    private int numElevators;
    private int numRiders;
    private int maxCapacity;
    private int minRequestBatchSize = 1;
    private int maxRequestBatchSize = 10;
    private int minSleepTime = 1000;
    private int maxSleepTime = 3000;
    private int timeOutLimit;
    private int myId;
    private long totalTime;
    
    private FileWriter myFileWriter;
    private String DELIMITER = " ";
    private Scanner myScanner;
    private List<RunnableRider> riders;
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_ELEVATOR_TEST) {
            System.out.printf(format, args);
        }
    }
    
    public ElevatorTest(File file, int timeOut) {
        try {
            myScanner = new Scanner(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String setup = myScanner.nextLine();
        String[] array = setup.split(DELIMITER);
        numFloors = Integer.parseInt(array[0]);
        numElevators = Integer.parseInt(array[1]);
        numRiders = Integer.parseInt(array[2]);
        maxCapacity = Integer.parseInt(array[3]);
        timeOutLimit = timeOut;
    }
    
    public ElevatorTest(int id, int floors, int elevators, int riders, int capacity, int timeOut) {
        myId = id;
        numFloors = floors;
        numElevators = elevators;
        numRiders = riders;
        maxCapacity = capacity;
        timeOutLimit = timeOut;
        totalTime = -1;
        myScanner = null;
        print("**ElevatorTest: created test %d with %d floors, %d elevators, %d riders, %d time limit\n", myId, 
              numFloors, numElevators, numRiders, timeOutLimit);
    }
    
    /**
     * Set the output file.
     */
    public void setWriter(FileWriter writer) {
        myFileWriter = writer;
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
    
    /**
     * Call ONLY after running test, otherwise will return -1;
     * @return
     */
    public long getTestTime() {
        return totalTime;
    }
    
    public List<List<Integer>> getTestSetup() {
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        for (RunnableRider rider : riders) {
            result.add(rider.getDetails());
        }
        return result;
    }
    
    public boolean test() {
        long startTime = System.currentTimeMillis();
        write((new Date(System.currentTimeMillis())).toString() + ": begin elevator test with id " + myId + "\n");
        Building building = new Building(myFileWriter, numFloors, numElevators, maxCapacity);
        riders = new ArrayList<RunnableRider>(numRiders);
        List<Thread> riderThreads = new ArrayList<Thread>(numRiders);
        for (int i=0; i<numRiders; i++) {
            if (myScanner == null) {
                Random random = new Random();
                int from = random.nextInt(building.getNumFloors());
                int to = from;
                while (to == from) {
                    to = random.nextInt(building.getNumFloors());
                }
                RunnableRider rider = new RunnableRider(building, to, from, i);
                rider.setWriter(myFileWriter);                
                riders.add(rider);
            } else {
                String riderString = myScanner.nextLine();
                String[] riderArray = riderString.split(DELIMITER);
                int id = Integer.parseInt(riderArray[0]);
                int from = Integer.parseInt(riderArray[1]);
                int to = Integer.parseInt(riderArray[2]);
                RunnableRider rider = new RunnableRider(building, to, from, id);
                rider.setWriter(myFileWriter);
                riders.add(rider);
            }
        }
        int riderCount = 0;
        Random rand = new Random();
        int batchSize = rand.nextInt(maxRequestBatchSize - minRequestBatchSize) + minRequestBatchSize;
        print("**ElevatorTest: test -- test %d: releasing %d requests\n", myId, batchSize - riderCount);
        for (RunnableRider rider : riders) {
            if (riderCount == batchSize) {
                batchSize = rand.nextInt(maxRequestBatchSize - minRequestBatchSize) + minRequestBatchSize;
                batchSize += riderCount;
                Random random = new Random();
                int sleepTime = random.nextInt(maxSleepTime - minSleepTime) + minSleepTime;
                print("**ElevatorTest: test -- test %d: sleeping for %dms\n", myId, sleepTime);
                try {
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                print("**ElevatorTest: test -- test %d: slept for %dms\n", myId, sleepTime);
                print("**ElevatorTest: test -- test %d: releasing %d requests\n", myId, batchSize - riderCount);
            }
            riderCount++;
            Thread thread = new Thread(rider);
            riderThreads.add(thread);                        
            thread.start();
        }
        while (!riderThreads.isEmpty()) {
            long currentTime = System.currentTimeMillis() - startTime;
            if (currentTime >= timeOutLimit) {
                print("**ElevatorTest: test -- test %d: rider transportation timed out after %dms\n", myId, currentTime);
                totalTime = currentTime;
                return false;
            }
            for (int i=0; i<riderThreads.size(); i++) {
                Thread thread = riderThreads.get(i);
                if (!thread.isAlive()) {
                    riderThreads.remove(i);
                    i--;
                }
            }
        }
        totalTime = System.currentTimeMillis() - startTime;
        print("**ElevatorTest: main -- test %d: all riders transported in %dms\n", myId, totalTime);
        write((new Date(System.currentTimeMillis())).toString() + ": end elevator test with id " + myId + "\n");
        return true;
    }

}
