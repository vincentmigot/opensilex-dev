//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.core.variable.dal;

import org.opensilex.sparql.SPARQLService;


/**
 *
 * @author vidalmor
 */
public class MethodDAO extends BaseVariableDAO<MethodModel> {
    
    public MethodDAO(SPARQLService sparql) {
        super(MethodModel.class, sparql);
    }
}
