package eu.monnetproject.lemon.tache;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;

public class LemonGFTache {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            printUsage();
        } else {
            final String mustacheFileName = args[0].endsWith(".mustache") ? args[0] : (args[0] + ".mustache");
            final Template mustache = Mustache.compiler().escapeHTML(false).compile(new FileReader(mustacheFileName));
            final String sparqlFileName = args[0].endsWith(".mustache") ? args[0].replaceAll("\\.mustache$", ".sparql") : (args[0] + ".sparql");
            final SPARQLHTTPResolver resolverFactory = new SPARQLHTTPResolver(new URL(args[1]));
            final LemonQuery lemon = new LemonQuery(resolverFactory, new FileReader(sparqlFileName));
            final String gfFileName = args[0].endsWith(".mustache") ? args[0].replaceAll("\\.mustache$", ".gf") : (args[0] + ".gf");
            final PrintWriter out = new PrintWriter(gfFileName);
            mustache.execute(lemon, out);
            out.close();
        }
    }

    public String convert(String mustache, LemonQuery query) {
        final Template tmpl = Mustache.compiler().escapeHTML(false).compile(mustache);
        return tmpl.execute(query).trim();

    }

    private static void printUsage() {
        System.err.println("Usage:\n\tjava -jar lemon.gftache.jar eu.monnetproject.lemon.gftache template.mustache http://sparql.lemon.endpoint/");
    }
}
