package com.microsoft.azure.maven.servicefabric;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Goal which adds a secret resource to a project.
 */
@Mojo(name = "addsecret", defaultPhase = LifecyclePhase.NONE)
public class AddSecretMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    
    /**
     * schema version of the secret yaml to be generated
    */
    @Parameter(property = "schemaVersion", defaultValue = Constants.DEFAULT_SCHEMA_VERSION)
    String schemaVersion;

    /**
     * Name of the secret
    */
    @Parameter(property = "secretName", required = true)
    String secretName;
    
    /**
     * Description of the secret
    */
    @Parameter(property = "secretDescription", defaultValue= Constants.DEFAULT_SECRET_DESCRIPTION)
    String secretDescription;

    /**
     * Kind of the secret
     */
    @Parameter(property = "secretKind", defaultValue= Constants.DEFAULT_SECRET_KIND)
    String secretKind;

    /**
     * Content type of the secret
     */
    @Parameter(property = "secretContentType", defaultValue = Constants.DEFAULT_SECRET_CONTENT_TYPE)
    String secretContentType;

    public Log logger  = getLog();
	
	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
		String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
        	throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        else{
            if(Utils.checkIfExists(Utils.getPath(appResourcesDirectory, "secret_" + secretName + ".yaml"))){
                throw new MojoFailureException("Secret Resource with the specified name already exists");
            }
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.SECRET_RESOURCE_NAME);
            try {
                String secretContent = IOUtil.toString(resource, "UTF-8");
                secretContent = Utils.replaceString(logger, secretContent, "SCHEMA_VERSION", schemaVersion, Constants.SECRET_RESOURCE_NAME);
                secretContent = Utils.replaceString(logger, secretContent, "SECRET_NAME", secretName, Constants.SECRET_RESOURCE_NAME);
                secretContent = Utils.replaceString(logger, secretContent, "SECRET_DESCRIPTION", secretDescription, Constants.SECRET_RESOURCE_NAME);
                secretContent = Utils.replaceString(logger, secretContent, "SECRET_CONTENT_TYPE", secretContentType, Constants.SECRET_RESOURCE_NAME);
                secretContent = Utils.replaceString(logger, secretContent, "SECRET_KIND", secretKind, Constants.SECRET_RESOURCE_NAME);
                FileUtils.fileWrite(Utils.getPath(appResourcesDirectory, "secret_" + secretName + ".yaml"), secretContent);
				logger.debug(String.format("Wrote %s secret content to output", secretName));
                TelemetryHelper.sendEvent(TelemetryEventType.ADDSECRET, String.format("Added secret with name: %s", secretName), logger);
            }
            catch (IOException e) {
				logger.error(e);
				throw new MojoFailureException("Error while writing output");
			} 
        }
    }
}