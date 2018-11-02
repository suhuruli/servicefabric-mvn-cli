package com.microsoft.azure.maven.servicefabric;

import java.io.IOException;
import java.io.InputStream;

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
     * schema version of the network yaml to be generated
    */
    @Parameter(property = "schemaVersion", defaultValue = Constants.DefaultSchemaVersion)
    String schemaVersion;

    /**
     * Name of the network
    */
    @Parameter(property = "networkName", required = true)
    String networkName;
    
    /**
     * Description of the network
    */
    @Parameter(property = "networkDescription", defaultValue= Constants.DefaultNetworkDescription)
    String networkDescription;

    /**
     * Kind of the network
     */
    @Parameter(property = "networkKind", defaultValue= Constants.DefaultNetworkKind)
    String networkKind;

    /**
     * Address prefix of the subnet
     */
    @Parameter(property = "networkAddressPrefix", required = true)
    String networkAddressPrefix;

    public Log logger  = getLog();
	
	@Override
	public void execute() throws MojoFailureException {
        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
		String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);
        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
        	throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        else{
            if(Utils.checkIfExists(Utils.getPath(appResourcesDirectory, "network_" + networkName + ".yaml"))){
                throw new MojoFailureException("Network Resource with the specified name already exists");
            }
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.NetworkResourceName);
            try {
                String networkContent = IOUtil.toString(resource, "UTF-8");
                networkContent = Utils.replaceString(logger, networkContent, "NETWORK_NAME", networkName, Constants.NetworkResourceName);
                networkContent = Utils.replaceString(logger, networkContent, "NETWORK_DESCRIPTION", networkDescription, Constants.NetworkResourceName);
                networkContent = Utils.replaceString(logger, networkContent, "ADDRESS_PREFIX", networkAddressPrefix, Constants.NetworkResourceName);
                networkContent = Utils.replaceString(logger, networkContent, "NETWORK_KIND", networkName, Constants.NetworkResourceName);
                FileUtils.fileWrite(Utils.getPath(appResourcesDirectory, "network_" + networkName + ".yaml"), networkContent);
				logger.debug("Wrote content to output");
                TelemetryHelper.sendEvent(TelemetryEventType.ADDNETWORK, String.format("Added network with name: %s", networkName), logger);
            }
            catch (IOException e) {
				logger.error(e);
				throw new MojoFailureException("Error while writing output");
			} 
        }
    }
}