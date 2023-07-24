package com.company.talend.components.source;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Data
@GridLayout({ @GridLayout.Row({ "label", "originalDbColumnName", "key", "talendType", "nullable", "pattern" }) })
@GridLayout({ @GridLayout.Row({ "label" }) })
@Documentation("Schema definition.")
public class SchemaInfo implements Serializable {

    @Option
    @Documentation("Column name.")
    private String label;

    @Option
    @Documentation("Is it a Key column.")
    private String originalDbColumnName;

    @Option
    @Documentation("Is it a Key column.")
    private boolean key;

    @Option
    @Documentation("Talend type such as id_String.")
    private String talendType;

    @Option
    @Documentation("Is it a Nullable column.")
    private boolean nullable;

    @Option
    @Documentation("Pattern used for datetime processing.")
    private String pattern = "yyyy-MM-dd HH:mm";
}
