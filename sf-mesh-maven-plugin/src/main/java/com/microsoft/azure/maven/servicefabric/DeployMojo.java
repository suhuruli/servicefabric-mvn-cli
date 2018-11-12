package com.microsoft.azure.maven.servicefabric;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.logging.Log;
/**
 * Goal which deploys the mesh application to local cluster, SFRP or mesh
 */
@Mojo( name = "deploy", defaultPhase = LifecyclePhase.NONE )
public class DeployMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Type of deployment local or mesh or sfrp
    */
    @Parameter(property = "deploymentType", defaultValue = Constants.LOCAL_DEPLOYMENT_TYPE)
    String deploymentType;

    /**
     * Comma seperated resource files or the directory in which the resource files are present
    */
    @Parameter(property = "inputYamlFilePaths", defaultValue = Constants.SERVICE_FABRIC_RESOURCES_PATH)
    String inputYamlFilePaths;

    /**
     * URL of the cluster in which this application should be deployed. Only valid in local/sfrp deployment type.
    */
    @Parameter(property = "clusterEndpoint", defaultValue = Constants.DEFAULT_CLUSTER_ENDPOINT)
    String clusterEndpoint;

    /**
     * Resource Group name for mesh deployment. Only valid in mesh deployment type
    */
    @Parameter(property = "resourceGroup", defaultValue = Constants.DEFAULT_RESOURCE_GROUP)
    String resourceGroup;

    /**
     * Location of the resource group. Only valid in mesh deployment type
    */
    @Parameter(property = "location", defaultValue = Constants.DEFAULT_LOCATION)
    String location;

    /**
     * Location of pem file. Only valid in sfrp deployment type
     */
    @Parameter(property = "pemFilePath", defaultValue = Constants.DEFAULT_PEM_FILE_PATH)
    String pemFilePath;

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        if(inputYamlFilePaths.equals(Constants.SERVICE_FABRIC_RESOURCES_PATH)){
            inputYamlFilePaths = Utils.getServicefabricResourceDirectory(logger, project);
        }
        if(deploymentType.equalsIgnoreCase(Constants.LOCAL_DEPLOYMENT_TYPE)){
            Utils.checksfctlinstallation(logger);
            Utils.connecttounsecurecluster(logger, clusterEndpoint);
            Utils.executeCommand(logger, "sfctl mesh deployment create --input-yaml-file-paths " + inputYamlFilePaths);
            TelemetryHelper.sendEvent(TelemetryEventType.DEPLOYLOCAL, String.format("Deployed application locally"), logger);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.SFRP_DEPLOYMENT_TYPE)){
            if(pemFilePath.equalsIgnoreCase(Constants.DEFAULT_PEM_FILE_PATH)){
                throw new MojoFailureException("PEM file path is not provided. Please provide location to the pem file for connecting to SFRP cluster");
            }
            Utils.checksfctlinstallation(logger);
            Utils.connecttosecurecluster(logger, clusterEndpoint, pemFilePath);
            Utils.executeCommand(logger, "sfctl mesh deployment create --input-yaml-file-paths " + inputYamlFilePaths);
            TelemetryHelper.sendEvent(TelemetryEventType.DEPLOYSFRP, String.format("Deployed application to SFRP"), logger);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.MESH_DEPLOYMENT_TYPE)){
            Utils.checkazinstallation(logger);

            if(resourceGroup.equals(Constants.DEFAULT_RESOURCE_GROUP)){
                throw new MojoFailureException("Resource group is not provided. Please provide a resource group name");
            }

            // Create resource group
            logger.info("Creating Resource Group");
            Utils.executeCommand(logger, String.format("az group create --name %s --location %s", resourceGroup, location));
            // Perform deployment
            logger.info("Performing deployment");
            Utils.executeCommand(logger, String.format("az mesh deployment create --resource-group %s --input-yaml-file-paths %s  --parameters \"{'location': {'value': '%s'}}\"", resourceGroup, inputYamlFilePaths, location));
            TelemetryHelper.sendEvent(TelemetryEventType.DEPLOYMESH, String.format("Deployed application on mesh"), logger);
        }
        else{
            throw new MojoFailureException(String.format("%s deployment type is not valid", deploymentType));
        }
    }
}