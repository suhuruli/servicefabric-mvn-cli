package com.microsoft.servicefabric;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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

    /**
     * Resource Group name for mesh deployment. Only valid in cloud deployment type
    */
    @Parameter(property = "resourceGroup", defaultValue = Constants.DefaultResourceGroup)
    String resourceGroup;

    /**
     * Delete Resource group along with application resource deletion. Only valid in cloud deployment type
    */
    @Parameter(property = "deleteResourceGroup", defaultValue = Constants.DefaultDeleteResourceGroup)
    String deleteResourceGroup;


    public Log logger  = getLog();

	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        if(deploymentType.equalsIgnoreCase(Constants.LocalDeploymentType)){
            /*Utils.checksfctlinstallation(logger);
            Utils.connecttolocalcluster(logger, ipAddress, port);
            Utils.executeCommand(logger, "sfctl mesh app delete --application-resource-name " + applicationName);*/
            removeApplicationLocal();
            TelemetryHelper.sendEvent(TelemetryEventType.RemoveMojo, String.format("Removed application %s locally", applicationName), logger);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.CloudDeploymentType)){
            //To be implemented
            if(resourceGroup.equalsIgnoreCase(Constants.DefaultResourceGroup)){
                throw new MojoFailureException("Resource Group is not mentioned. Please mention the resource group in which your application is deployed");    
            }
            logger.info("Deleting Application");
            Utils.executeCommand(logger, String.format("az mesh app delete --name %s --resource-group %s --yes", applicationName, resourceGroup));
            TelemetryHelper.sendEvent(TelemetryEventType.RemoveMojo, String.format("Removed application %s from mesh", applicationName), logger);
            if(deleteResourceGroup.equalsIgnoreCase("true")){
                logger.info("Deleting Resource group");
                Utils.executeCommand(logger, String.format("az group delete --name %s --yes", resourceGroup));
            }
        }
        else{
            throw new MojoFailureException(String.format("%s deployment type is not valid", deploymentType));
        }
    }
    
    private void removeApplicationLocal() throws MojoFailureException {
        try{
            String urlString = String.format("http://%s:%s/Resources/Applications/%s?api-version=6.3-preview", ipAddress, port, applicationName);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            String responseCode = Integer.toString(conn.getResponseCode());
            if(!responseCode.matches("2..")){
                logger.debug("Got invalid response Code");
                throw new MojoFailureException("Failed : HTTP error code : " + conn.getResponseCode());    
            }
        }
        catch (IOException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("Failed removing the resource %s", applicationName));
        }
    }
}