/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.lemon.tache;

import eu.monnetproject.lemon.tache.LemonQuery;
import java.io.StringReader;
import junit.framework.TestCase;

/**
 *
 * @author jmccrae
 */
public class LemonQueryTest extends TestCase {
    
    public LemonQueryTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of result method, of class LemonQuery.
     */
    public void testReadQueries() throws Exception {
        System.out.println("readQueries");
        final String s = new String("test<<<query>>>\n\n\ntest2<<<\nquery\n>>>");
        final LemonQuery lemonQuery = new LemonQuery(null, new StringReader(s));
        assertEquals(2,lemonQuery.queries.size());
    }
}
