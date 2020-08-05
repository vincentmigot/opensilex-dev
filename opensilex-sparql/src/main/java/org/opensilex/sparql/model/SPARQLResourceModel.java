//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.sparql.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.vocabulary.DCTerms;
import org.opensilex.sparql.annotations.SPARQLProperty;
import org.opensilex.sparql.annotations.SPARQLResource;
import org.opensilex.sparql.annotations.SPARQLResourceURI;
import org.opensilex.sparql.annotations.SPARQLTypeRDF;
import org.opensilex.sparql.annotations.SPARQLTypeRDFLabel;
import org.opensilex.sparql.utils.Ontology;

/**
 *
 * @author vidalmor
 */
@SPARQLResource(
        ontology = Ontology.class,
        resource = "SPARQLResourceModel"
)
public class SPARQLResourceModel implements SPARQLModel {

    @SPARQLResourceURI()
    protected URI uri;
    public static final String URI_FIELD = "uri";

    @SPARQLTypeRDF()
    protected URI type;
    public static final String TYPE_FIELD = "type";

    @SPARQLTypeRDFLabel()
    protected SPARQLLabel typeLabel;

    @SPARQLProperty(
            ontology = DCTerms.class,
            property = "creator"
    )
    protected URI creator;

    protected List<SPARQLModelRelation> relations = new ArrayList<>();

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public SPARQLLabel getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(SPARQLLabel typeLabel) {
        this.typeLabel = typeLabel;
    }

    public List<SPARQLModelRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<SPARQLModelRelation> relations) {
        this.relations = relations;
    }

    public URI getCreator() {
        return creator;
    }

    public void setCreator(URI creator) {
        this.creator = creator;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.uri);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SPARQLResourceModel other = (SPARQLResourceModel) obj;
        return Objects.equals(this.uri, other.uri);
    }

    public void addRelation(URI graph, URI propertyURI, Class<?> type, String value) {
        if (this.relations == null) {
            this.relations = new ArrayList<>();
        }

        SPARQLModelRelation r = new SPARQLModelRelation();
        r.setGraph(graph);
        r.setProperty(Ontology.property(propertyURI));
        r.setType(type);
        r.setValue(value);
        r.setReverse(false);

        this.relations.add(r);
    }

}
