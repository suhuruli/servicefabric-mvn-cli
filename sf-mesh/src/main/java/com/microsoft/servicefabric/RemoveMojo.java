package com.microsoft.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.logging.Log;

@Mojo( name = "remove", defaultPhase = LifecyclePhase.NONE )
public class RemoveMojo extends AbstractMojo
{

    @Parameter(property = "deploymentType", defaultValue = Constants.LocalDeploymentType)
    String deploymentType;

    @Parameter(property = "applicationName", required = true)
    String applicationName;

    @Parameter(property = "ipAddress", defaultValue = Constants.DefaultIPAddress)
    String ipAddress;

    @Parameter(property = "port", defaultValue = Constants.DefaultPort)
    String port;

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        if(deploymentType.equalsIgnoreCase(Constants.LocalDeploymentType)){
            Utils.checksfctlinstallation(logger);
            Utils.connecttolocalcluster(logger, ipAddress, port);
            Utils.executeCommand(logger, "sfctl mesh app delete --application-resource-name " + applicationName);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.CloudDeploymentType)){
            //To be implemented
        }
        else{
            logger.error(String.format("%s deployment type is not vaild", deploymentType));
            return;
        }
	}
}