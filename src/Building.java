import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Building implements IBuilding {

    private int numFloors;
    private int numElevators;
    private Map<Integer,EventBarrier[]> myEventBarriers;
    private List<RunnableElevator> myElevators;
//    private EventBarrier myRequestBarrier;
    
    /**
     * myEventBarriers maps integers representing floor numbers to EventBarrier arrays containing 3 EventBarriers,
     * 1) going up, 2) going down, 3) exiting at that floor
     * 
     * myNewRequestEventBarrier is the event barrier that elevators arrive and complete at when waiting for or
     * delivering passengers.
     */
    
    public static final int ACTION_UP = 0;
    public static final int ACTION_DOWN = 1;
    public static final int ACTION_EXIT = 2;
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_BUILDING) {
            System.out.printf(format, args);
        }
    }
    
    public Building (int numFloors, int numElevators) {
        this.numFloors = numFloors;
        this.numElevators = numElevators;
        myEventBarriers = new HashMap<Integer,EventBarrier[]>();
        myElevators = new ArrayList<RunnableElevator>();
//        myRequestBarrier = new EventBarrier(0);
        int currentId = 1;
        for (int i=0; i<numFloors; i++) {
            myEventBarriers.put(i, new EventBarrier[]{new EventBarrier(currentId), new EventBarrier(currentId+1), new EventBarrier(currentId+2)});
            currentId += 3;
        }
        
        for (int i=0; i<numElevators; i++) {
            RunnableElevator el = new RunnableElevator(this, numFloors, i, ElevatorConstants.ELEVATOR_MAX_OCCUPANCY);
            myElevators.add(el);
            el.runThread();
        }
        print("****Building: building created with %d elevators and %d floors\n", numElevators, numFloors);
    }

    @Override
    public RunnableElevator CallUp (int fromFloor) {
        if (fromFloor >= numFloors) {
            return null;
        }
        RunnableElevator el = assignElevator(fromFloor, RunnableElevator.DIRECTION_UP);
        print("****Building: CallUp -- assigned elevator %d to callup from floor %d\n", el.getId(), fromFloor);
//        el.runThread();
        return el;
        
    }

    @Override
    public RunnableElevator CallDown (int fromFloor) {
        if (fromFloor >= numFloors) {
            return null;
        }
        RunnableElevator el = assignElevator(fromFloor, RunnableElevator.DIRECTION_DOWN);
        print("****Building: CallDown -- assigned elevator %d to calldown from floor %d\n", el.getId(), fromFloor);
//        el.runThread();
        return el;
    }
    
//    public void signalNewRequest() {
//        print("****Building: signalNewRequest -- raising at request barrier %d with %d waiters\n", myRequestBarrier.getId(),
//              myRequestBarrier.waiters());
//        myRequestBarrier.raise();
//        print("****Building: signalNewRequest -- completing at request barrier %d\n", myRequestBarrier.getId());
//    }
    
    public EventBarrier getBarrierForFloorAndAction(int floor, int action) {
        Map<Integer,EventBarrier[]> barriers = getBarriers();
        EventBarrier[] array = barriers.get(floor);
        if (array == null) {
            return null;
        }
        EventBarrier result = array[action];
        return result;
    }
    
//    public EventBarrier getRequestEventBarrier() {
//        return myRequestBarrier;
//    }
    
    public int getNumFloors() {
        return numFloors;
    }
    
    public int getNumElevators() {
        return numElevators;
    }

    public Map<Integer,EventBarrier[]> getBarriers() {
        return myEventBarriers;
    }
    
    public List<RunnableElevator> getElevators() {
        return myElevators;
    }
    
    /**
     * Assign an elevator to move to a floor. Priority goes to elevators that are idle, then elevators
     * heading towards the floor, then random.
     * @param floor
     * @return
     */
    private RunnableElevator assignElevator(int floor, int direction) {
        print("****Building: assignElevator -- assigning elevator to visit floor %d\n", floor);
        List<RunnableElevator> elevators = getElevators();
        for (RunnableElevator elevator : elevators) {
            if (elevator.getDirection() == RunnableElevator.DIRECTION_IDLE) {
//                elevator.addFloor(floor, direction);
                return elevator;
            }
            if (elevator.getFloor() < floor && elevator.getDirection() == RunnableElevator.DIRECTION_UP) {
//                elevator.addFloor(floor, direction);
                return elevator;
            }
        }
        for (RunnableElevator elevator : elevators) {
            if (elevator.getFloor() > floor && elevator.getDirection() == RunnableElevator.DIRECTION_DOWN) {
//                elevator.addFloor(floor, direction);
                return elevator;
            }
        }
        Random random = new Random();
        double elId = Math.floor(elevators.size() * random.nextFloat());
        RunnableElevator el = elevators.get((int) elId);
//        el.addFloor(floor, direction);
        return el;        
    }

}
