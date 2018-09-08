package com.microsoft.servicefabric;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

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
 * Goal which adds a service resource to a project.
 */
@Mojo( name = "addservice", defaultPhase = LifecyclePhase.PROCESS_RESOURCES )
public class AddServiceMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Name of the application
    */
    @Parameter(property = "applicationName", required = true)
    String applicationName;

    /**
     * Name of the service
    */
    @Parameter(property = "serviceName", required = true)
    String serviceName;

    /**
     * Container image name of the service
    */
    @Parameter(property = "imageName", required = true)
    String imageName;

    /**
     * Description of the service
    */
    @Parameter(property = "serviceDescription", defaultValue = Constants.DefaultServiceDescription)
    String serviceDescription;

    /**
     * OS environment on which this service is deployed
    */
    @Parameter(property = "osType", defaultValue = Constants.DefaultOS)
    String osType;
    
    /**
     * Name of the code package
    */
    @Parameter(property = "codePackageName", defaultValue = Constants.DefaultCodePackageName)
    String codePackageName;

    /**
     * Name of the listener
    */
    @Parameter(property = "listenerName", defaultValue = Constants.DefaultListenerName)
    String listenerName;
    
    /**
     * Port to expose of the container
    */
    @Parameter(property = "listenerPort", defaultValue = Constants.DefaultPortNumber)
    String listenerPort;

    /**
     * Max CPU usage (in cores) of the container
    */
    @Parameter(property = "cpuUsage", defaultValue = Constants.DefaultCpuUsage)
    String cpuUsage;

    /**
     * Max Memory usage (in GB) of the container
    */
    @Parameter(property = "memoryUsage", defaultValue = Constants.DefaultMemoryUsage)
    String memoryUsage;

    /**
     * Replica count of the container
    */
    @Parameter(property = "replicaCount", defaultValue = Constants.DefaultReplicaCount)
    String replicaCount;    

    /**
     * Network resource reference in which the container should be deployed
    */
    @Parameter(property = "networkRef", defaultValue = Constants.DefaultNetworkRefName)
    String networkRef; 

    private Log logger  = getLog();

	@Override
	public void execute() throws MojoFailureException {
        addService();
    }
    
    public void addService() throws MojoFailureException{
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);
        String serviceDirectory = Utils.getPath(serviceFabricResourcesDirectory, serviceName);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
            throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        else{
            if(!Utils.checkIfExists(Utils.getPath(appResourcesDirectory, "app_" + applicationName + ".yaml"))){
                throw new MojoFailureException(String.format("Application resource with the name %s does not exist", applicationName));
            }
            if(Utils.checkIfExists(serviceDirectory)){
                throw new MojoFailureException("Resource with the specified name already exists");
            }
            try {
                InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.ServiceResourceName);
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
                Utils.createDirectory(logger, serviceDirectory);
                FileUtils.fileWrite(Utils.getPath(serviceDirectory, "service_" + serviceName + ".yaml"), serviceContent);
                logger.debug("Wrote content to output");
            } catch (IOException e) {
                logger.error(e);
                throw new MojoFailureException("Error while writing output");
            }
    
        }
    }
}