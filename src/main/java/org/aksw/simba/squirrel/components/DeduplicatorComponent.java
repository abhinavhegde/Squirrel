package org.aksw.simba.squirrel.components;

import org.aksw.simba.squirrel.data.uri.filter.KnownUriFilter;
import org.aksw.simba.squirrel.data.uri.filter.RDBKnownUriFilter;
import org.aksw.simba.squirrel.deduplication.hashing.impl.HashValueUriPair;
import org.aksw.simba.squirrel.sink.Sink;
import org.aksw.simba.squirrel.sink.impl.rdfSink.RDFSink;
import org.hobbit.core.components.AbstractComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DeduplicatorComponent extends AbstractComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeduplicatorComponent.class);

    private KnownUriFilter knownUriFilter;
    private Sink sink;


    @Override
    public void init() {
        Map<String, String> env = System.getenv();

        String rdbHostName = null;
        int rdbPort = -1;
        if (env.containsKey(FrontierComponent.RDB_HOST_NAME_KEY)) {
            rdbHostName = env.get(FrontierComponent.RDB_HOST_NAME_KEY);
            if (env.containsKey(FrontierComponent.RDB_PORT_KEY)) {
                rdbPort = Integer.parseInt(env.get(FrontierComponent.RDB_PORT_KEY));
            } else {
                LOGGER.warn("Couldn't get {} from the environment. An in-memory queue will be used.", FrontierComponent.RDB_PORT_KEY);
            }
        } else {
            LOGGER.warn("Couldn't get {} from the environment. An in-memory queue will be used.", FrontierComponent.RDB_HOST_NAME_KEY);
        }

        if ((rdbHostName != null) && (rdbPort > 0)) {
            knownUriFilter = new RDBKnownUriFilter(rdbHostName, rdbPort, FrontierComponent.doRecrawling);
            knownUriFilter.open();
        }

        // TODO: other kinds of sinks must be possible as well
        sink = new RDFSink();
    }

    @Override
    public void run() throws Exception {
        // periodically compare hash values for all uris
        List<HashValueUriPair> allUrisAndHashValues = knownUriFilter.getAllUrisAndHashValues();

        for (HashValueUriPair pair1 : allUrisAndHashValues) {
            for (HashValueUriPair pair2 : allUrisAndHashValues) {
                if (!pair1.uri.equals(pair2.uri)) {
                    if (pair1.hashValue.equals(pair2.hashValue)) {
                        // get triples from pair1 and pair2 and compare them
                        //sink.getTriplesForUri(pair1.uri);
                        //sink.getTriplesForUri(pair2.uri);
                    }
                }
            }
        }

        // TODO: sleep for some time

    }

    @Override
    public void close() throws IOException {
        knownUriFilter.close();
    }
}
