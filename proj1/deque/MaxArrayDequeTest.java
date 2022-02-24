package deque;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;

public class MaxArrayDequeTest {
    @Test
    public void testComparator() {
        MaxArrayDeque<String> mad = new MaxArrayDeque<>((s1,  s2) -> s1.compareTo(s2));
        mad.addFirst("abcdef");
        mad.addLast("bcdef");
        mad.addFirst("qwer");
        mad.addFirst(new String("qwer"));
        mad.addFirst("CSAPP is one of my favourite!");
        assertEquals(mad.max(), "qwer");

        // compare two strings according to their lengths
        String max1 = mad.max((s1, s2) -> s1.length() - s2.length());
        assertEquals(max1, "CSAPP is one of my favourite!");
    }
}
