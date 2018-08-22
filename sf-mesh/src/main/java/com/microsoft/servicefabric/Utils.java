package com.microsoft.servicefabric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;

public class Utils
{
    public static void createDirectory(Log logger ,String directoryPath) throws MojoFailureException{
        try {
            Files.createDirectory(Paths.get(directoryPath));
        } catch (IOException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("Error while creating directory %s", directoryPath));
        }
    }

    public static boolean checkIfExists(String path){
            if(Files.exists(Paths.get(path))){
                return true;
            }
            else{
                return false;
            }
    }

    public static String replaceString(Log logger, String content, String originalString, String replaceString, String resourceFileName){
        logger.debug(String.format("replacing %s with %s in %s", originalString, replaceString, resourceFileName));
        return content.replace(originalString, replaceString);
    }

    public static String getResourcesDirectory(Log logger, MavenProject project){
        String resourceDirectory;
        List<Resource> resources = project.getResources();
        logger.debug(String.format("No of resources present %d", resources.size()));
        if(resources.size() == 0){
            resourceDirectory = project.getBasedir().toString().concat(Constants.DefaultResourcePath);
            logger.debug("resource directory is not present so will be creating one");
        }
        else{
            resourceDirectory = resources.get(0).getDirectory();
        }
        return resourceDirectory;
    }

    public static String getServicefabricResourceDirectory(Log logger, MavenProject project){
        return Paths.get(getResourcesDirectory(logger, project), "servicefabric").toString();
    }

    public static String getPath(String directoryPath, String fileOrDirName){
        return Paths.get(directoryPath, fileOrDirName).toString();
    }

    public static String executeCommand(Log logger,String command) throws MojoFailureException, MojoExecutionException{
        try {
            logger.info(String.format("Executing command %s", command));
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            String stderr = IOUtil.toString(p.getErrorStream(), "UTF-8");
            String stdout = IOUtil.toString(p.getInputStream(), "UTF-8");
            logger.debug(String.format("STDOUT: %s", stdout));
            if(stderr != null && stderr.length() > 0 ){
                logger.error(String.format("STDERR: %s", stderr));
                throw new MojoFailureException(String.format("Error while running the %s command", command));
            }
            return stdout;
		} catch (IOException e){
            logger.error(e);
            throw new MojoExecutionException(String.format("Error while running the %s command", command));
        } catch (InterruptedException e) {
            logger.error(e);
            throw new MojoExecutionException(String.format("Interrupted while running command %s", command));
        }
    }

    public static void checksfctlinstallation(Log logger) throws MojoFailureException, MojoExecutionException{
        Utils.executeCommand(logger, "sfctl --help");
    }

    public static void connecttolocalcluster(Log logger, String ipString, String port) throws MojoFailureException, MojoExecutionException{
        Utils.executeCommand(logger, "sfctl cluster select --endpoint " + "http://" + ipString + ":" + port);
    }
}