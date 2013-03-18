
public class EventBarrier extends AbstractEventBarrier{
	private volatile boolean _isSignaled;
	private int _numWorkers;
	
	public EventBarrier(int numWorkers) {
		super(numWorkers);
		_isSignaled = false;
		_numWorkers = numWorkers;
	}

	@Override
	public synchronized void arrive() throws InterruptedException{
		if (_isSignaled){
			//no need to wait just continue
			//numWaiters ++ or numWaiers--
		}
		else{
			//numWaiers ++ or numWaiters--
			//wait
			//while(!_isSignaled)
			//this.wait();
		}
	}

	@Override
	public synchronized void raise() throws InterruptedException {
		//notifyAll - when you have a consumer/producer trying to depend on your event
		//wait
		
	}

	@Override
	public void complete() {
		//notify
		//wait
	}

	@Override
	public int waiters() {
		return _numWorkers;
	}

}
