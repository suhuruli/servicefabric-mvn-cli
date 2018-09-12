package com.microsoft.servicefabric;

import java.awt.List;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;


@Mojo(name = "addnetwork", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class AddNetworkMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    
    /**
     * Name of the network
    */
    @Parameter(property = "networkRef", required = true)
    String networkRef;
    
    /**
     * Name of the network
    */
    @Parameter(property = "networkDescription", defaultValue= Constants.DefaultNetworkDescription)
    String networkDescription;

    /**
     * Address prefix of the subnet
     */
    @Parameter(property = "addressPrefix", defaultValue= Constants.DefaultAddressPrefix)
    String addressPrefix;
    
    /**
     * Public IP port of Ingress
     */
    @Parameter(property = "ingressPort", defaultValue= Constants.DefaultPortNumber)
    String ingressPort;

    /**
     * Name of the application which uses this network
     */
    @Parameter(property = "applicationName", required= true)
    String applicationName;

    /**
     * List of comma seperated services  w.r.t the application which uses this network
     */
    @Parameter(property = "serviceName", required= true)
    String serviceName;

    /**
     * List of comma seperated endpoints of the services which is being exposed. This should be of the same order as the serviceNameList
     */
    @Parameter(property = "listenerName", required= true)
    String listenerName;

    public Log logger  = getLog();
	
	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
		String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
        	throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        else{
            if(Utils.checkIfExists(Utils.getPath(appResourcesDirectory, "network_" + networkRef + ".yaml"))){
                throw new MojoFailureException("Network Resource with the specified name already exists");
            }
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.NetworkResourceName);
            try {
                String networkContent = IOUtil.toString(resource, "UTF-8");
                networkContent = Utils.replaceString(logger, networkContent, "NETWORK_NAME", networkRef, Constants.NetworkResourceName);
                networkContent = Utils.replaceString(logger, networkContent, "NETWORK_DESCRIPTION", networkDescription, Constants.NetworkResourceName);
                networkContent = Utils.replaceString(logger, networkContent, "ADDRESS_PREFIX", addressPrefix, Constants.NetworkResourceName);
                if(ingressPort.equals(Constants.DefaultPortNumber)){
                    Random rand = new Random();
                    int port = rand.nextInt(65535 - 49152 + 1) + 49152; 
                    ingressPort = Integer.toString(port); 
                }
                String[] serviceNameList= serviceName.split(",");
                String[] listenerNameList= listenerName.split(",");
                if(serviceNameList.length != listenerNameList.length){
                    throw new MojoFailureException("The serviceNames and listenerNames length are not matching");
                }
                String ingressContentPrefix="    ingressConfig:\n" +
                "      layer4:\n";
                networkContent += ingressContentPrefix;
                for(int i=0; i<serviceNameList.length; i++){
                    String serviceName = serviceNameList[i];
                    String listenerName = listenerNameList[i];
                    String ingressContent = "        - name: INGRESS_NAME\n" +
                        "          publicPort: INGRESS_PORT_NO\n" +
                        "          applicationName: APPLICATION_NAME\n" +
                        "          serviceName: SERVICE_NAME\n" +
                        "          endpointName: ENDPOINT_NAME\n";
                    networkContent += ingressContent;
                    networkContent = Utils.replaceString(logger, networkContent, "INGRESS_PORT_NO", ingressPort, Constants.NetworkResourceName);
                    networkContent = Utils.replaceString(logger, networkContent, "APPLICATION_NAME", applicationName, Constants.NetworkResourceName);
                    networkContent = Utils.replaceString(logger, networkContent, "SERVICE_NAME", serviceName, Constants.NetworkResourceName);
                    networkContent = Utils.replaceString(logger, networkContent, "ENDPOINT_NAME", listenerName, Constants.NetworkResourceName);
                    networkContent = Utils.replaceString(logger, networkContent, "INGRESS_NAME", listenerName, Constants.NetworkResourceName);
                }
                FileUtils.fileWrite(Utils.getPath(appResourcesDirectory, "network_" + networkRef + ".yaml"), networkContent);
				logger.debug("Wrote content to output");

            }
            catch (IOException e) {
				logger.error(e);
				throw new MojoFailureException("Error while writing output");
			} 
        }
    }
}