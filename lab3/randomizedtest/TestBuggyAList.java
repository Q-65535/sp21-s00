package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> list = new AListNoResizing<>();
        BuggyAList<Integer> bList = new BuggyAList<>();

        for (int i = 0; i < 3; i++) {
            list.addLast(i);
            bList.addLast(i);
        }

        assertEquals(list.size(), bList.size());

        for (int i = 0; i < 3; i++) {
            assertEquals(list.removeLast(), bList.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyL = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggyL.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int buggySize = buggyL.size();
                assertEquals(size, buggySize);
            } else if (operationNumber == 2) {
                //removeLast
                if (L.size() == 0) continue;
                int removedItem = L.removeLast();
                int buggyRemovedItem = buggyL.removeLast();
                assertEquals(removedItem, buggyRemovedItem);
            } else if (operationNumber == 3) {
                if (L.size() == 0) continue;
                int lastItem = L.getLast();
                int buggyLastItem = buggyL.getLast();
                assertEquals(lastItem, buggyLastItem);
            }
        }
    }
}
