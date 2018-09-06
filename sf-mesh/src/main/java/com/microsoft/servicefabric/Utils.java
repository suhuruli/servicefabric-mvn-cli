package com.microsoft.servicefabric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public static String getServicefabricResourceDirectory(Log logger, MavenProject project) throws MojoFailureException {
        return Paths.get(project.getBasedir().toString(), "servicefabric").toString();
    }

    public static String getAppResourcesDirectory(Log logger, MavenProject project) throws MojoFailureException {
        return Paths.get(getServicefabricResourceDirectory(logger, project), "appresources").toString();
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