package com.microsoft.servicefabric;

import javax.rmi.CORBA.Util;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.logging.Log;

@Mojo( name = "deploylocal", defaultPhase = LifecyclePhase.NONE )
public class DeployLocalMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "filePathsOrDirectory", defaultValue = Constants.DefaultResourcePath)
    String filePathsOrDirectory;

    @Parameter(property = "ipAddress", defaultValue = Constants.DefaultIPAddress)
    String ipAddress;

    @Parameter(property = "port", defaultValue = Constants.DefaultPort)
    String port;

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        Utils.checksfctlinstallation(logger);
        Utils.connecttolocalcluster(logger, ipAddress, port);
        if(filePathsOrDirectory.equals(Constants.DefaultResourcePath)){
            filePathsOrDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        }
        Utils.executeCommand(logger, "sfctl mesh deployment create --file-paths-or-directory " + filePathsOrDirectory);
	}
}