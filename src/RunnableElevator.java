import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.PriorityQueue;


public class RunnableElevator implements Runnable, IElevator {

    private int numFloors; 
    private int myId;
    private int currentRiders;
    private int maxRiders;    
    private int myDirection;
    private int myFloor;
    private Thread myThread;
    private Building myBuilding;    
    private PriorityQueue<ElevatorCall> myUpFloors;
    private PriorityQueue<ElevatorCall> myDownFloors;
    
    private FileWriter myFileWriter;
    
    public static final int DIRECTION_IDLE = 0;
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = 2;    
    public static final ElevatorCall NULL_CALL = new ElevatorCall(-1, -1);
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_ELEVATOR) {
            System.out.printf(format, args);
        }
    }
    
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
    
    public RunnableElevator (Building building, int numFloors, int elevatorId, int maxOccupancyThreshold) {
        this.numFloors = numFloors;
        myFloor = 0;
        myBuilding = building;
        myId = elevatorId;
        currentRiders = 0;
        maxRiders = maxOccupancyThreshold;
        myUpFloors = new PriorityQueue<ElevatorCall>();
        myDownFloors = new PriorityQueue<ElevatorCall>(1, Collections.reverseOrder());
        myDirection = DIRECTION_IDLE;
        print("****RunnableElevator: elevator %d created\n", myId);
    }
    
    public void runThread() {
        myThread = new Thread(this);
        myThread.start();
    }

    /**
     * Any time the request barrier raises, we visit floors we need to then complete. This runs in an
     * infinite while loop, so program must be explicitly terminated with System.exit() when all requests
     * in the Building are made and completed.
     */
    @Override
    public void run () {
        while (true) {
            print("****RunnableElevator: run -- elevator %d waiting for requests\n", myId);
            while (!checkRequests()) {                
//                print("****RunnableElevator: run -- elevator %d going to sleep\n", myId);
                try {
                    synchronized (this) {
                        wait();
                        print("****RunnableElevator: run -- elevator %d awoken from wait\n", myId);
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            
            // pause for a bit in case other requests come in
            try {
                Thread.sleep(ElevatorConstants.ELEVATOR_REQUEST_DELAY_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            /**
             * Synchronized block just to avoid concurrent modification exceptions when printing.
             */
            synchronized (this) {
            
                print("****RunnableElevator: run -- elevator %d has up requests %s and down requests %s\n", myId, 
                      myUpFloors.toString(), myDownFloors.toString());
            
            }
            
            while (checkRequests()) {                
                VisitFloor();
            }
        }          
    }
    
    /**
     * Determine the next floor to visit. Also updates myDirection accordingly. Synchronized so that
     * the requests are not modified while determining the next floor. There are three cases.
     * 1) We are going up. In this case we go to the next floor up, unless there are no more, in
     * which case we switch directions and go down. If there are no down floors, we go idle.
     * 2) We are going down. Reverse of above.
     * 3) We are idle. If there are only floors above or below us to visit, we go in that direction.
     * Otherwise, we go to the closest floor to be visited and continue traveling in that direction.
     * If there are no floors to visit, we remain idle.
     */
    public synchronized ElevatorCall determineNextFloor() {
        print("****RunnableElevator: determineNextFloor -- elevator %d determining next floor to visit\n", myId);
        ElevatorCall nextFloor;
        if (myDirection == DIRECTION_UP) {
            if (myUpFloors.peek() != null) {
                nextFloor = myUpFloors.poll();
            } else {
                if (myDownFloors.peek() != null) {
                    nextFloor = myDownFloors.poll();
                    myDirection = DIRECTION_DOWN;
                } else {                    
                    myDirection = DIRECTION_IDLE;
                    return NULL_CALL;
                }
            }
        } else if (myDirection == DIRECTION_DOWN) {
            if (myDownFloors.peek() != null) {
                nextFloor = myDownFloors.poll();
            } else {
                if (myUpFloors.peek() != null) {
                    nextFloor = myUpFloors.poll();
                    myDirection = DIRECTION_UP;
                } else {                    
                    myDirection = DIRECTION_IDLE;
                    return NULL_CALL;
                }
            }
        } else {
            if (myUpFloors.peek() != null && myDownFloors.peek() == null) {
                nextFloor = myUpFloors.poll();
                myDirection = DIRECTION_UP;
            } else if (myUpFloors.peek() == null && myDownFloors.peek() != null) {
                nextFloor = myDownFloors.poll();
                myDirection = DIRECTION_DOWN;
            } else if (myUpFloors.peek() == null && myDownFloors.peek() == null) {
                return NULL_CALL;
            } else {
                ElevatorCall closestUp = myUpFloors.peek();
                ElevatorCall closestDown = myDownFloors.peek();
                if (Math.abs(myFloor - closestUp.getFloor()) < Math.abs(myFloor - closestDown.getFloor())) {
                    nextFloor = myUpFloors.poll();
                    myDirection = DIRECTION_UP;
                } else {
                    nextFloor = myDownFloors.poll();
                    myDirection = DIRECTION_DOWN;
                }
            }
        }
        return nextFloor;
    }


    @Override
    public void VisitFloor () {
        ElevatorCall nextCall = determineNextFloor();        
        if (nextCall.equals(NULL_CALL)) {
            print("****RunnableElevator: VisitFloor -- elevator %d received NULL CALL, aborting\n", myId); 
            return;
        }
        String direction;
        if (myDirection == RunnableElevator.DIRECTION_DOWN){
            direction = "down";
            write("E" + myId + " moves down from F" + myFloor + " to F" + nextCall.getFloor() + "\n");
        } else {
            direction = "up";
            write("E" + myId + " moves up from F" + myFloor + " to F" + nextCall.getFloor() + "\n");            
        }
        print("****RunnableElevator: VisitFloor -- elevator %d moving %s from floor %d to floor %d\n", myId, 
                          direction, myFloor, nextCall.getFloor());
        myFloor = nextCall.getFloor();
        myDirection = nextCall.getDirection();
        OpenDoors();
    }
    
    /**
     * First allow those on board to exit, then allow those who need to board to enter.
     */
    @Override
    public void OpenDoors () {
        write("E" + myId + " on F" + myFloor + " opens\n");
        print("****RunnableElevator: OpenDoors -- elevator %d opening doors at floor %d\n", myId, myFloor);
        EventBarrier exitBarrier = myBuilding.getBarrierForFloorAndAction(myFloor, Building.ACTION_EXIT);
//        print("****RunnableElevator: OpenDoors -- elevator %d raising exit barrier %d at floor %d\n", myId,
//                          exitBarrier.getId(), myFloor);
        exitBarrier.raise();
//        print("****RunnableElevator: OpenDoors -- elevator %d awoke from exit barrier %d at floor %d\n", myId, 
//                          exitBarrier.getId(), myFloor);
        int action;
        if (myDirection == DIRECTION_UP) {
            action = Building.ACTION_UP;
        } else if (myDirection == DIRECTION_DOWN) {
            action = Building.ACTION_DOWN;
        } else {
            return;
        }
        EventBarrier enterBarrier = myBuilding.getBarrierForFloorAndAction(myFloor, action);
//        print("****RunnableElevator: OpenDoors -- elevator %d raising enter barrier %d at floor %d\n", myId, 
//                          enterBarrier.getId(), myFloor);
        
        // need some way to raise, have all riders attempt action ONCE, then close doors and move on before they rerequest
        enterBarrier.raise();
//        print("****RunnableElevator: OpenDoors -- elevator %d awoke from enter barrier %d at floor %d\n", myId, 
//                          enterBarrier.getId(), myFloor);
        CloseDoors();
    }

    @Override
    public void CloseDoors () {
        write("E" + myId + " on F" + myFloor + " closes\n");        
        print("****RunnableElevator: CloseDoors -- elevator %d closing doors at floor %d\n", myId, myFloor);        
    }

    @Override
    public boolean Enter () {
        if (currentRiders == maxRiders) {
            return false;
        }        
        currentRiders++;
        print("****RunnableElevator: Enter -- elevator %d gained passenger, now holding %d\n", myId, currentRiders);
        return true;
    }

    @Override
    public void Exit () {
        currentRiders--;
        print("****RunnableElevator: Exit -- elevator %d lost passenger, now holding %d\n", myId, currentRiders);
    }

    @Override
    public void RequestFloor (int floor, int direction) {
        if (floor >= numFloors) {
            return;
        }
        addFloor(floor, direction);
    }
    
    public synchronized void addFloor(int floor, int direction) {
        ElevatorCall call = new ElevatorCall(floor, direction);
        if (myUpFloors.contains(call) || myDownFloors.contains(call)) {
            print("****RunnableElevator: addFloor -- elevator %d ignored repeat request to %s\n", myId, call.toString());
            return;
        }        
        if (floor > myFloor) {
            myUpFloors.add(call);
        } else {
            myDownFloors.add(call);
        }
        print("****RunnableElevator: addFloor -- elevator %d added %s call\n", myId, call.toString());
        print("****RunnableElevator: addFloor -- elevator %d current upfloors %s and downfloors %s\n", myId,
              myUpFloors.toString(), myDownFloors.toString());
//            myBuilding.signalNewRequest();
    }
    
    public int getNumFloors() {
        return numFloors;
    }
    
    public int getId() {
        return myId;
    }
    
    public int getMaxRiders() {
        return maxRiders;
    }
    
    public int getDirection() {
        return myDirection;
    }
    
    public int getFloor() {
        return myFloor;
    }
    
    /**
     * Returns true if there are active requests, false otherwise.
     */
    public boolean checkRequests() {
        return (myUpFloors.peek() != null || myDownFloors.peek() != null);
    }

}
