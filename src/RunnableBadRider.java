import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RunnableBadRider extends RunnableRider implements Runnable {

    private Building myBuilding;
    private RunnableElevator myElevator;
    private int myFrom;
    private int myTo;
    private int myId;
    private int myType;
    
    private FileWriter myFileWriter;
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_RIDER) {
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
    
    public RunnableBadRider(Building building, int from, int to, int id) {
        Random rand = new Random();
        myType = rand.nextInt(3);
        myType = 0;
        String typeDescrip;
        if (myType == 0) {
            typeDescrip = "doesn't get on elevator";
        } else if (myType == 1) {
            typeDescrip = "doesn't request a floor";
        } else {
            typeDescrip = "doesn't get off elevator";
        }
        myBuilding = building;
        myElevator = null;
        myFrom = from;
        myTo = to;
        myId = id;        
        print("******RunnableBadRider: rider %d created, traveling from floor %d to floor %d, type is %s\n",
              myId, myFrom, myTo, typeDescrip);
    }
    
    public void runThread() {
        Thread thread = new Thread(this);
        thread.start();
    }    
    
    public List<Integer> getDetails() {
        List<Integer> result = new ArrayList<Integer>();
        result.add(myId);
        result.add(myFrom);
        result.add(myTo);
        return result;
    }
    
    @Override
    public void run () {
        
        // establish direction of movement and make the call
        int action;
        int direction;
        if (myFrom > myTo) {
            action = Building.ACTION_DOWN;
        } else if (myFrom < myTo) {
            action = Building.ACTION_UP;
        } else {
            print("******RunnableBadRider: run -- rider %d requested same floor %d, exiting thread\n", myId, myFrom);
            return;
        }
        EventBarrier fromBarrier = myBuilding.getBarrierForFloorAndAction(myFrom, action);
        EventBarrier toBarrier = myBuilding.getBarrierForFloorAndAction(myTo, Building.ACTION_EXIT);
        
        // get the elevator that will be assigned to our request
        if (action == Building.ACTION_DOWN) {
            write("R" + myId + " pushes D" + myFrom + "\n");
            print("******RunnableBadRider: run -- rider %d requesting a calldown from floor %d\n", myId, myFrom);
            myElevator = myBuilding.CallDown(myFrom);
            direction = RunnableElevator.DIRECTION_DOWN;
        } else {
            write("R" + myId + " pushes U" + myFrom + "\n");
            print("******RunnableBadRider: run -- rider %d requesting a callup from floor %d\n", myId, myFrom);
            myElevator = myBuilding.CallUp(myFrom);
            direction = RunnableElevator.DIRECTION_UP;
        }
            
        /**
         * Synchronize this block to make sure passengers arrive at the appropriate enter barrier immediately after
         * requesting a call.
         */        
        synchronized (myElevator) {
            
            // signal arrival at the elevator
            print("******RunnableBadRider: run -- rider %d signaling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
            fromBarrier.signalArrival();
            
            myElevator.addFloor(myFrom, direction);
            
//            print("******RunnableBadRider: run -- rider %d notifying elevator %d to wake up\n", myId, myElevator.getId());            
            myElevator.notifyAll();
            
        }
         
        // actually arrive at the elevator, unless type is doesn't get on elevator
        if (myType == 0) {
            print("******RunnableBadRider: run -- rider %d terminating without arriving at fromBarrier %d\n", myId, fromBarrier.getId());
            return;
        }
        
        print("******RunnableBadRider: run -- rider %d fulfilling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
        fromBarrier.fulfillArrival();              
        print("******RunnableBadRider: run -- rider %d awoke at fromBarrier %d\n", myId, fromBarrier.getId());
            
        // after waking from enter arrive, attempt to enter the elevator        
        print("******RunnableBadRider: run -- rider %d entering elevator %d\n", myId, myElevator.getId());
        
        while (!myElevator.Enter()) {

            fromBarrier.complete();            
            
            print("******RunnableBadRider: run -- rider %d failed to enter elevator %d, reissuing request\n", myId, myElevator.getId());
            
            // redo our request and get the elevator
            if (action == Building.ACTION_DOWN) {
                print("******RunnableBadRider: run -- rider %d requesting a calldown from floor %d\n", myId, myFrom);
                myElevator = myBuilding.CallDown(myFrom);
                direction = RunnableElevator.DIRECTION_DOWN;
            } else {
                print("******RunnableBadRider: run -- rider %d requesting a callup from floor %d\n", myId, myFrom);
                myElevator = myBuilding.CallUp(myFrom);
                direction = RunnableElevator.DIRECTION_UP;
            }
            
            synchronized (myElevator) {                
                // signal arrival at the elevator
                print("******RunnableBadRider: run -- rider %d signaling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
                fromBarrier.signalArrival();                
                myElevator.addFloor(myFrom, direction);                
//                print("******RunnableBadRider: run -- rider %d notifying elevator %d to wake up\n", myId, myElevator.getId());            
                myElevator.notifyAll();                
            }
             
            // actually arrive at the elevator
            print("******RunnableBadRider: run -- rider %d fulfilling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
            fromBarrier.fulfillArrival();
            
        }
        
        write("R" + myId + " enters E" + myElevator.getId() + " on F" + myFrom + "\n");
        
        /**
         * Synchronize this block to make sure riders arrive at the appropriate exit barrier immediately after
         * requesting a floor.
         */
        synchronized (myElevator) {
            
            // request a floor, unless type is doesn't request a floor
            if (myType == 1) {
                print("******RunnableBadRider: run -- rider %d terminating without requesting a floor for elevator %d\n",
                      myId, myElevator.getId());
                return;
            }
            write("R" + myId + " pushes E" + myElevator.getId() + "B" + myTo + "\n");
            print("******RunnableBadRider: run -- rider %d in elevator %d requesting floor %d\n", myId, myElevator.getId(), myTo);
            myElevator.RequestFloor(myTo, direction);
            
            // signal arrival at exit floor event barrier
            print("******RunnableBadRider: run -- rider %d signaling arrival at toBarrier %d\n", myId, toBarrier.getId());
            toBarrier.signalArrival();            
            
        }
        
        // complete the enter barrier action
        print("******RunnableBadRider: run -- rider %d completing at fromBarrier %d\n", myId, fromBarrier.getId());
        fromBarrier.complete();
        
        // actually arrive at the exit floor event barrier
        print("******RunnableBadRider: run -- rider %d fulfilling arrival at toBarrier %d\n", myId, toBarrier.getId());
        toBarrier.fulfillArrival();
        
        // don't get off the elevator
        print("******RunnableBadRider: run -- rider %d terminating without exiting elevator %d at toBarrier %d\n", 
              myId, myElevator.getId(), toBarrier.getId());
    }

}
