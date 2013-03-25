import java.util.PriorityQueue;


public class ElevatorCall implements Comparable<ElevatorCall> {

    private int myFloor;
    private int myDirection;
    
    public ElevatorCall(int floor, int direction) {
        myFloor = floor;
        myDirection = direction;
    }
    
    public int getFloor() {
        return myFloor;
    }
    
    public int getDirection() {
        return myDirection;       
    }
    
    public int compareTo(ElevatorCall other) {
        int floorDiff = this.myFloor - other.getFloor();
        if (floorDiff != 0) {
            return floorDiff;
        } else {
            int dirDiff = this.myDirection - other.getDirection();
            return dirDiff;
        }
    }
    
    public boolean equals(Object o) {
        ElevatorCall other;
        if (o instanceof ElevatorCall) {
            other = (ElevatorCall) o;
        } else {
            return false;
        }
        boolean result = (myFloor == other.getFloor() && myDirection == other.getDirection());
        return result;
    }
    
    public int hashCode() {
        return myFloor + (myDirection + 1) * 1000;
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(myFloor);
        if (myDirection == RunnableElevator.DIRECTION_DOWN) {
            result.append("D");
        } else if (myDirection == RunnableElevator.DIRECTION_UP){
            result.append("U");
        } else {
            result.append("I");
        }
        return result.toString();
    }
    
}
