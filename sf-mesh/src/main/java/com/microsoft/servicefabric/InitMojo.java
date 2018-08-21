package com.microsoft.servicefabric;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;


/**
 * Goal which create application resources of a project.
 */
@Mojo( name = "init", defaultPhase = LifecyclePhase.PROCESS_RESOURCES )
public class InitMojo extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "applicationName", required = true)
    String applicationName;

    @Parameter(property = "applicationDescription", defaultValue = Constants.DefaultApplicationDescription)
    String applicationDescription;

    private Log logger  = getLog();

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        String resourceDirectory = Utils.getResourcesDirectory(logger, project);
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        if(Utils.checkIfExists(serviceFabricResourcesDirectory)){
            logger.info("Service fabric resources folder already exists!");
            return;
        }
        else{
            if(!Utils.checkIfExists(resourceDirectory)){
                logger.debug(String.format("Creating resources directory %s", resourceDirectory));
                Utils.createDirectory(logger, resourceDirectory);
            }
            logger.debug(String.format("Creating service fabric resources directory %s", serviceFabricResourcesDirectory));
            Utils.createDirectory(logger, serviceFabricResourcesDirectory);
        }
        logger.debug(String.format("Using the following directory: %s", resourceDirectory));
        try {
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.ApplicationResourceName);
            logger.debug(String.format("Reading content form the %s", resource));
            String appContent = IOUtil.toString(resource, "UTF-8"); 
            appContent = Utils.replaceString(logger, appContent, "APP_NAME", applicationName, Constants.ApplicationResourceName);
            appContent = Utils.replaceString(logger, appContent, "APP_DESCRIPTION", applicationDescription, Constants.ApplicationResourceName);
            FileUtils.fileWrite(Utils.getPath(serviceFabricResourcesDirectory, applicationName + ".yaml"), appContent);
            logger.debug("Wrote content to output");
		} catch (IOException e) {
            logger.error("Error while writing output");
            logger.error(e);
            return;
		}
    }
}
