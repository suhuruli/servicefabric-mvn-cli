package com.microsoft.servicefabric;

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

    public Log logger  = getLog();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        Utils.checksfctlinstallation(logger);
        if(filePathsOrDirectory.equals(Constants.DefaultResourcePath)){
            filePathsOrDirectory = Utils.getServicefabricResourceDirectory(logger, project);
        }
        Utils.executeCommand(logger, "sfctl mesh deployment create --file-paths-or-directory " + filePathsOrDirectory);
	}
}