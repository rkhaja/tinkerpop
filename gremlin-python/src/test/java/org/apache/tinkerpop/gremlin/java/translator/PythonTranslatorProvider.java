/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tinkerpop.gremlin.java.translator;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.AbstractGraphProvider;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalInterruptionComputerTest;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalInterruptionTest;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.ProgramTest;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.ElementIdStrategyProcessTest;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategyProcessTest;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.PartitionStrategyProcessTest;
import org.apache.tinkerpop.gremlin.python.jsr223.JythonScriptEngineSetup;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerEdge;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerElement;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraphVariables;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerProperty;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerVertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerVertexProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class PythonTranslatorProvider extends AbstractGraphProvider {

    protected static final boolean IMPORT_STATICS = new Random().nextBoolean();

    static {
        JythonScriptEngineSetup.setup();
    }

    private static Set<String> SKIP_TESTS = new HashSet<>(Arrays.asList(
            "testProfileStrategyCallback",
            "testProfileStrategyCallbackSideEffect",
            "g_V_branchXlabelX_optionXperson__ageX_optionXsoftware__langX_optionXsoftware__nameX",
            "g_V_chooseXout_countX_optionX2L__nameX_optionX3L__valueMapX",
            "g_withSideEffectXa_setX_V_both_name_storeXaX_capXaX",
            "g_V_both_hasLabelXpersonX_order_byXage_decrX_name",
            "g_VX1X_out_injectXv2X_name",
            "g_addVXpersonX_propertyXsingle_name_stephenX_propertyXsingle_name_stephenmX",
            "g_addVXpersonX_propertyXsingle_name_stephenX_propertyXsingle_name_stephenm_since_2010X",
            "shouldSupportGraphFilter",
            "shouldNeverPropagateANoBulkTraverser",
            "shouldNeverPropagateANullValuedTraverser",
            "shouldTraversalResetProperly",
            "shouldHidePartitionKeyForValues",
            "shouldAddStartsProperly",
            ProgramTest.Traversals.class.getCanonicalName(),
            TraversalInterruptionTest.class.getCanonicalName(),
            TraversalInterruptionComputerTest.class.getCanonicalName(),
            ElementIdStrategyProcessTest.class.getCanonicalName(),
            EventStrategyProcessTest.class.getCanonicalName(),
            PartitionStrategyProcessTest.class.getCanonicalName()));

    private static final Set<Class> IMPLEMENTATION = new HashSet<Class>() {{
        add(TinkerEdge.class);
        add(TinkerElement.class);
        add(TinkerGraph.class);
        add(TinkerGraphVariables.class);
        add(TinkerProperty.class);
        add(TinkerVertex.class);
        add(TinkerVertexProperty.class);
    }};

    @Override
    public Map<String, Object> getBaseConfiguration(final String graphName, final Class<?> test, final String testMethodName,
                                                    final LoadGraphWith.GraphData loadGraphWith) {

        final TinkerGraph.DefaultIdManager idManager = selectIdMakerFromGraphData(loadGraphWith);
        final String idMaker = (idManager.equals(TinkerGraph.DefaultIdManager.ANY) ? selectIdMakerFromGraphData(loadGraphWith) : idManager).name();
        return new HashMap<String, Object>() {{
            put(Graph.GRAPH, TinkerGraph.class.getName());
            put(TinkerGraph.GREMLIN_TINKERGRAPH_VERTEX_ID_MANAGER, idMaker);
            put(TinkerGraph.GREMLIN_TINKERGRAPH_EDGE_ID_MANAGER, idMaker);
            put(TinkerGraph.GREMLIN_TINKERGRAPH_VERTEX_PROPERTY_ID_MANAGER, idMaker);
            put("skipTest", SKIP_TESTS.contains(testMethodName) || SKIP_TESTS.contains(test.getCanonicalName()));
            if (loadGraphWith == LoadGraphWith.GraphData.CREW)
                put(TinkerGraph.GREMLIN_TINKERGRAPH_DEFAULT_VERTEX_PROPERTY_CARDINALITY, VertexProperty.Cardinality.list.name());
        }};
    }

    @Override
    public void clear(final Graph graph, final Configuration configuration) throws Exception {
        if (graph != null) graph.close();
    }

    @Override
    public Set<Class> getImplementations() {
        return IMPLEMENTATION;
    }

    /**
     * Test that load with specific graph data can be configured with a specific id manager as the data type to
     * be used in the test for that graph is known.
     */
    protected TinkerGraph.DefaultIdManager selectIdMakerFromGraphData(final LoadGraphWith.GraphData loadGraphWith) {
        if (null == loadGraphWith) return TinkerGraph.DefaultIdManager.ANY;
        if (loadGraphWith.equals(LoadGraphWith.GraphData.CLASSIC))
            return TinkerGraph.DefaultIdManager.INTEGER;
        else if (loadGraphWith.equals(LoadGraphWith.GraphData.MODERN))
            return TinkerGraph.DefaultIdManager.INTEGER;
        else if (loadGraphWith.equals(LoadGraphWith.GraphData.CREW))
            return TinkerGraph.DefaultIdManager.INTEGER;
        else if (loadGraphWith.equals(LoadGraphWith.GraphData.GRATEFUL))
            return TinkerGraph.DefaultIdManager.INTEGER;
        else
            throw new IllegalStateException(String.format("Need to define a new %s for %s", TinkerGraph.IdManager.class.getName(), loadGraphWith.name()));
    }
}
