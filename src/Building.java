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
//    private EventBarrier myNewRequestEventBarrier;
    
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
    
    public Building (int numFloors, int numElevators) {
        this.numFloors = numFloors;
        this.numElevators = numElevators;
        myEventBarriers = new HashMap<Integer,EventBarrier[]>();
        myElevators = new ArrayList<RunnableElevator>();
//        myNewRequestEventBarrier = new EventBarrier();
        for (int i=0; i<numFloors; i++) {
            myEventBarriers.put(i, new EventBarrier[]{new EventBarrier(), new EventBarrier(), new EventBarrier()});
            // TODO: replace the 0's in EventBarrier constructor with total capacity of that barrier,
            // probably something like number of elevators plus elevator capacity but more likely just infinite 
        }
        for (int i=0; i<numElevators; i++) {
            myElevators.add(new RunnableElevator(this, numFloors, i, 0));
            // TODO: replace 0 with max occupancy threshold
        }
    }

    @Override
    public RunnableElevator CallUp (int fromFloor) {
        RunnableElevator el = assignElevator(fromFloor);        
        return el;
        
    }

    @Override
    public RunnableElevator CallDown (int fromFloor) {
        RunnableElevator el = assignElevator(fromFloor);
        return el;
    }
    
    public EventBarrier getBarrierForFloorAndAction(int floor, int action) {
        Map<Integer,EventBarrier[]> barriers = getBarriers();
        EventBarrier[] array = barriers.get(floor);
        if (array == null) {
            return null;
        }
        EventBarrier result = array[action];
        return result;
    }
    
//    public EventBarrier getNewRequestEventBarrier() {
//        return myNewRequestEventBarrier;
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
    private RunnableElevator assignElevator(int floor) {
        List<RunnableElevator> elevators = getElevators();
        for (RunnableElevator elevator : elevators) {
            if (elevator.getDirection() == RunnableElevator.DIRECTION_IDLE) {
                elevator.addFloor(floor);
                return elevator;
            }
            if (elevator.getFloor() < floor && elevator.getDirection() == RunnableElevator.DIRECTION_UP) {
                elevator.addFloor(floor);
                return elevator;
            }
        }
        for (RunnableElevator elevator : elevators) {
            if (elevator.getFloor() > floor && elevator.getDirection() == RunnableElevator.DIRECTION_DOWN) {
                elevator.addFloor(floor);
                return elevator;
            }
        }
        Random random = new Random();
        double elId = Math.floor(elevators.size() * random.nextFloat());
        RunnableElevator el = elevators.get((int) elId);
        el.addFloor(floor);
        return el;        
    }

}
