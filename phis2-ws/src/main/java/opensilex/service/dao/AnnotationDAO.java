//******************************************************************************
//                               AnnotationDAO.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 14 June 2018
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.configuration.DateFormats;
import opensilex.service.dao.exception.SemanticInconsistencyException;
import opensilex.service.dao.exception.UnknownUriException;
import opensilex.service.dao.manager.SparqlDAO;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.model.User;
import opensilex.service.ontology.Contexts;
import opensilex.service.ontology.Oa;
import opensilex.service.ontology.Oeso;
import opensilex.service.utils.sparql.SPARQLQueryBuilder;
import opensilex.service.utils.JsonConverter;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.utils.UriGenerator;
import opensilex.service.utils.dates.Dates;
import opensilex.service.view.brapi.Status;
import opensilex.service.model.Annotation;

/**
 * Annotations DAO.
 * @update [Andréas Garcia] 15 Feb. 2019: search parameters are no longer class 
 * attributes but parameters sent through search functions
 * @author Arnaud Charleroy <arnaud.charleroy@inra.fr>
 */
public class AnnotationDAO extends SparqlDAO<Annotation> {

    final static Logger LOGGER = LoggerFactory.getLogger(AnnotationDAO.class);

    // constants used for SPARQL names in the SELECT
    public static final String CREATED = "created";
    public static final String BODY_VALUE = "bodyValue";
    public static final String BODY_VALUES = "bodyValues";
    public static final String CREATOR = "creator";
    public static final String TARGET = "target";
    public static final String TARGETS = "targets";
    public static final String MOTIVATED_BY = "motivatedBy";

    public AnnotationDAO() {
        super();
    }

    public AnnotationDAO(User user) {
        super(user);
    }
    
    /**
     * Query generated with the searched parameters
     * @param uri
     * @param creator
     * @param target
     * @param bodyValue
     * @param motivatedBy
     * @example
     * SELECT DISTINCT ?uri 
     * WHERE { 
     *   ?uri <http://purl.org/dc/terms/creationDate> ?creationDate . 
     *   ?uri <http://purl.org/dc/terms/creator> ?creator .
     *   ?uri <http://www.w3.org/ns/oa#motivatedBy> ?motivatedBy . 
     *   ?uri <http://www.w3.org/ns/oa#bodyValue> ?bodyValue . } 
     * LIMIT 20
     * @return query generated with the searched parameter above
     */
    protected SPARQLQueryBuilder prepareSearchQuery(String uri, String creator, String target, String bodyValue, String motivatedBy) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();

        String annotationUri;
        if (uri != null) {
            annotationUri = "<" + uri + ">";
        } else {
            annotationUri = "?" + URI;
            query.appendSelect(annotationUri);
            query.appendGroupBy(annotationUri);
        }
        
        query.appendSelect("?" + CREATED);
        query.appendGroupBy("?" + CREATED);
        query.appendTriplet(annotationUri, DCTerms.created.getURI(), "?" + CREATED, null);

        if (creator != null) {
            query.appendTriplet(annotationUri, DCTerms.creator.getURI(), creator, null);
        } else {
            query.appendSelect("?" + CREATOR);
            query.appendGroupBy("?" + CREATOR);
            query.appendTriplet(annotationUri, DCTerms.creator.getURI(), "?" + CREATOR, null);
        }

        if (motivatedBy != null) {
            query.appendTriplet(annotationUri, Oa.RELATION_MOTIVATED_BY.toString(), motivatedBy, null);
        } else {
            query.appendSelect("?" + MOTIVATED_BY);
            query.appendGroupBy("?" + MOTIVATED_BY);
            query.appendTriplet(annotationUri, Oa.RELATION_MOTIVATED_BY.toString(), "?" + MOTIVATED_BY, null);
        }

        query.appendSelectConcat("?" + TARGET, SPARQLQueryBuilder.GROUP_CONCAT_SEPARATOR, "?" + TARGETS);
        query.appendTriplet(annotationUri, Oa.RELATION_HAS_TARGET.toString(), "?" + TARGET, null);
        if (target != null) {
            query.appendTriplet(annotationUri, Oa.RELATION_HAS_TARGET.toString(), target, null);
        }

