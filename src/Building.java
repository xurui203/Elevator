import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Building implements IBuilding {

    private int numFloors;
    private int numElevators;
    private int maxCapacity;
    private FileWriter myFileWriter;
    private Map<Integer,EventBarrier[]> myEventBarriers;
    private List<RunnableElevator> myElevators;
    
    /**
     * myEventBarriers maps integers representing floor numbers to EventBarrier arrays containing 3 EventBarriers,
     * 1) going up, 2) going down, 3) exiting at that floor 
     */
    
    public static final int ACTION_UP = 0;
    public static final int ACTION_DOWN = 1;
    public static final int ACTION_EXIT = 2;
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_BUILDING) {
            System.out.printf(format, args);
        }
    }
    
    public void setWriter(FileWriter writer) {
        myFileWriter = writer;        
    }
    
    public Building (FileWriter writer, int numFloors, int numElevators, int maxCapacity) {
        this.numFloors = numFloors;
        this.numElevators = numElevators;
        this.maxCapacity = maxCapacity;
        setWriter(writer);
        myEventBarriers = new HashMap<Integer,EventBarrier[]>();
        myElevators = new ArrayList<RunnableElevator>();
        int currentId = 0;
        for (int i=0; i<numFloors; i++) {
            myEventBarriers.put(i, new EventBarrier[]{new EventBarrier(currentId), new EventBarrier(currentId+1), new EventBarrier(currentId+2)});
            currentId += 3;
        }
        
        for (int i=0; i<numElevators; i++) {
            RunnableElevator el = new RunnableElevator(this, numFloors, i, maxCapacity);
            el.setWriter(myFileWriter);
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
        return el;
        
    }

    @Override
    public RunnableElevator CallDown (int fromFloor) {
        if (fromFloor >= numFloors) {
            return null;
        }
        RunnableElevator el = assignElevator(fromFloor, RunnableElevator.DIRECTION_DOWN);
        print("****Building: CallDown -- assigned elevator %d to calldown from floor %d\n", el.getId(), fromFloor);
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
                return elevator;
            }
        }
        for (RunnableElevator elevator : elevators) {
            if (elevator.getFloor() < floor && elevator.getDirection() == RunnableElevator.DIRECTION_UP) {
                return elevator;
            }
            if (elevator.getFloor() > floor && elevator.getDirection() == RunnableElevator.DIRECTION_DOWN) {
                return elevator;
            }
        }
        Random random = new Random();
        double elId = Math.floor(elevators.size() * random.nextFloat());
        RunnableElevator el = elevators.get((int) elId);
        return el;        
    }

}
