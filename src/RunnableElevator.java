import java.util.Collections;
import java.util.PriorityQueue;


public class RunnableElevator implements Runnable, IElevator {

    private int numFloors; 
    private int myId;
    private int currentRiders;
    private int maxRiders;    
    private int myDirection;
    private int myFloor;
    private Building myBuilding;
//    private EventBarrier myRequestBarrier;
    private PriorityQueue<Integer> myUpFloors;
    private PriorityQueue<Integer> myDownFloors;
    
    public static final int DIRECTION_IDLE = 0;
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = 2;
    
    public RunnableElevator (Building building, int numFloors, int elevatorId, int maxOccupancyThreshold) {
        this.numFloors = numFloors;
        myBuilding = building;
        myId = elevatorId;
        currentRiders = 0;
        maxRiders = maxOccupancyThreshold;
        myUpFloors = new PriorityQueue<Integer>();
        myDownFloors = new PriorityQueue<Integer>(0, Collections.reverseOrder());
//        myRequestBarrier = myBuilding.getNewRequestEventBarrier();
        myDirection = DIRECTION_IDLE;
    }

    /**
     * Anytime the request barrier raises, we visit floors we need to then complete. This runs in an
     * infinite while loop, so program must be explicitly terminated with System.exit() when all requests
     * in the Building are made and completed.
     */
    @Override
    public void run () {
        while (true) {
//            while (myUpFloors.peek() == null && myDownFloors.peek() == null) {
//                myRequestBarrier.arrive();
//            }
            VisitFloor();
        }        
    }

    @Override
    public void OpenDoors () {
        int action;
        if (myDirection == DIRECTION_UP) {
            action = Building.ACTION_UP;
        } else if (myDirection == DIRECTION_DOWN) {
            action = Building.ACTION_DOWN;
        } else {
            return;
        }
        EventBarrier openBarrier = myBuilding.getBarrierForFloorAndAction(myFloor, action);
        openBarrier.raise();
        CloseDoors();
    }

    @Override
    public void CloseDoors () {
        if (myUpFloors.peek() != null || myDownFloors.peek() != null) {
            VisitFloor();
        }
    }

    /**
     * Visit the next floor. There are three cases.
     * 1) We are going up. In this case we go to the next floor up, unless there are no more, in
     * which case we switch directions and go down. If there are no down floors, we go idle.
     * 2) We are going down. Reverse of above.
     * 3) We are idle. If there are only floors above or below us to visit, we go in that direction.
     * Otherwise, we go to the closest floor to be visited and continue traveling in that direction.
     * If there are no floors to visit, we remain idle.
     */
    @Override
    public void VisitFloor () {
        int nextFloor;
        if (myDirection == DIRECTION_UP) {
            if (myUpFloors.peek() != null) {
                nextFloor = myUpFloors.poll();
            } else {
                if (myDownFloors.peek() != null) {
                    nextFloor = myDownFloors.poll();
                    myDirection = DIRECTION_DOWN;
                } else {                    
                    myDirection = DIRECTION_IDLE;
                    return;
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
                    return;
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
                return;
            } else {
                int closestUp = myUpFloors.peek();
                int closestDown = myDownFloors.peek();
                if (Math.abs(myFloor - closestUp) < Math.abs(myFloor - closestDown)) {
                    nextFloor = myUpFloors.poll();
                    myDirection = DIRECTION_UP;
                } else {
                    nextFloor = myDownFloors.poll();
                    myDirection = DIRECTION_DOWN;
                }
            }
        }
        myFloor = nextFloor;
        OpenDoors();
    }

    @Override
    public boolean Enter () {
//        uncomment this once max capacity for elevator is implemented
//        if (currentRiders == maxRiders) {
//            return false;
//        }
        currentRiders++;
        return true;
    }

    @Override
    public void Exit () {
        currentRiders--;
    }

    @Override
    public void RequestFloor (int floor) {
        addFloor(floor);
    }
    
    public void addFloor(int floor) {
        if (myUpFloors.contains(floor) || myDownFloors.contains(floor)) {
            return;
        }
        if (floor > myFloor) {
            myUpFloors.add(floor);
        } else {
            myDownFloors.add(floor);
        }
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

}
