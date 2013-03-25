import java.util.ArrayList;
import java.util.List;


public class EventBarrierTest {

    public static void main(String[] args) {
        EventBarrier barrier = new EventBarrier(0);
        List<RunnableTest> myTests = new ArrayList<RunnableTest>();
        int numTest = 10;
        for (int i=0; i<numTest; i++) {
            RunnableTest test = new RunnableTest(barrier, i);
            test.runThread();
        }
        System.out.printf("****EventBarrierTest: main -- raising at barrier\n");
        barrier.raise();
        System.out.printf("****EventBarrierTest: main -- all tests completed at barrier\n");        
    }
    
}
