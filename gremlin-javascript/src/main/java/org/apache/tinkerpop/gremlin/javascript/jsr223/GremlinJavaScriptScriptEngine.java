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

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.tinkerpop.gremlin.jsr223.CoreGremlinPlugin;
import org.apache.tinkerpop.gremlin.jsr223.Customizer;
import org.apache.tinkerpop.gremlin.jsr223.GremlinScriptEngine;
import org.apache.tinkerpop.gremlin.jsr223.GremlinScriptEngineFactory;
import org.apache.tinkerpop.gremlin.jsr223.ImportCustomizer;
import org.apache.tinkerpop.gremlin.process.traversal.Bytecode;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinJavaScriptScriptEngine implements GremlinScriptEngine {

    private final NashornScriptEngine scriptEngine;

    public GremlinJavaScriptScriptEngine(final Customizer... customizers) {
        this.scriptEngine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
        final List<Customizer> listOfCustomizers = new ArrayList<>(Arrays.asList(customizers));

        // always need this plugin for a scriptengine to be "Gremlin-enabled"
        CoreGremlinPlugin.instance().getCustomizers("gremlin-javascript").ifPresent(c -> listOfCustomizers.addAll(Arrays.asList(c)));

        final List<ImportCustomizer> importCustomizers = listOfCustomizers.stream()
                .filter(p -> p instanceof ImportCustomizer)
                .map(p -> (ImportCustomizer) p)
                .collect(Collectors.toList());

        try {
            for (ImportCustomizer ic : importCustomizers) {
                for (Class<?> c : ic.getClassImports()) {
                    if (null == c.getDeclaringClass())
                        this.scriptEngine.eval(c.getSimpleName() + " = Java.type(\"" + c.getName() + "\")");
                    else
                        this.scriptEngine.eval(c.getSimpleName() + " = Java.type(\"" + c.getDeclaringClass().getName() + "\")");
                }

                for (Method m : ic.getMethodImports()) {
                    this.scriptEngine.eval(SymbolHelper.toJavaScript(m.getName()) + " = Java.type(\"" + m.getDeclaringClass().getName() + "\")." + m.getName());
                }

                // enums need to import after methods for some reason or else label comes in as a PyReflectedFunction
                for (Enum e : ic.getEnumImports()) {
                    this.scriptEngine.eval(SymbolHelper.toJavaScript(e.name()) + " = Java.type(\"" + e.getDeclaringClass().getName() + "\")." + e.name());
                }
            }

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Traversal.Admin eval(final Bytecode bytecode, final Bindings bindings) throws ScriptException {
        bindings.putAll(bytecode.getBindings());
        String traversalSource = "g";
        for (final Map.Entry<String, Object> entry : bindings.entrySet()) {
            if (entry.getValue() instanceof TraversalSource) {
                traversalSource = entry.getKey();
                break;
            }
        }
        return (Traversal.Admin) this.eval(JavaScriptTranslator.of(traversalSource).translate(bytecode), bindings);
    }

    @Override
    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        return this.scriptEngine.eval(script, context);
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        return this.scriptEngine.eval(reader, context);
    }

    @Override
    public Object eval(final String script) throws ScriptException {
        return this.scriptEngine.eval(script);
    }

    @Override
    public Object eval(final Reader reader) throws ScriptException {
        return this.scriptEngine.eval(reader);
    }

    @Override
    public Object eval(final String script, final Bindings n) throws ScriptException {
        this.scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).putAll(n); // TODO: groovy and jython act different
        return this.scriptEngine.eval(script);
    }

    @Override
    public Object eval(final Reader reader, final Bindings n) throws ScriptException {
        this.scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).putAll(n); // TODO: groovy and jython act different
        return this.scriptEngine.eval(reader);
    }

    @Override
    public void put(final String key, final Object value) {
        this.scriptEngine.put(key, value);
    }

    @Override
    public Object get(final String key) {
        return this.scriptEngine.get(key);
    }

    @Override
    public Bindings getBindings(final int scope) {
        return this.scriptEngine.getBindings(scope);
    }

    @Override
    public void setBindings(final Bindings bindings, final int scope) {
        this.scriptEngine.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return this.scriptEngine.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return this.scriptEngine.getContext();
    }

    @Override
    public void setContext(final ScriptContext context) {
        this.scriptEngine.setContext(context);
    }

    @Override
    public GremlinScriptEngineFactory getFactory() {
        return new GremlinJavaScriptScriptEngineFactory();
    }
}
