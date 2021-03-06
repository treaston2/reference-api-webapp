package org.hspconsortium.platform.api.service;

import org.hspconsortium.platform.api.fhir.DatabaseManager;
import org.hspconsortium.platform.api.model.Sandbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class SandboxPersister {

    public static final String SANDBOX_SCHEMA_PREFIX = "hapi_";

    public static final String INITIAL_DATASET_PATH = "db/prod_hapi_pu_04-18-16.sql";

    @Autowired
    private DatabaseManager databaseManager;

    public Collection<Sandbox> getSandboxes() {
        Collection<String> schemas = databaseManager.getSchemas();

        Collection<Sandbox> results = new ArrayList<>();
        for (String schema : schemas) {
            if (schema.startsWith(SANDBOX_SCHEMA_PREFIX)) {
                results.add(new Sandbox(toTeamId(schema)));
            }
        }

        return results;
    }

    public Sandbox createSandbox(Sandbox sandbox) {
        // create the sandbox schema
        databaseManager.createSchema(toSchemaName(sandbox.getTeamId()));

        // copy in the starter set
        try {
            ClassPathResource classPathResource = new ClassPathResource(INITIAL_DATASET_PATH);
            FileReader fileReader = new FileReader(classPathResource.getFile());
            databaseManager.loadInitialDataset(toSchemaName(sandbox.getTeamId()), fileReader);
        } catch (IOException e) {
            throw new RuntimeException("Error creating initial dataset", e);
        }

        return sandbox;
    }

    private String toSchemaName(String teamId) {
        return SANDBOX_SCHEMA_PREFIX + teamId;
    }

    private String toTeamId(String schemaName) {
        return schemaName.substring(SANDBOX_SCHEMA_PREFIX.length());
    }

}
