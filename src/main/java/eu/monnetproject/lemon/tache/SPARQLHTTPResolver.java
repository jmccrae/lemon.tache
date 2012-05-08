/**
 * ********************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.lemon.tache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author John McCrae
 */
public class SPARQLHTTPResolver implements ResolverFactory {

    private final URL endpoint;

    public SPARQLHTTPResolver(URL endpoint) {
        this.endpoint = endpoint;
    }

    public Resolver resolver(String query) {
        return new SPARQLHTTPResolverFactory(query);
    }

    private class SPARQLHTTPResolverFactory implements Resolver {

        private final String query;

        public SPARQLHTTPResolverFactory(String query) {
            this.query = query;
        }

        public List<Map<String, String>> execute() {
            final LinkedList<Map<String, String>> results = new LinkedList<Map<String,String>>();
            try {
                final URL queryURL = new URL(endpoint + "?query=" + URLEncoder.encode(query.toString(), "UTF-8"));
                final URLConnection connection = queryURL.openConnection();
                connection.setRequestProperty("Accept", "application/sparql-results+xml");
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final InputStream in = connection.getInputStream();
                final Document document = db.parse(in);
                in.close();
                final NodeList resultsTags = document.getElementsByTagName("result");
                for (int i = 0; i < resultsTags.getLength(); i++) {
                    final HashMap<String, String> result = new HashMap<String,String>();
                    final Node node = resultsTags.item(i);
                    if (node instanceof Element) {
                        final Element element = (Element) node;
                        final NodeList resultTags = element.getElementsByTagName("binding");
                        for (int j = 0; j < resultTags.getLength(); j++) {
                            final Element resultElem = (Element) resultTags.item(j);
                            final String varName = resultElem.getAttribute("name");
                            result.put(varName, readResult(resultElem));
                        }
                    }
                    results.add(result);
                }
                return results;
            } catch (IOException x) {
                throw new ResolverException(x);
            } catch (SAXException x) {
                throw new ResolverException(x);
            } catch (ParserConfigurationException x) {
                throw new ResolverException(x);
            }

        }
    }
    
    private String readResult(Element resultTag) {
        final NodeList childNodes = resultTag.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child instanceof Element) {
                final Element c = (Element) child;
                if (c.getTagName().equals("uri")) {
                        return c.getTextContent();
                } else if (c.getTagName().equals("literal")) {
                        return c.getTextContent();
                } else if (c.getTagName().equals("bnode")) {
                    if (c.getTextContent() != null && c.getTextContent().startsWith("nodeID://")) {
                        // Virtuoso does this for some reason
                        return "_:" + c.getTextContent().substring(9, c.getTextContent().length());
                    } else {
                        return "_:" + c.getTextContent();
                    }
                } else {
                    throw new ResolverException("Unexpected tag in binding " + c);
                }
            }
        }
        throw new ResolverException("No tag in result set");
    }
}
