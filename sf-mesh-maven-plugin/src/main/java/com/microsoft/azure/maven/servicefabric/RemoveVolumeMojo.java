package com.microsoft.azure.maven.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal which removes the mesh volume from local cluster, SFRP or mesh
 */
@Mojo( name = "removevolume", defaultPhase = LifecyclePhase.NONE )
public class RemoveVolumeMojo extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Type of deployment local or cloud or sfrp
    */
    @Parameter(property = "deploymentType", defaultValue = Constants.LOCAL_DEPLOYMENT_TYPE)
    String deploymentType;

    /**
     * Name of the volume
    */
    @Parameter(property = "volumeName", required = true)
    String volumeName;

    /**
     * URL of the cluster from which the volume needs to be removed. Only valid in local/sfrp deployment type.
    */
    @Parameter(property = "clusterEndpoint", defaultValue = Constants.DEFAULT_CLUSTER_ENDPOINT)
    String clusterEndpoint;

    /**
     * Resource Group name for mesh deployment. Only valid in mesh deployment type
    */
    @Parameter(property = "resourceGroup", defaultValue = Constants.DEFAULT_RESOURCE_GROUP)
    String resourceGroup;

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
        if(deploymentType.equalsIgnoreCase(Constants.LOCAL_DEPLOYMENT_TYPE)){
            Utils.checksfctlinstallation(logger);
            Utils.connecttounsecurecluster(logger, clusterEndpoint);
            Utils.executeCommand(logger, "sfctl mesh volume delete --volume-resource-name " + volumeName);
            TelemetryHelper.sendEvent(TelemetryEventType.REMOVEVOLUME, String.format("Removed volume %s locally", volumeName), logger);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.SFRP_DEPLOYMENT_TYPE)){
            if(pemFilePath.equalsIgnoreCase(Constants.DEFAULT_PEM_FILE_PATH)){
                throw new MojoFailureException("PEM file path is not provided. Please provide location to the pem file for connecting to SFRP cluster");    
            }
            Utils.checksfctlinstallation(logger);
            Utils.connecttosecurecluster(logger, clusterEndpoint, pemFilePath);
            Utils.executeCommand(logger, "sfctl mesh volume delete --volume-resource-name " + volumeName);
            TelemetryHelper.sendEvent(TelemetryEventType.REMOVEVOLUME, String.format("Removed volume from SFRP"), logger); 
        }
        else if(deploymentType.equalsIgnoreCase(Constants.MESH_DEPLOYMENT_TYPE)){
            if(resourceGroup.equalsIgnoreCase(Constants.DEFAULT_RESOURCE_GROUP)){
                throw new MojoFailureException("Resource Group is not mentioned. Please mention the resource group in which your volume is deployed");    
            }
            logger.info("Deleting volume");
            Utils.executeCommand(logger, String.format("az mesh volume delete --name %s --resource-group %s --yes", volumeName, resourceGroup));
            TelemetryHelper.sendEvent(TelemetryEventType.REMOVEVOLUME, String.format("Removed volume %s from mesh", volumeName), logger);
        }
        else{
            throw new MojoFailureException(String.format("%s deployment type is not valid", deploymentType));
        }
    }
}