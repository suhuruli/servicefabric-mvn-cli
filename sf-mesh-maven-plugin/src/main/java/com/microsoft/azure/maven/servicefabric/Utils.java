package com.microsoft.azure.maven.servicefabric;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.rmi.CORBA.Util;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
public class Utils
{
    enum ResourceType{
        application, volume, network;
    }
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

    public static void deleteFileOrDirectory(String path, Log logger) throws MojoFailureException {
        try {
            FileUtils.deleteDirectory(path);
        } catch (IOException e) {
            logger.error("Directory deletion failed");
            throw new MojoFailureException(String.format("Error while deleting directory %s", path));
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

    public static String executeCommand(Log logger,String command) throws MojoFailureException{
        try {
            logger.info(String.format("Executing command %s", command));
            Process p;
            if(Utils.isWindows()){
                p = Runtime.getRuntime().exec("cmd.exe /C" +command);
            }
            else{
                p = Runtime.getRuntime().exec(command);
            }
            p.waitFor();
            int exitCode = p.exitValue();
            String stderr = IOUtil.toString(p.getErrorStream(), "UTF-8");
            String stdout = IOUtil.toString(p.getInputStream(), "UTF-8");
            logger.debug(String.format("STDOUT: %s", stdout));
            if(stderr != null && stderr.length() > 0 ){
                if(exitCode != 0){
                    logger.error(String.format("Process exited with exit code %d", exitCode));
                    logger.error(String.format("If STDERR: %s", stderr));
                    throw new MojoFailureException(String.format("Error while running the %s command", command));
                }
                else{
                    logger.info(String.format("Else STDERR: %s", stderr));
                }
            }
            return stdout;
		} catch (IOException e){
            logger.error(e);
            throw new MojoFailureException(String.format("Error while running the %s command", command));
        } catch (InterruptedException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("Interrupted while running command %s", command));
        }
    }

    public static String executeCommand(Log logger,String[] command) throws MojoFailureException{
        try {
            logger.info(String.format("Executing command %s", Arrays.toString(command)));
            Process p;
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            int exitCode = p.exitValue();
            String stderr = IOUtil.toString(p.getErrorStream(), "UTF-8");
            String stdout = IOUtil.toString(p.getInputStream(), "UTF-8");
            logger.debug(String.format("STDOUT: %s", stdout));
            if(stderr != null && stderr.length() > 0 ){
                if(exitCode != 0){
                    logger.error(String.format("Process exited with exit code %d", exitCode));
                    logger.error(String.format("STDERR: %s", stderr));
                    throw new MojoFailureException(String.format("Error while running the %s command", Arrays.toString(command)));
                }
                else{
                    logger.info(String.format("STDERR: %s", stderr));
                }
            }
            return stdout;
		} catch (IOException e){
            logger.error(e);
            throw new MojoFailureException(String.format("Error while running the %s command", Arrays.toString(command)));
        } catch (InterruptedException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("Interrupted while running command %s", Arrays.toString(command)));
        }
    }
    public static void checksfctlinstallation(Log logger) throws MojoFailureException{
        if(Utils.isWindows()){
            Utils.executeCommand(logger, "sfctl --help  > NUL 2>&1");
        }
        else{
            Utils.executeCommand(logger, "sfctl --help >> /dev/null 2>&1");
        }
    }

    public static void checkazinstallation(Log logger) throws MojoFailureException{
        if(Utils.isWindows()){
            Utils.executeCommand(logger, "az mesh --help > NUL 2>&1");
        }
        else{
            Utils.executeCommand(logger, "az mesh --help >> /dev/null 2>&1");
        }
    }

    public static void connecttolocalcluster(Log logger, String ipString, String port) throws MojoFailureException{
        Utils.executeCommand(logger, "sfctl cluster select --endpoint " + "http://" + ipString + ":" + port);
    }

    public static void checkdotnetinstallation(Log logger) throws MojoFailureException{
        Utils.executeCommand(logger, "dotnet --version");
    }

    public static String checkmergetoolpresence(Log logger) throws MojoFailureException{
        String localRepoPath;
        if(Utils.isWindows()){
            localRepoPath = Utils.executeCommand(logger, "mvn help:evaluate -Dexpression=settings.localRepository -DforceStdout | findstr repository");
        }
        else{
            localRepoPath = Utils.executeCommand(logger, new String[]{"sh", "-c", "mvn help:evaluate -Dexpression=settings.localRepository -DforceStdout | grep repository"});
        }

        String toolPath = Utils.getPath(localRepoPath.replace("\n", "").replace("\r", ""), "SfSbzYamlMergeCore");
        Utils.checkIfExists(toolPath);
        return toolPath;
    }

