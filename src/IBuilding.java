
public interface IBuilding {	
	
	/**
	 * Other variables/data structures as needed goes here 
	 */

	/**
	 * Elevator rider interface (part 2): invoked by rider threads.
 	 */

	/* Signal the elevator that we want to go up */
	public abstract RunnableElevator CallUp(int fromFloor);

	/* Signal the elevator that we want to go down */
	public abstract RunnableElevator CallDown(int fromFloor); 

	/* Other methods as needed goes here */
}
