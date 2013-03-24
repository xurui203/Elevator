
public class RunnableRider implements Runnable {

    private Building myBuilding;
    private RunnableElevator myElevator;
    private int myFrom;
    private int myTo;
    
    public RunnableRider(Building building, int from, int to) {
        myBuilding = building;
        myElevator = null;
        myFrom = from;
        myTo = to;
    }
    
    @Override
    public void run () {
        
        // establish direction of movement and make the call
        int direction;
        if (myFrom > myTo) {
            direction = Building.ACTION_DOWN;
            myElevator = myBuilding.CallDown(myFrom);
        } else if (myFrom < myTo) {
            direction = Building.ACTION_UP;
            myElevator = myBuilding.CallUp(myFrom);
        } else {
            return;
        }
        
        // arrive at the elevator
        EventBarrier fromBarrier = myBuilding.getBarrierForFloorAndAction(myFrom, direction);
        fromBarrier.arrive();        
        
        // after waking from enter arrive, enter the elevator and request a floor
        myElevator.Enter();
        myElevator.RequestFloor(myTo);
        
        // complete the enter barrier action
        fromBarrier.complete();
        
        // arrive at the exit floor event barrier
        EventBarrier toBarrier = myBuilding.getBarrierForFloorAndAction(myTo, Building.ACTION_EXIT);
        toBarrier.arrive();
        
        // after waking from exit arrive, exit the elevator
        myElevator.Exit();
    }

}
