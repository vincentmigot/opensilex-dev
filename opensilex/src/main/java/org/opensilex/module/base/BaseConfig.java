//******************************************************************************
// OpenSILEX - Licence AGPL V3.0 - https://www.gnu.org/licenses/agpl-3.0.en.html
// Copyright © INRA 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.module.base;

import org.opensilex.bigdata.BigDataService;
import org.opensilex.config.ConfigDescription;
import org.opensilex.module.ModuleConfig;
import org.opensilex.server.security.AuthenticationService;
import org.opensilex.fs.FileStorageService;
import org.opensilex.sparql.SPARQLService;

/**
 *
 * @author Vincent Migot
 */
public interface BaseConfig extends ModuleConfig {

    /**
     * Flag to determine if application is in debug mode or not
     *
     * @return true Application in debug mode false Application in production
     * mode
     */
    @ConfigDescription(
            value = "Flag to determine if application is in debug mode or not",
            defaultBoolean = false
    )
    public Boolean debug();

    /**
     * Default application language
     *
     * @return default application language
     */
    @ConfigDescription(
            value = "Default application language",
            defaultString = "en"
    )
    public String defaultLanguage();

    @ConfigDescription(
        value = "Platform base URI",
        defaultString = "http://www.opensilex.org/"
    )
    public String baseURI();
    
    @ConfigDescription(
        value = "Default file storage directory"
    )
    public String storageBasePath();
    
    @ConfigDescription(
            value = "Big data source"
    )
    public BigDataService bigData();

    @ConfigDescription(
            value = "SPARQL data sources"
    )
    public SPARQLService sparql();

    @ConfigDescription(
            value = "Authentication service"
    )
    public AuthenticationService authentication();

    @ConfigDescription(
            value = "File storage service"
    )
    public FileStorageService fs();

}
