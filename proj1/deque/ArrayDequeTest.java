package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void testArrayDequeAddFirst() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        LinkedListDeque<Integer> ld = new LinkedListDeque<>();

        int N = 99999;
        for (int i = 0; i < N; i++) {
            int caseIndicator = StdRandom.uniform(0, 6);

            switch (caseIndicator) {
                case 0: // test addFirst
                    int randVal = StdRandom.uniform(0, 5000);
                    ad.addFirst(randVal);
                    ld.addFirst(randVal);
                    assertEquals(ad, ld);
                    break;
                case 1: // test addLast
                    int randVal1 = StdRandom.uniform(0, 5000);
                    ad.addLast(randVal1);
                    ld.addLast(randVal1);
                    assertEquals(ad, ld);
                    break;
                case 2: // test removeLast
                    Integer arrayLastVal = ad.removeLast();
                    Integer linkLastVal = ld.removeLast();
                    if (arrayLastVal == null || linkLastVal == null) {
                        break;
                    }
                    assertEquals(arrayLastVal, linkLastVal);
                    assertEquals(ad, ld);
                    break;
                case 3: // test removeFirst
                    Integer arrayFirstVal = ad.removeFirst();
                    Integer linkFirstVal = ld.removeFirst();
                    if (arrayFirstVal == null || linkFirstVal == null) {
                        break;
                    }
                    assertEquals(arrayFirstVal, linkFirstVal);
                    assertEquals(ad, ld);
                    break;
                case 4: // test get
                    int rdIndex = StdRandom.uniform(0, ad.size() + 10);
                    Integer arrayGetVal = ad.get(rdIndex);
                    Integer linkGetVal = ld.get(rdIndex);
                    if (arrayGetVal == null || linkGetVal == null) {
                        break;
                    }
                    assertEquals(arrayGetVal, linkGetVal);
                    assertEquals(ad, ld);
                    break;
                case 5: // test size
                    assertTrue(ad.size() == ld.size());
                    assertEquals(ad, ld);
                    break;
                default:
                    continue;
            }
        }
    }
}
