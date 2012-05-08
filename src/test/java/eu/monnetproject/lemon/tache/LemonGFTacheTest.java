/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.monnetproject.lemon.tache;

import eu.monnetproject.lemon.tache.LemonQuery;
import eu.monnetproject.lemon.tache.LemonGFTache;
import eu.monnetproject.lemon.tache.ResolverFactory;
import eu.monnetproject.lemon.tache.Resolver;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author jmccrae
 */
@SuppressWarnings("unchecked")
public class LemonGFTacheTest extends TestCase {

    public LemonGFTacheTest(String testName) {
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
    private static final String QUERY1 = "prefix lemon: <http://www.monnet-project.eu/lemon#>  \n"
            + "select ?form where { \n"
            + "?entry a lemon:LexicalEntry ;\n"
            + " lemon:canonicalForm ?f . \n"
            + "?f lemon:writtenRep ?form }";
    private static final String MUSTACHE1 = "{{#entries}}\n"
            + " lin {{form}}_N = mkN \"{{form}}\"\n"
            + "{{/entries}}";
    private static final String EXPECTED1 = "lin cat_N = mkN \"cat\"";
    private static final String QUERY2 = "prefix lemon: <http://www.monnet-project.eu/lemon#>  \n"
            + "select ?form ?entry where { \n"
            + "?entry a lemon:LexicalEntry ;\n"
            + " lemon:canonicalForm ?f . \n"
            + "?f lemon:writtenRep ?form }";
    private static final String MUSTACHE2 = "{{#entries}}\n"
            + " lin {{#frag}}{{entry}}{{/frag}}_N = mkN \"{{form}}\"\n"
            + "{{/entries}}";
    private static final String EXPECTED2 = EXPECTED1;
    private static final HashMap<String, List<Map<String, String>>> results = new HashMap<String, List<Map<String, String>>>();

    static {
        results.put(QUERY1, Arrays.asList(Collections.singletonMap("form", "cat")));
        results.put(QUERY2, Arrays.asList((Map<String, String>) new HashMap<String, String>()));
        results.get(QUERY2).get(0).put("form", "cat");
        results.get(QUERY2).get(0).put("entry", "http://www.example.com/lexicon#cat");
    }

    /**
     * Test of convert method, of class LemonGFTache.
     */
    public void testConvert() throws Exception {
        System.out.println("convert");
        final LemonGFTache tache = new LemonGFTache();
        final String result = tache.convert(MUSTACHE1, new LemonQuery(new DummyResolverFactory(), Collections.singletonMap("entries", QUERY1)));
        assertEquals(EXPECTED1, result);
    }

    public void testConvert2() throws Exception {
        System.out.println("convert2");
        final LemonGFTache tache = new LemonGFTache();
        final LemonQuery query = new LemonQuery(new DummyResolverFactory(), Collections.singletonMap("entries", QUERY2));
        final String result = tache.convert(MUSTACHE2, query);
        assertEquals(EXPECTED2, result);
    }

    private static class DummyResolverFactory implements ResolverFactory {

        private static class DummyResolver implements Resolver {

            private final List<Map<String, String>> result;

            public DummyResolver(List<Map<String, String>> result) {
                this.result = result;
            }

            public List<Map<String, String>> execute() {
                return result;
            }
        }

        public Resolver resolver(String query) {
            return new DummyResolver(results.get(query));
        }
    }
}
