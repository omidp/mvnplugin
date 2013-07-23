package org.omidbiz.sample.plugin;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * @author omidp
 * 
 */
@Mojo(name = "resolve")
public class OmidbizMojo extends AbstractMojo
{

    private static final File DEFAULT_REPO_DIR = new File(System.getProperty("user.home"), ".omidbiz");

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    public void execute() throws MojoExecutionException
    {

        if (!DEFAULT_REPO_DIR.exists())
        {
            DEFAULT_REPO_DIR.mkdir();
        }

        ArtifactRequest cliArtifact = new ArtifactRequest();
        cliArtifact.setArtifact(new DefaultArtifact("commons-cli:commons-cli:1.2"));
        cliArtifact.setRepositories(remoteRepos);
        //
        ArtifactRequest ioArtifact = new ArtifactRequest();
        ioArtifact.setArtifact(new DefaultArtifact("commons-io:commons-io:2.4"));
        ioArtifact.setRepositories(remoteRepos);
      
        //
        ArtifactRequest httpClient = new ArtifactRequest();
        httpClient.setArtifact(new DefaultArtifact("org.apache.httpcomponents:httpclient:4.2.5"));
        httpClient.setRepositories(remoteRepos);

        //
        Dependency dependency = new Dependency(httpClient.getArtifact(), "compile");
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        for (RemoteRepository repoItem : remoteRepos)
        {
            collectRequest.addRepository(repoItem);
        }

        List<ArtifactResult> result = new ArrayList<ArtifactResult>();
        try
        {
            DependencyNode node = repoSystem.collectDependencies(repoSession, collectRequest).getRoot();
            DependencyRequest dr = new DependencyRequest();
            dr.setRoot(node);
            DependencyResult resolveDependencies = repoSystem.resolveDependencies(repoSession, dr);
            result = repoSystem.resolveArtifacts(repoSession, Arrays.asList(cliArtifact, ioArtifact, httpClient));
            result.addAll(resolveDependencies.getArtifactResults());

        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        catch (DependencyResolutionException e)
        {
            e.printStackTrace();
        }
        catch (DependencyCollectionException e)
        {
            e.printStackTrace();
        }
        for (ArtifactResult artifactResult : result)
        {
            getLog().info("Resolved artifact " + artifactResult.getArtifact().getFile() + " from " + artifactResult.getRepository());

            try
            {
                FileUtils.copyFileToDirectory(artifactResult.getArtifact().getFile(), DEFAULT_REPO_DIR);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
