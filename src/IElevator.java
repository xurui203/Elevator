
public interface IElevator {	

	/**
	 * Elevator control interface: invoked by Elevator thread.
 	 */

	/* Signal incoming and outgoing riders */
	public abstract void OpenDoors(); 	//raise

	/**
	 * When capacity is reached or the outgoing riders are exited and
	 * incoming riders are in. 
 	 */

	public abstract void CloseDoors();

	/* Go to a requested floor */
	public abstract void VisitFloor();


	/**
	 * Elevator rider interface (part 1): invoked by rider threads. 
  	 */

	/* Enter the elevator */
	public abstract boolean Enter(); //complete//3 event barriers per floor, one for exiting, one for going down, one for going up
	
	/* Exit the elevator */
	public abstract void Exit(); //complete

	/* Request a destination floor once you enter */
 	public abstract void RequestFloor(int floor, int direction);
	
	/* Other methods as needed goes here */
}
