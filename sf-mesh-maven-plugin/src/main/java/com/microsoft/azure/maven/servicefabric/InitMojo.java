package com.microsoft.azure.maven.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
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
 * Goal which creates initial application resource of a project.
 */
@Mojo( name = "init", defaultPhase = LifecyclePhase.PROCESS_RESOURCES )
public class InitMojo extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Name of the application
    */
    @Parameter(property = "applicationName", required = true)
    String applicationName;

    /**
     * Description of the application
    */
    @Parameter(property = "applicationDescription", defaultValue = Constants.DefaultApplicationDescription)
    String applicationDescription;

    private Log logger  = getLog();

    @Override
    public void execute() throws MojoFailureException
    {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            logger.debug(String.format("Creating service fabric resources directory %s", serviceFabricResourcesDirectory));
            Utils.createDirectory(logger, serviceFabricResourcesDirectory);
        }
        if(!Utils.checkIfExists(appResourcesDirectory)){
            Utils.createDirectory(logger, appResourcesDirectory);
        }
        try {
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.ApplicationResourceName);
            String appContent = IOUtil.toString(resource, "UTF-8"); 
            appContent = Utils.replaceString(logger, appContent, "APP_NAME", applicationName, Constants.ApplicationResourceName);
            appContent = Utils.replaceString(logger, appContent, "APP_DESCRIPTION", applicationDescription, Constants.ApplicationResourceName);
            String appYamlPath = Utils.getPath(appResourcesDirectory, "app_" + applicationName + ".yaml");
            if(Utils.checkIfExists(appYamlPath)){
                throw new MojoFailureException(String.format("App resource with the name %s already exists", applicationName));
            }
            else{
                FileUtils.fileWrite(appYamlPath, appContent);
            }
            logger.debug("Wrote content to output");
            logger.debug("Adding Service");
            TelemetryHelper.sendEvent(TelemetryEventType.INIT, String.format("Added application with name: %s", applicationName), logger);
		} catch (IOException e) {
            logger.error(e);
            throw new MojoFailureException("Error while writing output");
		}
    }
}
