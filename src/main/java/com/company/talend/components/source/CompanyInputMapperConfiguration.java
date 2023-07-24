package com.company.talend.components.source;

import java.io.Serializable;
import java.util.List;

import com.company.talend.components.dataset.CustomDataset;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Structure;
import org.talend.sdk.component.api.meta.Documentation;

@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "dataset" }),
    @GridLayout.Row({ "outgoing" })
})
@Documentation("TODO fill the documentation for this configuration")
public class CompanyInputMapperConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private CustomDataset dataset;

    @Option
    @Documentation("TODO fill the documentation for this parameter")
    @Structure(type = Structure.Type.OUT)
//    private List<SchemaInfo> outgoing;
    private List<String> outgoing;

    public CustomDataset getDataset() {
        return dataset;
    }

    public CompanyInputMapperConfiguration setDataset(CustomDataset dataset) {
        this.dataset = dataset;
        return this;
    }

//    public List<SchemaInfo> getOutgoing() {
//        return outgoing;
//    }
    public List<String> getOutgoing() {
        return outgoing;
    }

//    public CompanyInputMapperConfiguration setOutgoing(List<SchemaInfo> outgoing) {
    public CompanyInputMapperConfiguration setOutgoing(List<String> outgoing) {
        this.outgoing = outgoing;
        return this;
    }

}