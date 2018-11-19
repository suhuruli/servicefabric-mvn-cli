# Maven Plugin for Service Fabric Mesh
[![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-functions-maven-plugin.svg)]()

#### Table of Content
  - [Prerequisites](#prerequisites)
  - [Goals](#goals)
      - [sf-mesh:init](#sf-meshinit)
      - [sf-mesh:add-service](#sf-meshaddservice)
      - [sf-mesh:add-network](#sf-meshaddnetwork)
      - [sf-mesh:add-gateway](#sf-meshaddgateway)
      - [sf-mesh:add-secret](#sf-meshaddsecret)
      - [sf-mesh:add-secretvalue](#sf-meshaddsecretvalue)
      - [sf-mesh:add-volume](#sf-meshaddvolume)
      - [sf-mesh:remove-service](#sf-meshremoveservice)
      - [sf-mesh:remove-network](#sf-meshremovenetwork)
      - [sf-mesh:remove-gateway](#sf-meshremovegateway)
      - [sf-mesh:remove-secret](#sf-meshremovesecret)
      - [sf-mesh:remove-secretvalue](#sf-meshremovesecretvalue)
      - [sf-mesh:remove-volume](#sf-meshremovevolume)
      - [sf-mesh:deploy](#sf-meshdeploy)
  - [Usage](#usage)
  - [Common Configuration](#common-configuration)
  - [Configurations](#configurations)
  - [How-To](#how-to)
    - [Initialize Maven project for Mesh](#init-sf-mesh)
    - [Add a new resource to the project](#add-new-resource-to-current-project)
    - [Run the application locally](#run-application-locally)
    - [Deploy to Azure Mesh](#deploy-to-azure-mesh)

## Goals

#### `sf-mesh:init`
- Generates a `servicefabric` folder which contains an `appresources` folder which has the `application.yaml` file. 
- You will be prompted to enter the following parameters. 
    - -DapplicationName: A string that represents the application name 

#### `sf-mesh:addservice`
- Creates a folder under `servicefabric` which stores a services YAML file. 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:addnetwork`
- Will create a `network` YAML with the provided network name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:addgateway`
- Will create a `gateway` YAML with the provided gateway name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:addsecret`
- Will create a `secret` YAML with the provided secret name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:addsecretvalue`
- Will create a `secretvalue` YAML with the provided secretvalue name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:removeservice`
- Will remove the services folder with the provided name under the `servicefabric` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:removenetwork`
- Will remove the `network` YAML with the provided volume name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:removegateway`
- Will remove the `gateway` YAML with the provided gateway name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:removesecret`
- Will remove the `secret` YAML with the provided secret name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:removesecretvalue`
- Will remove the `secretvalue` YAML with the provided secretvalue name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:removevolume`
- Will create a `volume` YAML with the provided volume name under the `appresources` folder 
- You will be prompted to enter the parameters that are required for the comamnd. 

#### `sf-mesh:deploy`
- Will create a folder (`local` or `cloud`) which contains the deployment JSONs for the application 
- Deploys application to the local cluster or to the Azure Service Fabric Mesh environment 
- You will be prompted to enter the parameters that are required for the comamnd. 
 

## Usage

To use the Maven plugin in your Maven Java app, add the following snippet to your pom.xml file:

```XML
<project>
  ...
  <build>
    ...
    <plugins>
      ...
      <plugin>
        <groupId>com.microsoft.azure</groupId>
          <artifactId>azure-sf-maven-plugin</artifactId>
          <version>1.0.0-beta</version>
          <configuration>
            ...
          </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

## Common Configuration

This Maven plugin supports common configurations of all Maven Plugins for Azure.
Detailed documentation of common configurations is at [here](../docs/common-configuration.md).

## Configurations

This Maven Plugin supports the following configuration properties:

Property | Required | Description
---|---|---
`<resourceGroup>` | true | Specifies the Azure Resource Group for your Azure Functions.

## How-To

### Initialize Maven project with Mesh resource
Run below command to create an application resource 

```cmd
mvn sf-mesh:init -DapplicationName=TodoApp
```

### Add a new network to your application 
Run the command below to create a network. 

```cmd
mvn sf-mesh:addnetwork -DapplicationName=TodoApp -DserviceName=TodoAppSvc -DingressPort=8080 -DnetworkRef=TodoAppNetwork -DlistenerName=TodoAppSvcListener
```

- In folder `servicefabric->appresources`
- Named `network_TodoAppNetwork`

You don't have to provide all the properties on the command line. Missing properties will be prompted for input during the execution of the goal.

### Add a new service to your application 
Run the command below to create a service. 

```cmd
mvn sf-mesh:addservice -DapplicationName=TodoApp -DserviceName=TodoAppSvc -DimageName=suhuruli/todo-app-java-on-azure:1.0-SNAPSHOT -DlistenerPort=8080 -DnetworkRef=TodoAppNetwork -DmemoryUsage=2.0
```

- In folder `servicefabric->TodoAppSvc`
- Named `service_TodoAppSvc`
- References `TodoAppNetwork`

You don't have to provide all the properties on the command line. Missing properties will be prompted for input during the execution of the goal.


### Run the application locally 

With the help of goal `sf-mesh:deploy`, you can run the application locally using the command below:

```cmd
mvn sf-mesh:deploy -DdeploymentType=local 
```

This assumes you have a local Service Fabric cluster up and running. This is currently supported only on [Windows](service-fabric-mesh-howto-setup-developer-environment-sdk) until the local development cluster for resource based applications is available on Mac and Linux. 

### Deploy applications to Azure Mesh 

Directly deploy to target Mesh by running

```cmd
mvn sf-mesh:deploy -DdeploymentType=cloud -DresourceGroup=todoapprg -Dlocation=eastus
```

- Creates a resource group called `todoapprg` if it does not exist