package com.microsoft.servicefabric;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

@Mojo(name = "addgateway", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class AddGatewayMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * schema version of the gateway yaml to be generated
    */
    @Parameter(property = "schemaVersion", defaultValue = Constants.DefaultSchemaVersion)
    String schemaVersion;
    

    /**
     * Name of the gateway
    */
    @Parameter(property = "gatewayName", required = true)
    String gatewayName;

    /**
     * Description of the gateway
    */
    @Parameter(property = "gatewayDescription", defaultValue= Constants.DefaultGatewayDescription)
    String gatewayDescription;

    /**
     * Source network of gateway
    */
    @Parameter(property = "sourceNetwork", required = true)
    String sourceNetwork;

    /**
     * Destination network of gateway
    */
    @Parameter(property = "destinationNetwork", required = true)
    String destinationNetwork;

    /**
     * Name of the exposed endpoint
    */
    @Parameter(property = "tcpName", defaultValue = Constants.DefaultTcpName)
    String tcpName;

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
     * Name of the listener
    */
    @Parameter(property = "listenerName", required = true)
    String listenerName;
    
    /**
     * Port to be on the listener of the service
     */
    @Parameter(property = "tcpPort", required = true)
    String tcpPort;

    public Log logger  = getLog();

    @Override
    public void execute() throws MojoFailureException {

        String serviceFabricResourcesDirectory = Utils.getServicefabricResourceDirectory(logger, project);
		String appResourcesDirectory = Utils.getAppResourcesDirectory(logger, project);

        if(!Utils.checkIfExists(serviceFabricResourcesDirectory)){
        	throw new MojoFailureException("Service fabric resources folder does not exist. Please run init goal before running this goal!");
        }
        else{
            if(Utils.checkIfExists(Utils.getPath(appResourcesDirectory, "gateway_" + gatewayName + ".yaml"))){
                throw new MojoFailureException("Gateway Resource with the specified name already exists");
            }
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(Constants.GatewayResourceName);

            try{
                String gatewayContent = IOUtil.toString(resource, "UTF-8");
                gatewayContent = Utils.replaceString(logger, gatewayContent, "GATEWAY_NAME", gatewayName, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "GATEWAY_DESCRIPTION", gatewayDescription, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "SOURCE_NETWORK", sourceNetwork, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "DESTINATION_NETWORK", destinationNetwork, Constants.GatewayResourceName);
                if(tcpName.equals(Constants.DefaultTcpName)){
                    tcpName = listenerName+ "Config";
                }
                gatewayContent = Utils.replaceString(logger, gatewayContent, "TCP_NAME", tcpName, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "PORT", tcpPort, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "APPLICATION_NAME", applicationName, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "SERVICE_NAME", serviceName, Constants.GatewayResourceName);
                gatewayContent = Utils.replaceString(logger, gatewayContent, "LISTENER_NAME", listenerName, Constants.GatewayResourceName);
                FileUtils.fileWrite(Utils.getPath(appResourcesDirectory, "gateway_" + gatewayName + ".yaml"), gatewayContent);
				logger.debug("Wrote content to output");
                TelemetryHelper.sendEvent(TelemetryEventType.ADDGATEWAY, String.format("Added gateway with name: %s", gatewayName), logger);
            }
            catch (IOException e) {
				logger.error(e);
				throw new MojoFailureException("Error while writing output");
			} 
        }
	}
}