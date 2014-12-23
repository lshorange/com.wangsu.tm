package com.tm;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.maa.agent.rewriter.Agent;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import com.sun.tools.attach.VirtualMachine;


/**
 * Goal which touches a timestamp file.
 *
 * @goal instrument
 *
 * @phase validate
 */
public class MyMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException
    {
        getLog().info("Hello world");

        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        String jarFilePath = null;

        getLog().info("[MAA] Dynamically Android Agent instrumentation...");
        try {
            jarFilePath = Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString();

            jarFilePath = new File(jarFilePath).getCanonicalPath();

            getLog().info("[MAA] Found Android Agent instrumentation within " + jarFilePath);
        } catch (URISyntaxException e) {
            getLog().error("[MAA] Unable to find Android Agent instrumentation jar");
            throw new RuntimeException(e);
        } catch (IOException e) {
            getLog().error("[MAA] Unable to find Android Agent instrumentation jar");
            throw new RuntimeException(e);
        }
        try {
            getLog().info("VirtualMachine.attach..." + pid);
            VirtualMachine vm = VirtualMachine.attach(pid);
            getLog().info("System.getProperty..."+ System.getProperty("Maa.AgentArgs"));
            vm.loadAgent(jarFilePath, System.getProperty("Maa.AgentArgs"));
            vm.detach();
        } catch (Exception e) {
            getLog().error("[MAA] Error encountered while loading the Android agent", e);
            throw new RuntimeException(e);
        }

    }
}
