package com.microsoft.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

@Mojo( name = "deploycloud", defaultPhase = LifecyclePhase.NONE)
public class DeployCloudMojo extends AbstractMojo
{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
	}
}