public class EventBarrier implements AbstractEventBarrier {
    private volatile boolean hasEvent;
    private volatile int currentWorkers;

    public EventBarrier () {
        hasEvent = false;
        currentWorkers = 0;
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
    public void complete () {
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
