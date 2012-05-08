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
 ********************************************************************************
 */
package eu.monnetproject.lemon.tache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author John McCrae
 */
class LemonQuery extends AbstractMap<String, List<Map<String, Object>>> {

    private static final String END = ">>>";
    private static final String START = "<<<";
    private final ResolverFactory resolverFactory;
    final Map<String, String> queries;
    private Map<String, List<Map<String, Object>>> result = null;

    public LemonQuery(ResolverFactory resolverFactory, Reader querySrc) throws IOException {
        this.resolverFactory = resolverFactory;
        queries = new HashMap<String, String>();
        readQueries(querySrc instanceof BufferedReader ? (BufferedReader) querySrc : new BufferedReader(querySrc));
    }

    public LemonQuery(ResolverFactory resolverFactory, Map<String, String> queries) {
        this.resolverFactory = resolverFactory;
        this.queries = queries;
    }

    private void readQueries(BufferedReader reader) throws IOException {
        String s;
        StringBuffer currentQuery = null;
        String currentQueryName = null;
        while ((s = reader.readLine()) != null) {
            if (currentQuery == null) {
                if (s.matches("\\s*")) {
                    continue;
                } else {
                    if (s.indexOf(START) != -1) {
                        currentQueryName = s.substring(0, s.indexOf(START)).trim();
                        currentQuery = new StringBuffer();
                        final String rest = s.substring(s.indexOf(START) + 3, s.length());
                        if (!rest.matches("\\s*")) {
                            if (rest.indexOf(END) != 1) {
                                queries.put(currentQueryName, rest.substring(0, rest.indexOf(END)));
                                currentQueryName = null;
                                currentQuery = null;
                            } else {
                                currentQuery.append(rest).append("\n");
                            }
                        }
                    }
                }
            } else {
                if (s.indexOf(END) != -1) {
                    currentQuery.append(s.substring(0, s.indexOf(END)));
                    queries.put(currentQueryName, currentQuery.toString());
                    currentQuery = null;
                    currentQueryName = null;
                } else {
                    currentQuery.append(s).append("\n");
                }
            }
        }
    }

    public Map<String, List<Map<String, Object>>> result() {
        if (result == null) {
            resolveResult();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void resolveResult() {
        result = new HashMap<String, List<Map<String, Object>>>();
        for (Map.Entry<String, String> query : queries.entrySet()) {
            final Resolver resolver = resolverFactory.resolver(query.getValue());
            result.put(query.getKey(), (List)resolver.execute());
            final ListIterator<Map<String, Object>> iterator = result.get(query.getKey()).listIterator();
            while(iterator.hasNext()) {
                final Map<String, Object> resolution = iterator.next();
                final HashMap<String, Object> newMap = new HashMap<String,Object>(resolution);
                newMap.put("frag", frag(resolution));  
                iterator.set(newMap);
            }
        }
    }

    @Override
    public Set<Entry<String, List<Map<String, Object>>>> entrySet() {
        return result().entrySet();
    }

    @Override
    public List<Map<String, Object>> get(Object key) {
        return result().get(key);
    }
    
    

    public Map<String, String> frag(final Map<String, Object> resolver) {
        return new FunctionAsMap() {

            public String get(Object key) {
                final String text = resolver.get(key).toString();
                if (text.lastIndexOf("#") > 0) {
                    return text.substring(text.lastIndexOf("#") + 1);
                } else if (text.lastIndexOf("/") > 0) {
                    return text.substring(text.lastIndexOf("/") + 1);
                } else {
                    return text;
                }
            }
        };
    }
}
