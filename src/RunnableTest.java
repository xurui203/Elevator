import java.util.Random;


public class RunnableTest implements Runnable {

    private EventBarrier myBarrier;
    private Thread myThread;
    private int myId;
    
    public RunnableTest(EventBarrier barrier, int id) {
        myBarrier = barrier;
        myId = id;
        myThread = new Thread(this);
        System.out.printf("****RunnableTest: test %d created\n", myId);
    }
    
    public void runThread() {
        myThread.start();
    }
    
    @Override
    public void run() {
        System.out.printf("****RunnableTest: run -- test %d arrived at barrier\n", myId);
        myBarrier.arrive();
        System.out.printf("****RunnableTest: run -- test %d awoke from wait at barrier\n", myId);
        Random random = new Random();
        int sleepTime = random.nextInt(10000);
        System.out.printf("****RunnableTest: run -- test %d sleeping for %d\n", myId, sleepTime);
        try {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.printf("****RunnableTest: run -- test %d slept for %d\n", myId, sleepTime);
        System.out.printf("****RunnableTest: run -- test %d completed at barrier\n", myId);
        myBarrier.complete();        
    }
    
}
