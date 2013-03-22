public class EventBarrier implements AbstractEventBarrier {
    private volatile boolean hasEvent;
    private volatile int currentWorkers;
    private int maxWorkers;

    public EventBarrier (int num) {
        hasEvent = false;
        maxWorkers = num;
        currentWorkers = 0;
    }

    @Override
    public synchronized void arrive () {
        if (hasEvent) {
            // no need to wait just continue
            // numWaiters ++ or numWaiers--
            currentWorkers++;
            return;
        }
        else {
            // numWaiers ++ or numWaiters--
            // wait
            // while(!_isSignaled)
            // this.wait();
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
        // notifyAll - when you have a consumer/producer trying to depend on your event
        // wait
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
    public void complete () {
        // notify
        // wait
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

    @Override
    public int waiters () {
        return currentWorkers;
    }

}
