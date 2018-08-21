package com.microsoft.servicefabric;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.apache.maven.plugins.annotations.LifecyclePhase;

/**
 * Goal which adds a service resources to a project.
 */
@Mojo( name = "addservice", defaultPhase = LifecyclePhase.PROCESS_RESOURCES )
public class AddServiceMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "applicationName", required = true)
    String applicationName;

    @Parameter(property = "serviceName", required = true)
    String serviceName;

    @Parameter(property = "imageName", required = true)
    String imageName;

    @Parameter(property = "serviceDescription", defaultValue = Constants.DefaultServiceDescription)
    String serviceDescription;

    @Parameter(property = "osType", defaultValue = Constants.DefaultOS)
    String osType;
    
    @Parameter(property = "codePackageName", defaultValue = Constants.DefaultCodePackageName)
    String codePackageName;

    @Parameter(property = "listenerName", defaultValue = Constants.DefaultListenerName)
    String listenerName;
    
    @Parameter(property = "listenerPort", defaultValue = Constants.DefaultPortNumber)
    String listenerPort;

    @Parameter(property = "cpuUsage", defaultValue = Constants.DefaultCpuUsage)
    String cpuUsage;

    @Parameter(property = "memoryUsage", defaultValue = Constants.DefaultMemoryUsage)
    String memoryUsage;

    @Parameter(property = "replicaCount", defaultValue = Constants.DefaultReplicaCount)
    String replicaCount;    

    @Parameter(property = "networkRef", defaultValue = Constants.DefaultNetworkRefName)
    String networkRef; 

    private Log logger  = getLog();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        String resourceDirectory = Utils.getResourcesDirectory(logger, project);
        String serviceFabricResourcesDirectory = Utils.getPath(resourceDirectory, "servicefabric");
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            logger.error("Service fabric resources folder does not exist. Please run init goal before running this goal!");
            return;
        }
        else{
            if(!Utils.checkIfExists(Utils.getPath(serviceFabricResourcesDirectory, applicationName + ".yaml"))){
                logger.error("Application resource with the specified name does not exist");
                return;
            }
            if(Utils.checkIfExists(Utils.getPath(serviceFabricResourcesDirectory, serviceName + ".yaml"))){
                logger.error("Service resource with the specified name already exists");
                return;
            }
            logger.debug(String.format("Using the following directory: %s", resourceDirectory));
            try {
                InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.ServiceResourceName);
                logger.debug(String.format("Reading content form the %s", resource));
                String serviceContent = IOUtil.toString(resource, "UTF-8"); 
                serviceContent = Utils.replaceString(logger, serviceContent, "APP_NAME", applicationName, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "SERVICE_NAME", serviceName, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "SERVICE_DESCRIPTION", serviceDescription, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "OS_TYPE", osType, Constants.ServiceResourceName);
                if(codePackageName.equals(Constants.DefaultCodePackageName)){
                    codePackageName = serviceName + "CodePackage";
                }
                serviceContent = Utils.replaceString(logger, serviceContent, "CODE_PACKAGE_NAME", codePackageName, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "DOCKER_IMAGE", imageName, Constants.ServiceResourceName);
                if(listenerName.equals(Constants.DefaultListenerName)){
                    listenerName = serviceName + "Listener";
                }
                serviceContent = Utils.replaceString(logger, serviceContent, "LISTENER_NAME", listenerName, Constants.ServiceResourceName);
                if(listenerPort.equals(Constants.DefaultPortNumber)){
                    Random rand = new Random();
                    int port = rand.nextInt(65535 - 49152 + 1) + 49152; 
                    listenerPort = Integer.toString(port); 
                }
                serviceContent = Utils.replaceString(logger, serviceContent, "LISTENER_PORT", listenerPort, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "CPU_USAGE", cpuUsage, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "MEMORY_USAGE", memoryUsage, Constants.ServiceResourceName);
                serviceContent = Utils.replaceString(logger, serviceContent, "REPLICA_COUNT", replicaCount, Constants.ServiceResourceName);
                if(networkRef.equals(Constants.DefaultNetworkRefName)){
                    networkRef = applicationName + "Network";
                }
                serviceContent = Utils.replaceString(logger, serviceContent, "NETWORK_NAME", networkRef, Constants.ServiceResourceName);
                FileUtils.fileWrite(Utils.getPath(serviceFabricResourcesDirectory, serviceName + ".yaml"), serviceContent);
                logger.debug("Wrote content to output");
            } catch (IOException e) {
                logger.error("Error while writing output");
                logger.error(e);
                return;
            }
    
        }
	}
}