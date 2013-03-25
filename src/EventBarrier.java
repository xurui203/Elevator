public class EventBarrier implements IEventBarrier {
    private volatile boolean hasEvent;
    private volatile int currentWorkers;
    private int myId;

    public void print(String format, Object... args) {
        if (ElevatorConstants.PRINT_BARRIER) {
            System.out.printf(format, args);
        }
    }
    
    public EventBarrier (int id) {
        hasEvent = false;
        currentWorkers = 0;
        myId = id;
    }

    @Override
    public synchronized void arrive () {
        if (hasEvent) {
            currentWorkers++;
            return;
        }
        else {
            currentWorkers++;
            while (!hasEvent) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }

    @Override
    public synchronized void raise () {
        hasEvent = true;
        this.notifyAll();
        while (currentWorkers > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                continue;
            }
        }
        hasEvent = false;

    }

    @Override
    public synchronized void complete () {
        currentWorkers--;
        this.notifyAll();
        while (currentWorkers > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                continue;
            }
        }
    }
    
    /**
     * Used to signal an impending arrival without blocking.
     */
    public synchronized void signalArrival () {
        currentWorkers++;
    }
    
    /**
     * Actually arrive and block until signaled. Must first call signalArrival before fulfillArrival
     * to ensure integrity of waiter count.
     */
    public synchronized void fulfillArrival () {
        if (hasEvent) {
            return;
        }
        else {
            while (!hasEvent) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }

    @Override
    public int waiters () {
        return currentWorkers;
    }
    
    public int getId() {
        return myId;
    }

}
