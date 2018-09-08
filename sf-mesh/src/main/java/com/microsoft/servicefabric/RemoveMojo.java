package com.microsoft.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal which removes the mesh application from local cluster or mesh
 */
@Mojo( name = "remove", defaultPhase = LifecyclePhase.NONE )
public class RemoveMojo extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Type of deployment local or cloud
    */
    @Parameter(property = "deploymentType", defaultValue = Constants.LocalDeploymentType)
    String deploymentType;

    /**
     * Name of the application
    */
    @Parameter(property = "applicationName", required = true)
    String applicationName;

    /**
     * IP address or domain name of the cluster in which this application should be deployed. Only valid in local deployment type.
    */
    @Parameter(property = "ipAddress", defaultValue = Constants.DefaultIPAddress)
    String ipAddress;

    /**
     * HTTP Gateway port of the cluster. Only valid in local deployment type
    */
    @Parameter(property = "port", defaultValue = Constants.DefaultPort)
    String port;

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        if(deploymentType.equalsIgnoreCase(Constants.LocalDeploymentType)){
            Utils.checksfctlinstallation(logger);
            Utils.connecttolocalcluster(logger, ipAddress, port);
            Utils.executeCommand(logger, "sfctl mesh app delete --application-resource-name " + applicationName);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.CloudDeploymentType)){
            //To be implemented
        }
        else{
            throw new MojoFailureException(String.format("%s deployment type is not vaild", deploymentType));
        }
	}
}