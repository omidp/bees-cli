/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk;

import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.maven.RemoteRepositoryDecorator;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * {@link RemoteRepositoryDecorator} that configures proxy from {@link BeesClientConfiguration}.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class RemoteRepositoryDecoratorImpl extends RemoteRepositoryDecorator {
    @Inject
    BeesClientFactory bees;

    @Override
    public RemoteRepository decorate(RemoteRepository repo) {
        try {
            BeesClientConfiguration bcc = bees.get().getBeesClientConfiguration();

            if (bcc==null)  return repo;

            if (bcc.getProxyHost() != null && bcc.getProxyPort() > 0) {
                String proxyType = Proxy.TYPE_HTTP;
                if (repo.getUrl().startsWith("https"))
                    proxyType = Proxy.TYPE_HTTPS;
                Proxy proxy = new Proxy(proxyType, bcc.getProxyHost(), bcc.getProxyPort(), null);
                if (bcc.getProxyUser() != null) {
                    Authentication authentication = new Authentication(bcc.getProxyUser(), bcc.getProxyPassword());
                    proxy.setAuthentication(authentication);
                }
                repo.setProxy(proxy);
            }
            return repo;
        } catch (IOException e) {
            throw (Error)new Error("Failed to configure remote repository").initCause(e);
        }
    }
}
