//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core;

import com.auth0.jwt.JWTCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import org.opensilex.OpenSilexModule;

import java.util.List;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OA;
import org.opensilex.core.ontology.Oeso;
import org.opensilex.core.ontology.dal.OntologyDAO;
import org.opensilex.security.extensions.LoginExtension;
import org.opensilex.security.user.dal.UserModel;
import org.opensilex.server.extensions.APIExtension;
import org.opensilex.server.rest.cache.JCSApiCacheExtension;
import org.opensilex.sparql.deserializer.URIDeserializer;
import org.opensilex.sparql.extensions.OntologyFileDefinition;
import org.opensilex.sparql.extensions.SPARQLExtension;
import org.opensilex.sparql.service.SPARQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core OpenSILEX module implementation
 */
public class CoreModule extends OpenSilexModule implements APIExtension, LoginExtension, SPARQLExtension, JCSApiCacheExtension {

    private final static Logger LOGGER = LoggerFactory.getLogger(CoreModule.class);

    @Override
    public Class<?> getConfigClass() {
        return CoreConfig.class;
    }

    @Override
    public String getConfigId() {
        return "core";
    }

    @Override
    public void login(UserModel user, JWTCreator.Builder tokenBuilder) throws Exception {

        // TODO add experiments, projects, infrastructures related to the user as token claims...
    }

    @Override
    public List<String> getPackagesToScan() {
        List<String> list = APIExtension.super.getPackagesToScan();

        if (getConfig(CoreConfig.class).enableLogs()) {
            list.add("org.opensilex.core.logs.filter");
        }

        return list;
    }

    @Override
    public List<OntologyFileDefinition> getOntologiesFiles() throws Exception {
        List<OntologyFileDefinition> list = SPARQLExtension.super.getOntologiesFiles();
        list.add(new OntologyFileDefinition(
                OA.NS,
                "ontologies/oa.rdf",
                Lang.RDFXML,
                "oa"
        ));
        list.add(new OntologyFileDefinition(
                "http://www.opensilex.org/vocabulary/oeso#",
                "ontologies/oeso-core.owl",
                Lang.RDFXML,
                "oeso"
        ));
        list.add(new OntologyFileDefinition(
                "http://www.opensilex.org/vocabulary/oeev#",
                "ontologies/oeev.owl",
                Lang.RDFXML,
                "oeev"
        ));
        return list;
    }

    @Override
    public void setup() throws Exception {
        SPARQLService.addPrefix(Oeso.PREFIX, Oeso.NS);
        URIDeserializer.setPrefixes(SPARQLService.getPrefixMapping(), true);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        for (OpenSilexModule module : getOpenSilex().getModules()) {
            InputStream yamlInput = module.getFileInputStream("ontologies/opensilex-ontology.yml");
            if (yamlInput != null) {
                JsonNode node = yamlMapper.readTree(yamlInput);
                root = mapper.updateValue(root, node);
            }
        }

        OntologyDAO.customizeOntology(root);
    }

}
