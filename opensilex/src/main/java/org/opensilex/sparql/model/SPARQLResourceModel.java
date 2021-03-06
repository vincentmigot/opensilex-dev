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
import org.opensilex.sparql.annotations.SPARQLResourceURI;


/**
 *
 * @author vidalmor
 */
public abstract class SPARQLResourceModel implements SPARQLModel {
    
    @SPARQLResourceURI()
    private URI uri;
    public static final String URI_FIELD = "uri";
    
    protected List<SPARQLModelRelation> relations = new ArrayList<>();
        
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
    
    public List<SPARQLModelRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<SPARQLModelRelation> relations) {
        this.relations = relations;
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
    
    
}
