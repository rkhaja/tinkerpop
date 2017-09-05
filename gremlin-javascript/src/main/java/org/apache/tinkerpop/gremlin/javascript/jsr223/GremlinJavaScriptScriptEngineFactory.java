/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.javascript.jsr223;

import org.apache.tinkerpop.gremlin.jsr223.AbstractGremlinScriptEngineFactory;
import org.apache.tinkerpop.gremlin.jsr223.Customizer;
import org.apache.tinkerpop.gremlin.jsr223.GremlinScriptEngine;
import org.apache.tinkerpop.gremlin.jsr223.GremlinScriptEngineFactory;

import java.util.Collections;
import java.util.List;

/**
 * A {@link GremlinScriptEngineFactory} implementation that creates {@link GremlinJavaScriptScriptEngine} instances.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinJavaScriptScriptEngineFactory extends AbstractGremlinScriptEngineFactory {
    private static final String ENGINE_NAME = "gremlin-javascript";
    private static final String LANGUAGE_NAME = "gremlin-javascript";
    private static final String PLAIN = "plain";
    private static final List<String> EXTENSIONS = Collections.singletonList("js");

    public GremlinJavaScriptScriptEngineFactory() {
        super(ENGINE_NAME, LANGUAGE_NAME, EXTENSIONS, Collections.singletonList(PLAIN));
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String m, final String... args) {
        return null;
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return "println " + toDisplay;
    }

    @Override
    public GremlinScriptEngine getScriptEngine() {
        final List<Customizer> customizers =  manager.getCustomizers(ENGINE_NAME);
        return (customizers.isEmpty()) ? new GremlinJavaScriptScriptEngine() :
                new GremlinJavaScriptScriptEngine(customizers.toArray(new Customizer[customizers.size()]));
    }
}
