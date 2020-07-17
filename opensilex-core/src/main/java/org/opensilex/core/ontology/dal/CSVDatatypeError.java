/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.core.ontology.dal;

/**
 *
 * @author vmigot
 */
public class CSVDatatypeError extends CSVCell {

    private String datatype;

    public CSVDatatypeError(CSVCell cell, String datatype) {
        super(cell);
        this.datatype = datatype;
    }

    public String getDatatype() {
        return datatype;
    }

}