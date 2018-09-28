package com.microsoft.servicefabric;

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
 * Goal which deploys the mesh application to local cluster or mesh
 */
@Mojo( name = "deploy", defaultPhase = LifecyclePhase.NONE )
public class DeployMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Type of deployment local or cloud
    */
    @Parameter(property = "deploymentType", defaultValue = Constants.LocalDeploymentType)
    String deploymentType;

    /**
     * Comma seperated resource files or the directory in which the resource files are present
    */
    @Parameter(property = "filePathsOrDirectory", defaultValue = Constants.ServiceFabricResourcesPath)
    String filePathsOrDirectory;

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
     * Location of the resource group. Only valid in cloud deployment type
    */
    @Parameter(property = "location", defaultValue = Constants.DefaultLocation)
    String location;

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        if(filePathsOrDirectory.equals(Constants.ServiceFabricResourcesPath)){
            filePathsOrDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        }
        Utils.generatejsonfrommergetool(logger, deploymentType, project, filePathsOrDirectory);
        if(deploymentType.equalsIgnoreCase(Constants.LocalDeploymentType)){
            /*Utils.checksfctlinstallation(logger);
            Utils.connecttolocalcluster(logger, ipAddress, port);
            Utils.executeCommand(logger, "sfctl mesh deployment create --file-paths-or-directory " + filePathsOrDirectory);*/
            listAndDeployResourcesLocal();
            TelemetryHelper.sendEvent(TelemetryEventType.DeployMojo, String.format("Deployed application locally"), logger);
        }
        else if(deploymentType.equalsIgnoreCase(Constants.CloudDeploymentType)){
            //To be implemented
            Utils.checkazinstallation(logger);

            if(resourceGroup.equals(Constants.DefaultResourceGroup)){
                throw new MojoFailureException("Resource group is not provided. Please provide a resource group name");
            }

            // Create resource group
            logger.info("Creating Resource Group");
            Utils.executeCommand(logger, String.format("az group create --name %s --location %s", resourceGroup, location));
            // Perform template deployment
            logger.info("Perform deployment");
            String path = Utils.getPath(Utils.getServicefabricResourceDirectory(logger, project), "cloud");
            File[] rpjson = new File(path).listFiles();
            if(Utils.isWindows()){
                Utils.executeCommand(logger, String.format("az mesh deployment create --resource-group %s --template-file %s  --parameters \"{'location': {'value': '%s'}}\"", resourceGroup, rpjson[0], location));
            }
            else{
                Utils.executeCommand(logger, new String[]{"sh", "-c", String.format("az mesh deployment create --resource-group %s --template-file %s  --parameters \"{'location': {'value': '%s'}}\"", resourceGroup, rpjson[0], location)});
            }
            TelemetryHelper.sendEvent(TelemetryEventType.DeployMojo, String.format("Deployed application on mesh"), logger);
        }
        else{
            throw new MojoFailureException(String.format("%s deployment type is not valid", deploymentType));
        }
    }
    
    private void  listAndDeployResourcesLocal() throws MojoFailureException {
        String path = Utils.getPath(Utils.getServicefabricResourceDirectory(logger, project), "local");
        File[] resources = new File(path).listFiles();
        Arrays.sort(resources);
        for(int i=0; i<resources.length; i++){
            File file = resources[i];
            logger.debug(String.format("Deploying Resource %s", file.getName()));
            OutputStream os = null;
            try {
                String urlString = null;
                String resourceName = Utils.getResourceName(file.getName());
                if(Utils.getResourceType(file.getName()).equals(Utils.ResourceType.application)){
                    urlString = String.format("http://%s:%s/Resources/Applications/%s?api-version=6.3-preview", ipAddress, port, resourceName);
                }
                else if(Utils.getResourceType(file.getName()).equals(Utils.ResourceType.volume)){
                    urlString = String.format("http://%s:%s/Resources/Volumes/%s?api-version=6.3-preview", ipAddress, port, resourceName);
                }
                else if(Utils.getResourceType(file.getName()).equals(Utils.ResourceType.network)){
                    // Ignore this as network resource deployment locally is not supported
                    continue;
                }
                logger.debug(String.format("Connecting to %s", urlString));
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                os = conn.getOutputStream();
                os.write(Utils.getResourceDescription(file, logger).getBytes());
                os.flush();
                String responseCode = Integer.toString(conn.getResponseCode());
                if(!responseCode.matches("2..")){
                    logger.debug("Got invalid response Code");
                    throw new MojoFailureException("Failed : HTTP error code : " + conn.getResponseCode());    
                }
                conn.disconnect();
                    
            } catch (IOException e) {
                logger.error(e);
                throw new MojoFailureException(String.format("Failed while deploying the resource %s", file.getName()));
			}
        }
    }
}