/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.core.ontology.api;

import org.opensilex.core.ontology.dal.CSVValidationModel;

/**
 *
 * @author vmigot
 */
public class CSVValidationDTO {

    private String validationToken;

    private CSVValidationModel errors;

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }

    public CSVValidationModel getErrors() {
        return errors;
    }

    public void setErrors(CSVValidationModel errors) {
        this.errors = errors;
    }

}
