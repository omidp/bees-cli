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
