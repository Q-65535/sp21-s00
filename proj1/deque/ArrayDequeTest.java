package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void testArrayDequeAddFirst() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        LinkedListDeque<Integer> ld = new LinkedListDeque<>();

        int N = 50000000;
        for (int i = 0; i < N; i++) {
            int caseIndicator = StdRandom.uniform(0, 6);

            switch (caseIndicator) {
                case 0: // test addFirst
                    int randVal = StdRandom.uniform(0, 5000);
                    ad.addFirst(randVal);
                    ld.addFirst(randVal);
                    assertEquals(ad, ld);
                case 1: // test addLast
                    int randVal1 = StdRandom.uniform(0, 5000);
                    ad.addLast(randVal1);
                    ld.addLast(randVal1);
                    assertEquals(ad, ld);
                case 2: // test removeLast
                    Integer arrayLastVal = ad.removeLast();
                    Integer linkLastVal = ld.removeLast();
                    assertEquals(arrayLastVal, linkLastVal);
                    assertEquals(ad, ld);
                case 3: // test removeFirst
                    Integer arrayFirstVal = ad.removeFirst();
                    Integer linkFirstVal = ld.removeFirst();
                    assertEquals(arrayFirstVal, linkFirstVal);
                    assertEquals(ad, ld);
                case 4: // test get
                    int rdIndex = StdRandom.uniform(0, ad.size() + 10);
                    Integer arrayGetVal = ad.get(rdIndex);
                    Integer linkGetVal = ld.get(rdIndex);
                    assertEquals(arrayGetVal, linkGetVal);
                    assertEquals(ad, ld);
                case 5: // test size
                    assertTrue(ad.size() == ld.size());
                    assertEquals(ad, ld);
            }
        }
    }
}
