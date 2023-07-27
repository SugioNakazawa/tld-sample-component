package com.company.talend.components.output;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import com.company.talend.components.dataset.CustomDataset;

@GridLayout({
        // the generated layout put one configuration entry per line,
        // customize it as much as needed
        @GridLayout.Row({ "dataset" }),
        @GridLayout.Row({ "transactionMode" })
})
@Documentation("TODO fill the documentation for this configuration")
public class CompanyOutputOutputConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private CustomDataset dataset;

    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private TransactionMode transactionMode = TransactionMode.Occ;

    public CustomDataset getDataset() {
        return dataset;
    }

    public CompanyOutputOutputConfiguration setDataset(CustomDataset dataset) {
        this.dataset = dataset;
        return this;
    }

    public TransactionMode getTransactionMode() {
        return transactionMode;
    }

    public CompanyOutputOutputConfiguration setTransactionMode(TransactionMode transactionMode) {
        this.transactionMode = transactionMode;
        return this;
    }

    public enum TransactionMode {
        Occ,
        Ltx,
        OccLtx
    }
}