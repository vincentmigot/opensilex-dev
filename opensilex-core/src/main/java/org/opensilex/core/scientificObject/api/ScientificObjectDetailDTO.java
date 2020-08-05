/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.core.scientificObject.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.opensilex.core.factor.dal.FactorLevelModel;
import org.opensilex.core.germplasm.dal.GermplasmModel;
import org.opensilex.core.ontology.api.RDFObjectRelationDTO;
import org.opensilex.core.scientificObject.dal.ScientificObjectModel;
import org.opensilex.sparql.model.SPARQLModelRelation;
import org.opensilex.sparql.response.NamedResourceDTO;

/**
 *
 * @author vmigot
 */
public class ScientificObjectDetailDTO extends NamedResourceDTO<ScientificObjectModel> {
    
    private URI parent;
    
    private String parentLabel;
    
    private URI type;
    
    private String typeLabel;
    
    private List<NamedResourceDTO<FactorLevelModel>> factorLevels;
    
    private List<RDFObjectRelationDTO> relations;
    
    public URI getParent() {
        return parent;
    }
    
    public void setParent(URI parent) {
        this.parent = parent;
    }
    
    public String getParentLabel() {
        return parentLabel;
    }
    
    public void setParentLabel(String parentLabel) {
        this.parentLabel = parentLabel;
    }
    
    public URI getType() {
        return type;
    }
    
    public void setType(URI type) {
        this.type = type;
    }
    
    public String getTypeLabel() {
        return typeLabel;
    }
    
    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }
    
    public List<NamedResourceDTO<FactorLevelModel>> getFactorLevels() {
        return factorLevels;
    }
    
    public void setFactorLevels(List<NamedResourceDTO<FactorLevelModel>> factorLevels) {
        this.factorLevels = factorLevels;
    }
    
    public List<RDFObjectRelationDTO> getRelations() {
        return relations;
    }
    
    public void setRelations(List<RDFObjectRelationDTO> relations) {
        this.relations = relations;
    }
    
    @Override
    public void toModel(ScientificObjectModel model) {
        super.toModel(model);
    }
    
    @Override
    public void fromModel(ScientificObjectModel model) {
        super.fromModel(model);
        setType(model.getType());
        setTypeLabel(model.getTypeLabel().getDefaultValue());
        if (model.getParent() != null) {
            setParent(model.getParent().getUri());
            setParentLabel(model.getParent().getName());
        }

        List<NamedResourceDTO<FactorLevelModel>> factorLevelsDTO = new ArrayList<>(model.getFactorLevels().size());
        for (FactorLevelModel factorLevel : model.getFactorLevels()) {
            factorLevelsDTO.add(NamedResourceDTO.getDTOFromModel(factorLevel));
        }
        setFactorLevels(factorLevelsDTO);
        
        List<RDFObjectRelationDTO> relationsDTO = new ArrayList<>(model.getRelations().size());
        for (SPARQLModelRelation relation : model.getRelations()) {
            relationsDTO.add(RDFObjectRelationDTO.getDTOFromModel(relation));
        }
        setRelations(relationsDTO);
        
    }
    
    @Override
    public ScientificObjectModel newModelInstance() {
        return new ScientificObjectModel();
    }
    
    public static ScientificObjectDetailDTO getDTOFromModel(ScientificObjectModel model) {
        ScientificObjectDetailDTO dto = new ScientificObjectDetailDTO();
        dto.fromModel(model);
        
        return dto;
    }
}