        query.appendSelectConcat("?" + BODY_VALUE, SPARQLQueryBuilder.GROUP_CONCAT_SEPARATOR, "?" + BODY_VALUES);
        query.appendTriplet(annotationUri, Oa.RELATION_BODY_VALUE.toString(), "?" + BODY_VALUE, null);
        if (bodyValue != null) {
            query.appendFilter("regex(STR(?" + BODY_VALUE + "), '" + bodyValue + "', 'i')");
        }
        query.appendLimit(this.getPageSize());
        query.appendOffset(this.getPage() * this.getPageSize());
        LOGGER.debug(SPARQL_QUERY + query.toString());
        return query;
    }

    /**
     * @param searchUri
     * @param searchCreator
     * @param searchTarget
     * @param searchBodyValue
     * @param searchMotivatedBy
     * @return number of total annotation returned with the search field
     */
    public Integer count(String searchUri, String searchCreator, String searchTarget, String searchBodyValue, String searchMotivatedBy) 
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        SPARQLQueryBuilder prepareCount = prepareCount(
                searchUri, 
                searchCreator, 
                searchTarget, 
                searchBodyValue, 
                searchMotivatedBy);
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, prepareCount.toString());
        Integer count = 0;
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                BindingSet bindingSet = result.next();
                count = Integer.parseInt(bindingSet.getValue(COUNT_ELEMENT_QUERY).stringValue());
            }
        }
        return count;
    }

    /**
     * Counts query generated by the searched parameters.
     * @example
     * SELECT (count(distinct ?uri) as ?count) 
     * WHERE { 
     *   ?uri <http://purl.org/dc/terms/creationDate> ?creationDate . 
     *   ?uri <http://purl.org/dc/terms/creator>
     *   <http://www.phenome-fppn.fr/diaphen/id/agent/arnaud_charleroy> . 
     *   ?uri <http://www.w3.org/ns/oa#motivatedBy> <http://www.w3.org/ns/oa#commenting> . 
     *   ?uri <http://www.w3.org/ns/oa#bodyValue> ?bodyValue . 
     * FILTER (regex(STR(?bodyValue), 'Ustilago maydis infection', 'i') ) 
     * }
     * @return query generated with the searched parameters
     */
    private SPARQLQueryBuilder prepareCount(String searchUri, String searchCreator, String searchTarget, String searchBodyValue, String searchMotivatedBy) {
        SPARQLQueryBuilder query = this.prepareSearchQuery(
                searchUri, 
                searchCreator, 
                searchTarget, 
                searchBodyValue, 
                searchMotivatedBy);
        query.clearSelect();
        query.clearLimit();
        query.clearOffset();
        query.clearGroupBy();
        query.appendSelect("(COUNT(DISTINCT ?" + URI + ") AS ?" + COUNT_ELEMENT_QUERY + ")");
        LOGGER.debug(SPARQL_QUERY + " " + query.toString());
        return query;
    }

    /**
     * Checks and inserts the given annotations in the triplestore.
     * @param annotations
     * @return the insertion resultAnnotationUri. Message error if errors
     * found in data the list of the generated URI of the annotations if the
     * insertion has been done
     */
    public POSTResultsReturn checkAndInsert(List<Annotation> annotations) {
        POSTResultsReturn checkResult = check(annotations);
        if (checkResult.getDataState()) {
            return insert(annotations);
        } else { //errors found in data
            return checkResult;
        }
    }

    /**
     * Inserts the given annotations in the triplestore.
     * @param annotations
     * @return the insertion resultAnnotationUri, with the errors list or the
     * URI of the inserted annotations
     */
    public POSTResultsReturn insert(List<Annotation> annotations) {
        List<Status> insertStatus = new ArrayList<>();
        List<String> createdResourcesUri = new ArrayList<>();

        POSTResultsReturn results;
        boolean resultState = false;
        boolean annotationInsert = true;

        UriGenerator uriGenerator = new UriGenerator();

        //SILEX:test
        //Triplestore connection has to be checked (this is kind of a hot fix)
        this.getConnection().begin();
        //\SILEX:test

        for (Annotation annotation : annotations) {
            try {
                annotation.setUri(uriGenerator.generateNewInstanceUri(Oeso.CONCEPT_ANNOTATION.toString(), null, null));
            } catch (Exception ex) { //In the annotations case, no exception should be raised
                annotationInsert = false;
            }

            UpdateRequest query = prepareInsertQuery(annotation);
            Update prepareUpdate = this.getConnection().prepareUpdate(QueryLanguage.SPARQL, query.toString());
            prepareUpdate.execute();

            createdResourcesUri.add(annotation.getUri());
        }

        if (annotationInsert) {
            resultState = true;
            getConnection().commit();
        } else {
            getConnection().rollback();
        }

        results = new POSTResultsReturn(resultState, annotationInsert, true);
        results.statusList = insertStatus;
        results.setCreatedResources(createdResourcesUri);
        if (resultState && !createdResourcesUri.isEmpty()) {
            results.createdResources = createdResourcesUri;
            results.statusList.add(new Status(
                    StatusCodeMsg.RESOURCES_CREATED, 
                    StatusCodeMsg.INFO, 
                    createdResourcesUri.size() + " new resource(s) created"));
        }
        if (getConnection() != null) {
            getConnection().close();
        }
        return results;
    }

    /**
     * Generates an insert query for annotations. 
     * @example
     * INSERT DATA {
     *  <http://www.phenome-fppn.fr/platform/id/annotation/a2f9674f-3e49-4a02-8770-e5a43a327b37> rdf:type  <http://www.w3.org/ns/oa#Annotation> .
     *  <http://www.phenome-fppn.fr/platform/id/annotation/a2f9674f-3e49-4a02-8770-e5a43a327b37> <http://purl.org/dc/terms/creationDate> "2018-06-22 15:18:13+0200"^^xsd:dateTime .
     *  <http://www.phenome-fppn.fr/platform/id/annotation/a2f9674f-3e49-4a02-8770-e5a43a327b37> <http://purl.org/dc/terms/creator> http://www.phenome-fppn.fr/diaphen/id/agent/arnaud_charleroy> .
     *  <http://www.phenome-fppn.fr/platform/id/annotation/a2f9674f-3e49-4a02-8770-e5a43a327b37> <http://www.w3.org/ns/oa#bodyValue> "Ustilago maydis infection" .
     *  <http://www.phenome-fppn.fr/platform/id/annotation/a2f9674f-3e49-4a02-8770-e5a43a327b37> <http://www.w3.org/ns/oa#hasTarget> <http://www.phenome-fppn.fr/diaphen/id/agent/arnaud_charleroy> . 
     * @param annotation
     * @return the query
     */
    private UpdateRequest prepareInsertQuery(Annotation annotation) {
        UpdateBuilder spql = new UpdateBuilder();
        
        Node graph = NodeFactory.createURI(Contexts.ANNOTATIONS.toString());
        Resource annotationUri = ResourceFactory.createResource(annotation.getUri());
        Node annotationConcept = NodeFactory.createURI(Oeso.CONCEPT_ANNOTATION.toString());
        
        spql.addInsert(graph, annotationUri, RDF.type, annotationConcept);
        
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormats.YMDTHMSZ_FORMAT);
        Literal creationDate = ResourceFactory.createTypedLiteral(
                annotation.getCreated().toString(formatter), 
                XSDDatatype.XSDdateTime);
        spql.addInsert(graph, annotationUri, DCTerms.created, creationDate);
        
        Node creator =  NodeFactory.createURI(annotation.getCreator());
        spql.addInsert(graph, annotationUri, DCTerms.creator, creator);

        Property relationMotivatedBy = ResourceFactory.createProperty(Oa.RELATION_MOTIVATED_BY.toString());
        Node motivatedByReason =  NodeFactory.createURI(annotation.getMotivatedBy());
        spql.addInsert(graph, annotationUri, relationMotivatedBy, motivatedByReason);

        /**
         * @link https://www.w3.org/TR/annotation-model/#bodies-and-targets
         */
        if (annotation.getBodyValues() != null && !annotation.getBodyValues().isEmpty()) {
            Property relationBodyValue = ResourceFactory.createProperty(Oa.RELATION_BODY_VALUE.toString());
            for (String annotbodyValue : annotation.getBodyValues()) {
                 spql.addInsert(graph, annotationUri, relationBodyValue, annotbodyValue);
            }
        }
        /**
         * @link https://www.w3.org/TR/annotation-model/#bodies-and-targets
         */
        if (annotation.getTargets() != null && !annotation.getTargets().isEmpty()) {
            Property relationHasTarget = ResourceFactory.createProperty(Oa.RELATION_HAS_TARGET.toString());
            for (String targetUri : annotation.getTargets()) {
                Resource targetResourceUri = ResourceFactory.createResource(targetUri);
                spql.addInsert(graph, annotationUri, relationHasTarget, targetResourceUri);
            }
        }
        
        UpdateRequest query = spql.buildRequest();
                
        LOGGER.debug(getTraceabilityLogs() + " query : " + query.toString());
        return query;
    }

    /**
     * Checks the given annotations metadata.
     * @param annotations
     * @throws opensilex.service.dao.exception.SemanticInconsistencyException
     * @throws opensilex.service.dao.exception.UnknownUriException
     */
    public void check(List<Annotation> annotations) throws SemanticInconsistencyException, UnknownUriException {
        UriDAO uriDao = new UriDAO();
        UserDAO userDao = new UserDAO();

        // 1. check data
        for (Annotation annotation : annotations) {
            // 1.1 check motivation
            if (!uriDao.existUri(annotation.getMotivatedBy())
                    || !uriDao.isInstanceOf(annotation.getMotivatedBy(), Oa.CONCEPT_MOTIVATION.toString())) {
                throw new SemanticInconsistencyException(StatusCodeMsg.DATA_ERROR + ": " 
                        + StatusCodeMsg.WRONG_VALUE + " for the motivatedBy field");
            }

            // 1.2 check if person exist
            if (!userDao.existUserUri(annotation.getCreator())) {
                throw new UnknownUriException(StatusCodeMsg.UNKNOWN_URI + ": " +
                        StatusCodeMsg.WRONG_VALUE + " for the person URI");
            }
        }
    }

    /**
     * Searches all the annotations corresponding to the search parameters
     * @param searchUri
     * @param searchCreator
     * @param searchTarget
     * @param searchPage
     * @param searchBodyValue
     * @param searchMotivatedBy
     * @param searchPageSize
     * @return the list of the annotations found
     */
    public ArrayList<Annotation> searchAnnotations(String searchUri, String searchCreator, String searchTarget, String searchBodyValue, String searchMotivatedBy, int searchPage, int searchPageSize) {
        setPage(searchPage);
        setPageSize(searchPageSize);

        // retreve uri list
        SPARQLQueryBuilder query = prepareSearchQuery(
                searchUri, 
                searchCreator, 
                searchTarget, 
                searchBodyValue, 
                searchMotivatedBy);
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Annotation> annotations;
        // Retreive all informations
        // for each uri
        try (TupleQueryResult resultAnnotationUri = tupleQuery.evaluate()) {
            annotations = getAnnotationsFromResult(resultAnnotationUri, searchUri, searchCreator, searchMotivatedBy);
        }
        LOGGER.debug(JsonConverter.ConvertToJson(annotations));
        return annotations;
    }

    /**
     * Gets an annotation result from a given resultAnnotationUri. 
     * @param result a list of annotation from a search query
     * @param searchUri search URI
     * @param searchCreator search creator
     * @param searchMotivatedBy search motivated by
     * @return annotations with data extracted from the given bindingSets
     */
    private ArrayList<Annotation> getAnnotationsFromResult(TupleQueryResult result, String searchUri, String searchCreator, String searchMotivatedBy) {
        ArrayList<Annotation> annotations = new ArrayList<>();
        UriDAO uriDao = new UriDAO();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
       
            String annotationUri = null;
            if (searchUri != null) {
                if(uriDao.existUri(searchUri)){
                    annotationUri = searchUri;
                }
            } else {
                if(bindingSet.getValue(URI) != null){
                    annotationUri = bindingSet.getValue(URI).stringValue();
                }
            }
            //SILEX:info
            // This test is made because group concat function in the query can create empty row
            // e.g.
            // Uri Created Creator MotivatedBy Targets	BodyValues
            //                                 ""       ""
            //\SILEX:info
            if (annotationUri != null) {
                
                DateTime annotationCreated;
                // creationDate date
                String creationDate = bindingSet.getValue(CREATED).stringValue();
                annotationCreated = Dates.stringToDateTimeWithGivenPattern(creationDate, DateFormats.YMDTHMSZ_FORMAT);

                String annotationCreator;
                if (searchCreator != null) {
                    annotationCreator = searchCreator;
                } else {
                    annotationCreator = bindingSet.getValue(CREATOR).stringValue();
                }

                ArrayList<String> annotationBodyValues = null;
                if (bindingSet.getValue(BODY_VALUES) != null) {
                    //SILEX:info
                    // concat query return a list with comma separated value in one column
                    //\SILEX:info
                    annotationBodyValues = new ArrayList<>(Arrays.asList(bindingSet
                            .getValue(BODY_VALUES)
                            .stringValue()
                            .split(SPARQLQueryBuilder.GROUP_CONCAT_SEPARATOR)));
                }

                String annotationMotivation;
                if (searchMotivatedBy != null) {
                    annotationMotivation = searchMotivatedBy;
                } else {
                    annotationMotivation = bindingSet.getValue(MOTIVATED_BY).stringValue();
                }

                //SILEX:info
                // concat query return a list with comma separated value in one column.
                // An annotation has a least one target.
                //\SILEX:info
                ArrayList<String> annotationTargets = new ArrayList<>(Arrays.asList(bindingSet
                        .getValue(TARGETS)
                        .stringValue()
                        .split(SPARQLQueryBuilder.GROUP_CONCAT_SEPARATOR)));

                annotations.add(new Annotation(
                        annotationUri, 
                        annotationCreated, 
                        annotationCreator, 
                        annotationBodyValues, 
                        annotationMotivation, 
                        annotationTargets));
            }
        }
        return annotations;
    }

    @Override
    public List<Annotation> create(List<Annotation> objects) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<Annotation> objects) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Annotation> update(List<Annotation> objects) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Annotation find(Annotation object) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Annotation findById(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
