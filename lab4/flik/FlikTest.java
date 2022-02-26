package flik;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlikTest {
    @Test
    public void testFlik() {
        for (int i = 0; i < 130; i++) {
            System.out.println(i);
            assertTrue(Flik.isSameNumber(i, i));
        }
    }
}
