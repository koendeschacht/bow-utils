package be.bagofwords.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by koen on 14.01.17.
 */

public class TimeUtilsTest {

    @Test
    public void testShortJob() {
        TimeUtils.TimedResult<Integer> result = TimeUtils.limitInTime(1000, () -> 1 + 1);
        Assert.assertTrue(result.finishedInTime);
        Assert.assertNotNull(result.result);
        Assert.assertEquals(2, (int) result.result);
    }

    @Test
    public void testTooLongJob() {
        TimeUtils.TimedResult<Double> result = TimeUtils.limitInTime(1000, () -> {
            double sum = 0;
            for (long i = 0; i < Long.MAX_VALUE; i++) {
                sum += i;
            }
            return sum;
        });
        Assert.assertFalse(result.finishedInTime);
        Assert.assertNull(result.result);
    }

}