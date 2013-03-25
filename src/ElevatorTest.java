import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ElevatorTest {

    private int numFloors;
    private int numElevators;
    private int numRiders;
    private int minRequestBatchSize = 1;
    private int maxRequestBatchSize = 10;
    private int minSleepTime = 1000;
    private int maxSleepTime = 3000;
    private int timeOutLimit;
    private int myId;
    private long totalTime;
    
    private List<RunnableRider> riders;
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_ELEVATOR_TEST) {
            System.out.printf(format, args);
        }
    }
    
    public ElevatorTest(int id, int floors, int elevators, int riders, int timeOut) {
        myId = id;
        numFloors = floors;
        numElevators = elevators;
        numRiders = riders;
        timeOutLimit = timeOut;
        totalTime = -1;
        print("**ElevatorTest: created test %d with %d floors, %d elevators, %d riders, %d time limit\n", myId, 
              numFloors, numElevators, numRiders, timeOutLimit);
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
        Building building = new Building(numFloors, numElevators);
        riders = new ArrayList<RunnableRider>(numRiders);
        List<Thread> riderThreads = new ArrayList<Thread>(numRiders);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        for (int i=0; i<numRiders; i++) {
//            Random random = new Random();
//            int from = random.nextInt(building.getNumFloors());
//            int to = from;
//            while (to == from) {
//                to = random.nextInt(building.getNumFloors());
//            }
            int from = 1;
            int to = 2;
            riders.add(new RunnableRider(building, from, to, i));
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
        return true;
    }

}
