package com.company.talend.components.dataset;

import java.io.Serializable;

import com.company.talend.components.datastore.CustomDatastore;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Validable;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Code;
import org.talend.sdk.component.api.meta.Documentation;

@DataSet("CustomDataset")
@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "datastore" }),
    @GridLayout.Row({ "tableName" }),
    @GridLayout.Row({ "query" })
})
@Documentation("TODO fill the documentation for this configuration")
public class CustomDataset implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private CustomDatastore datastore;

    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private String tableName;

    @Option
    @Code("sql")
    @Validable("validateQuery")
    @Documentation("TODO fill the documentation for this parameter")
    private String query;

    public CustomDatastore getDatastore() {
        return datastore;
    }

    public CustomDataset setDatastore(CustomDatastore datastore) {
        this.datastore = datastore;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public CustomDataset setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public CustomDataset setQuery(String query) {
        this.query = query;
        return this;
    }
}