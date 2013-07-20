package org.omidbiz.sample.hello;

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

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 *
 */
@Mojo(name = "touch")
public class MyMojo extends AbstractMojo
{

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    public void execute() throws MojoExecutionException
    {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact("commons-cli:commons-cli:1.2"));
        request.setRepositories(remoteRepos);

        getLog().info("Resolving artifact " + " from " + remoteRepos);

        ArtifactResult result;
        try
        {
            result = repoSystem.resolveArtifact(repoSession, request);
        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        getLog().info( "Resolved artifact "+ 
                result.getArtifact().getFile() + " from "
                + result.getRepository() );

    }
}
