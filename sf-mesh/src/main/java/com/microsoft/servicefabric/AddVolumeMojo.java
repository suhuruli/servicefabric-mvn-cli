package com.microsoft.servicefabric;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

@Mojo( name = "addvolume", defaultPhase = LifecyclePhase.PROCESS_RESOURCES )
public class AddVolumeMojo extends AbstractMojo
{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
	}
}