package com.company.talend.components.service;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.schema.DiscoverSchema;

import com.company.talend.components.dataset.CustomDataset;

@Service
public class CompanyComponentService {

    // you can put logic here you can reuse in components
    @DiscoverSchema("CustomDataset")
    public Schema guessSchema(@Option final CustomDataset dataset) {
       // some code
    //    retrurn factory.newSchemaBuilder(Schema.Type.RECORD)
    //             .withEntry(factory.newEntryBuilder()
    //                     .withName("DataSetor")
    //                     .withType(Schema.Type.STRING)
    //                     .withNullable(true)
    //                     .build())
    //    // building some entries
    //             .withEntry(factory.newEntryBuilder()
    //                     .withName("effective_date")
    //                     .withType(Schema.Type.DATETIME)
    //                     .withNullable(true)
    //                     .withComment("Effective date of purchase")
    //                     .build())
    //             .build();
        System.out.println("CompanyComponentService#guessSchema");
        return null;
    }

}