package com.microsoft.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.logging.Log;

@Mojo( name = "removelocal", defaultPhase = LifecyclePhase.NONE )
public class RemoveLocalMojo extends AbstractMojo
{
    @Parameter(property = "applicationName", required = true)
    String applicationName;

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        Utils.checksfctlinstallation(logger);
        Utils.executeCommand(logger, "sfctl mesh app delete --application-resource-name " + applicationName);
	}
}