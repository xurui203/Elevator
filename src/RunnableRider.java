import java.util.ArrayList;
import java.util.List;


public class RunnableRider implements Runnable {

    private Building myBuilding;
    private RunnableElevator myElevator;
//    private Thread myThread;
    private int myFrom;
    private int myTo;
    private int myId;
    
    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_RIDER) {
            System.out.printf(format, args);
        }
    }
    
    public RunnableRider(Building building, int from, int to, int id) {
        myBuilding = building;
        myElevator = null;
        myFrom = from;
        myTo = to;
        myId = id;
        print("******RunnableRider: rider %d created, traveling from floor %d to floor %d\n", myId, myFrom, myTo);
//        myThread = new Thread(this);
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
            print("******RunnableRider: run -- rider %d requested same floor %d, exiting thread\n", myId, myFrom);
            return;
        }
        EventBarrier fromBarrier = myBuilding.getBarrierForFloorAndAction(myFrom, action);
        EventBarrier toBarrier = myBuilding.getBarrierForFloorAndAction(myTo, Building.ACTION_EXIT);
        
        // get the elevator that will be assigned to our request
        if (action == Building.ACTION_DOWN) {
            print("******RunnableRider: run -- rider %d requesting a calldown from floor %d\n", myId, myFrom);
            myElevator = myBuilding.CallDown(myFrom);
            direction = RunnableElevator.DIRECTION_DOWN;
        } else {
            print("******RunnableRider: run -- rider %d requesting a callup from floor %d\n", myId, myFrom);
            myElevator = myBuilding.CallUp(myFrom);
            direction = RunnableElevator.DIRECTION_UP;
        }
            
        /**
         * Synchronize this block to make sure passengers arrive at the appropriate enter barrier immediately after
         * requesting a call.
         */        
        synchronized (myElevator) {
            
            // signal arrival at the elevator
//            print("******RunnableRider: run -- rider %d signaling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
            fromBarrier.signalArrival();
            
            myElevator.addFloor(myFrom, direction);
            
//            print("******RunnableRider: run -- rider %d notifying elevator %d to wake up\n", myId, myElevator.getId());            
            myElevator.notifyAll();
            
        }
         
        // actually arrive at the elevator
//        print("******RunnableRider: run -- rider %d fulfilling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
        fromBarrier.fulfillArrival();            
        
        
//        print("******RunnableRider: run -- rider %d awoke at fromBarrier %d\n", myId, fromBarrier.getId());
            
        // after waking from enter arrive, attempt to enter the elevator        
        print("******RunnableRider: run -- rider %d entering elevator %d\n", myId, myElevator.getId());
        
        while (!myElevator.Enter()) {

            fromBarrier.complete();            
            
            print("******RunnableRider: run -- rider %d failed to enter elevator %d, reissuing request\n", myId, myElevator.getId());
            
            // redo our request and get the elevator
            if (action == Building.ACTION_DOWN) {
                print("******RunnableRider: run -- rider %d requesting a calldown from floor %d\n", myId, myFrom);
                myElevator = myBuilding.CallDown(myFrom);
                direction = RunnableElevator.DIRECTION_DOWN;
            } else {
                print("******RunnableRider: run -- rider %d requesting a callup from floor %d\n", myId, myFrom);
                myElevator = myBuilding.CallUp(myFrom);
                direction = RunnableElevator.DIRECTION_UP;
            }
            
            synchronized (myElevator) {                
                // signal arrival at the elevator
//                print("******RunnableRider: run -- rider %d signaling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
                fromBarrier.signalArrival();                
                myElevator.addFloor(myFrom, direction);                
//                print("******RunnableRider: run -- rider %d notifying elevator %d to wake up\n", myId, myElevator.getId());            
                myElevator.notifyAll();                
            }
             
            // actually arrive at the elevator
//            print("******RunnableRider: run -- rider %d fulfilling arrival at fromBarrier %d\n", myId, fromBarrier.getId());
            fromBarrier.fulfillArrival();
            
        }
        
        /**
         * Synchronize this block to make sure riders arrive at the appropriate exit barrier immediately after
         * requesting a floor.
         */
        synchronized (myElevator) {
            
            // request a floor
            print("******RunnableRider: run -- rider %d in elevator %d requesting floor %d\n", myId, myElevator.getId(), myTo);
            myElevator.RequestFloor(myTo, direction);
            
            // signal arrival at exit floor event barrier
//            print("******RunnableRider: run -- rider %d signaling arrival at toBarrier %d\n", myId, toBarrier.getId());
            toBarrier.signalArrival();            
            
        }
        
        // complete the enter barrier action
//        print("******RunnableRider: run -- rider %d completing at fromBarrier %d\n", myId, fromBarrier.getId());
        fromBarrier.complete();
        
        // actually arrive at the exit floor event barrier
//        print("******RunnableRider: run -- rider %d fulfilling arrival at toBarrier %d\n", myId, toBarrier.getId());
        toBarrier.fulfillArrival();
        
        /**
         * Synchronize this block to make sure riders complete at the event barrier immediately after exiting.
         */
        synchronized (myElevator) {
        
            // after waking from exit arrive, exit the elevator
//            print("******RunnableRider: run -- rider %d awoke at toBarrier %d\n", myId, toBarrier.getId());
            print("******RunnableRider: run -- rider %d exiting elevator %d at floor %d\n", myId, 
                              myElevator.getId(), myElevator.getFloor());
            myElevator.Exit();                    
        
        }
        
        // complete the exit barrier action
//        print("******RunnableRider: run -- rider %d completing at toBarrier %d\n", myId, toBarrier.getId());
        toBarrier.complete();
        
    }

}
