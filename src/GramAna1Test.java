import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GramAna1Test {

    GramAna1 gramAna1;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void test(){
        Assert.assertTrue("double[32]".matches("^double\\[(\\d+)]*"));
    }

}