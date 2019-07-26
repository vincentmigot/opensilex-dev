/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.module.core.api.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opensilex.module.core.api.CoreAPIDocumentation;
import org.opensilex.module.core.dal.project.Project;
import org.opensilex.module.core.dal.project.ProjectDAO;
import org.opensilex.module.core.service.sparql.SPARQLService;
import org.opensilex.server.rest.RestApplicationAPI;
import org.opensilex.server.rest.validation.interfaces.Required;
import org.opensilex.server.rest.validation.interfaces.URL;

/**
 * Services of the projects
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
@Api("core/projects")
@Path("core/projects")
public class ProjectAPI implements RestApplicationAPI {
    @Inject
    SPARQLService sparql;
    
    /**
     * GET the informations of a project by its URI.
     * @param uri
     * @param limit
     * @param page
     * @return the project corresponding to its URI if it exist.
     */
    @GET
    @Path("{uri}")
    @ApiOperation(value = "Get a project by its URI",
                  notes = "Retrieve a project. Need URL encoded project URI (Unique Resource Identifier)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = RESPONSE_MESSAGE_200, response = ProjectDetailDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = RESPONSE_MESSAGE_400),
        @ApiResponse(code = 401, message = RESPONSE_MESSAGE_401),
        @ApiResponse(code = 500, message = RESPONSE_MESSAGE_500)
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectDetails(
        @ApiParam(value = CoreAPIDocumentation.RESOURCE_URI_DEFINITION, example = CoreAPIDocumentation.EXAMPLE_PROJECT_URI, required = true) @PathParam("projectURI") @URL @Required String uri,
        @ApiParam(value = CoreAPIDocumentation.PAGE_SIZE) @QueryParam(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) @Min(1) int limit,
        @ApiParam(value = CoreAPIDocumentation.PAGE) @QueryParam(PARAM_PAGE) @DefaultValue(DEFAULT_PAGE) @Min(0) int page) {
                
        try {
            ProjectDAO projectDAO = new ProjectDAO(sparql);
            
            Project project = projectDAO.getProjectByUri(new URI(uri));
            
            if (project != null) {
                return Response.ok(new ProjectDetailDTO(project)).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN.getStatusCode(), "Unknown project " + uri).build();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(ProjectAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ProjectAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}