    public static String listFilesAndFilesSubDirectories(Log logger, String directoryName){
        File directory = new File(directoryName);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        String inputFiles="";
        for (File file : fList){
            if (file.isFile()){
                String fileName = file.getName();
                int index = fileName.lastIndexOf('.');
                String fileExtension=null;
                if(index != -1){
                    fileExtension = fileName.substring(index);
                }
                if(!fileExtension.equals(".json")){
                    inputFiles = inputFiles.concat(String.format(" /inputFileName:\"%s\"", file.getAbsolutePath()));
                }
                logger.debug("Function" + inputFiles);
            } else if (file.isDirectory()){
                inputFiles = inputFiles.concat(listFilesAndFilesSubDirectories(logger, file.getAbsolutePath()));
                logger.debug("Function" + inputFiles);
            }
        }
        return inputFiles;
    }

    public static String getOS(){
     return System.getProperty("os.name").toLowerCase();   
    }
    public static boolean isWindows() {
		return (getOS().indexOf("win") >= 0);
	}
	public static boolean isLinux() {
        String OS = getOS();
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") >= 0|| OS.indexOf("mac") >= 0 );	
	}

    public static String getResourceName(String fileName){
        String resourceName=fileName.split("_")[3];
        return resourceName.substring(0, resourceName.length() - 5);
    }

    public static ResourceType getResourceType(String fileName){
        return ResourceType.valueOf(fileName.split("_")[2]);
    }
    public static void generatejsonfrommergetool(Log logger, String deploymentType, MavenProject project, String outputPath) throws MojoFailureException {
        String toolPath = Utils.checkmergetoolpresence(logger);
        String toolExePath = Utils.getPath(toolPath, "SfSbzYamlMerge");
        String outputFormat = null;
        String folderName = null;
        String inputFiles = listFilesAndFilesSubDirectories(logger, getServicefabricResourceDirectory(logger, project));
        logger.debug("inputFiles:" + inputFiles);
        if(deploymentType.equalsIgnoreCase(Constants.MeshDeploymentType)){
            outputFormat = "SF_SBZ_RP_JSON";
            folderName = "cloud";
        }
        else{
            outputFormat = "SF_SBZ_JSON";
            folderName = "local";
        }
        outputPath = Utils.getPath(Utils.getServicefabricResourceDirectory(logger, project), folderName);
        if(Utils.checkIfExists(outputPath)){
            Utils.deleteFileOrDirectory(outputPath, logger);
        }
        Utils.createDirectory(logger, outputPath);
        if(isWindows()){
            Utils.executeCommand(logger, String.format("%s.exe %s /outputFormat:%s /out:%s",toolExePath, inputFiles, outputFormat, outputPath));
        }
        else{
            Utils.executeCommand(logger, String.format("chmod a+x %s", toolExePath));
            Utils.executeCommand(logger, new String[]{"sh", "-c", String.format("%s %s /outputFormat:%s /out:%s",toolExePath, inputFiles, outputFormat, outputPath)});
        }
    }

    public static String getResourceDescription(File file, Log logger) throws MojoFailureException {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(file);
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            logger.info(obj.get("description").toString());
            return obj.get("description").toString();

        } catch (FileNotFoundException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("%s File not found", file.getName()));
        } catch (IOException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("%s IOException", file.getName()));
        } catch (ParseException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("%s ParseException", file.getName()));
		}
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashMap<String, Object> stringToYaml(Log logger, String content) throws MojoFailureException {
        ObjectMapper oMapper = new ObjectMapper(new YAMLFactory());
        InputStream stream = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
        try {
            return oMapper.readValue(stream, LinkedHashMap.class);
        } catch (IOException e) {
            logger.error(e);
            throw new MojoFailureException(String.format("string to yaml conversion failed"));
        }
    }

    public static String yamlToString(LinkedHashMap<String, Object> yaml){
        StringWriter content = new StringWriter();
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml dumper = new Yaml (options);
        dumper.dump(yaml, content);
        return content.toString();
    }
}