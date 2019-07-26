//******************************************************************************
//                            Server.java
// OpenSILEX
// Copyright © INRA 2019
// Creation date: 15 March 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package org.opensilex.server;

import org.opensilex.OpenSilex;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.IOTools;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JasperInitializer;
import org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer;
import org.opensilex.utils.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends Tomcat server to embbeded it and load OpenSilex services,
 * rdf4j server and workbench
 */
public class Server extends Tomcat {

    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static Server start(OpenSilex app, Path tomcatDirectory, String host, int port, int adminPort) throws LifecycleException {
        Server server = new Server(app, host, port, adminPort, tomcatDirectory);
        server.start();
        
        return server;
    }

      private final OpenSilex instance;
    /**
     * Construct OpenSilex server with host, port and adminPort adminPort is
     * used to communicate with the running server by the cli
     *
     * @param host
     * @param port
     * @param adminPort
     */
    public Server(OpenSilex instance, String host, int port, int adminPort, Path tomcatDirectory) {
        super();
        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");

        this.instance = instance;
        String baseDir = tomcatDirectory.toFile().getAbsolutePath();
        setBaseDir(baseDir);
        setPort(port);
        setHostname(host);
        getHost().setAppBase(baseDir);
        getServer().setParentClassLoader(Thread.currentThread().getContextClassLoader());

        initRootApp();

        getConnector();

        initAdminThread(adminPort);
    }

    /**
     * Initialize OpenSilex Root webapp (swagger and jersey services) located at
     * the root "/" of the server.
     */
    private void initRootApp() {
        try {
            Context context = addWebapp("", new File(".").getAbsolutePath());
            WebResourceRoot resource = new StandardRoot(context);
            File jarFile = ClassInfo.getJarFile(Server.class);
            if (jarFile.isFile()) {
                resource.createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", jarFile.getCanonicalPath(), null, "/webapp");
            } else {
                resource.createWebResourceSet(WebResourceRoot.ResourceSetType.PRE, "/", jarFile.getCanonicalPath(), null, "/webapp");
            }
            context.getServletContext().setAttribute("opensilex", instance);
            context.setResources(resource);
        } catch (IOException ex) {
            LOGGER.error("Can't initialize main application", ex);
        }
    }

    public void stop() throws LifecycleException {
        instance.clean();
        super.stop();
    }

    /**
     * Load war application at the given contextPath (root url of the war)
     *
     * @param contextPath
     * @param warFile
     */
    public void addWarApp(String contextPath, String warFile) {
        try {
            Path basePath = Paths.get(getHost().getAppBase());

            File targetWar = basePath.resolve(warFile).toFile();

            try (InputStream warSource = getClass().getClassLoader().getResourceAsStream("war/" + warFile);
                    OutputStream output = new FileOutputStream(targetWar)) {
                IOTools.flow(warSource, output);
            }

            Context context = addWebapp(contextPath, targetWar.getAbsolutePath());
            JerseyServletContainerInitializer j;
            context.setContainerSciFilter("JerseyServletContainerInitializer");
            context.addServletContainerInitializer(new JasperInitializer(), null);

        } catch (IOException ex) {
            LOGGER.error("[Context: " + contextPath + "] Can't add WAR file: " + warFile, ex);
        }
    }

    /**
     * Init administration thread on the given port to listen for commands
     * execution from cli
     *
     * @param adminPort
     */
    private void initAdminThread(int adminPort) {
        try {
            Thread adminThread = new Thread(new ServerAdmin(this, adminPort));
            adminThread.setDaemon(true);
            adminThread.start();
        } catch (IOException ex) {
            LOGGER.warn("Can't initialize admin thread, tomcat must be killed in another way", ex);
        }
    }

}