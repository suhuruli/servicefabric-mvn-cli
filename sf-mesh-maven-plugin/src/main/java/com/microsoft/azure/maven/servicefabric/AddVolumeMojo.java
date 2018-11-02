package com.microsoft.azure.maven.servicefabric;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.apache.maven.plugins.annotations.LifecyclePhase;

/**
 * Goal which adds a volume resource to a project.
 */
@Mojo( name = "addvolume", defaultPhase = LifecyclePhase.PROCESS_RESOURCES )
public class AddVolumeMojo extends AbstractMojo
{

    /**
     * schema version of the network yaml to be generated
    */
    @Parameter(property = "schemaVersion", defaultValue = Constants.DefaultSchemaVersion)
	String schemaVersion;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

	/**
     * Name of the volume
    */
    @Parameter(property = "volumeName", required = true)
	String volumeName;

	/**
     * Name of the volume
    */
    @Parameter(property = "volumeDescription", defaultValue = Constants.DefaultVolumeDescription)
	String volumeDescription;

	/**
     * Name of the volme provider
    */
    @Parameter(property = "volumeProvider", defaultValue = Constants.DefaultVolumeProvider)
	String volumeProvider;

	/**
     * Name of the volume share
    */
    @Parameter(property = "volumeShareName", required = true)
	String volumeShareName;

	/**
     * Name of the volume account
    */
    @Parameter(property = "volumeAccountName", required = true)
	String volumeAccountName;

	/**
     * Key of the volume account 
    */
    @Parameter(property = "volumeAccountKey", required = true)
	String volumeAccountKey;

	public Log logger  = getLog();
	
	@Override
	public void execute() throws MojoFailureException {
		String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
		String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
        	throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        else{
            if(Utils.checkIfExists(Utils.getPath(appResourcesDirectory, "volume_" + volumeName + ".yaml"))){
                throw new MojoFailureException("Volume Resource with the specified name already exists");
			}
			InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.VolumeResourceName);
			try {
				String volumeContent = IOUtil.toString(resource, "UTF-8");
				volumeContent = Utils.replaceString(logger, volumeContent, "VOLUME_NAME", volumeName, Constants.VolumeResourceName);
				volumeContent = Utils.replaceString(logger, volumeContent, "VOLUME_DESCRIPTION", volumeDescription, Constants.VolumeResourceName);
				volumeContent = Utils.replaceString(logger, volumeContent, "VOLUME_PROVIDER", volumeProvider, Constants.VolumeResourceName);
				volumeContent = Utils.replaceString(logger, volumeContent, "VOLUME_ACCOUNT_NAME", volumeAccountName, Constants.VolumeResourceName);
				volumeContent = Utils.replaceString(logger, volumeContent, "VOLUME_ACCOUNT_KEY", volumeAccountKey, Constants.VolumeResourceName);
				FileUtils.fileWrite(Utils.getPath(appResourcesDirectory, "volume_" + volumeName + ".yaml"), volumeContent);
				logger.debug("Wrote content to output");
				TelemetryHelper.sendEvent(TelemetryEventType.ADDVOLUME, String.format("Added volume with name: %s", volumeName), logger);
			} catch (IOException e) {
				logger.error(e);
				throw new MojoFailureException("Error while writing output");
			} 
		}
	}
}