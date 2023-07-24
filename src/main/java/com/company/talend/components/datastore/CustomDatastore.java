package com.company.talend.components.datastore;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

@DataStore("CustomDatastore")
@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "url" }),
    @GridLayout.Row({ "username" }),
    @GridLayout.Row({ "password" })
})
@Documentation("TODO fill the documentation for this configuration")
public class CustomDatastore implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private String url;

    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private String username;

    @Credential
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private String password;

    public String getUrl() {
        return url;
    }

    public CustomDatastore setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public CustomDatastore setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public CustomDatastore setPassword(String password) {
        this.password = password;
        return this;
    }
}