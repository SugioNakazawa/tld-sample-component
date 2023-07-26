package com.company.talend.components.output;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.BeforeGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

import com.company.talend.components.service.CompanyComponentService;
import com.tsurugidb.iceaxe.TsurugiConnector;
import com.tsurugidb.iceaxe.metadata.TgTableMetadata;
import com.tsurugidb.iceaxe.session.TsurugiSession;
import com.tsurugidb.iceaxe.sql.TsurugiSqlPreparedStatement;
import com.tsurugidb.iceaxe.sql.parameter.TgBindParameters;
import com.tsurugidb.iceaxe.sql.parameter.TgBindVariables;
import com.tsurugidb.iceaxe.sql.parameter.TgParameterMapping;
import com.tsurugidb.iceaxe.transaction.manager.TgTmSetting;
import com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager;
import com.tsurugidb.iceaxe.transaction.option.TgTxOption;
import com.tsurugidb.tsubakuro.channel.common.connection.UsernamePasswordCredential;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions
            // you can add a migrationHandler
@Icon(value = CUSTOM, custom = "CompanyOutput") // icon is located at src/main/resources/icons/CompanyOutput.svg
@Processor(name = "CompanyOutput")
@Documentation("TODO fill the documentation for this processor")
public class CompanyOutputOutput implements Serializable {
    private final CompanyOutputOutputConfiguration configuration;
    private final CompanyComponentService service;

    private TsurugiSession session;
    private TgTableMetadata metadata;
    //  TODO この書き方で良いか？
    private TsurugiSqlPreparedStatement<TgBindParameters> ps;
    private TsurugiTransactionManager tm;

    public CompanyOutputOutput(@Option("configuration") final CompanyOutputOutputConfiguration configuration,
            final CompanyComponentService service) {
        this.configuration = configuration;
        this.service = service;
    }

    @PostConstruct
    public void init() {
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        // Note: if you don't need it you can delete it
        System.out.println("CompanyOutputOutput#init");

        var endpoint = URI.create("tcp://" + configuration.getDataset().getDatastore().getUrl());
        var credential = new UsernamePasswordCredential("user", "password");
        var connector = TsurugiConnector.of(endpoint, credential);
        var tableName = configuration.getDataset().getTableName();

        try {
            this.session = connector.createSession();
            var metaOpt = this.session.findTableMetadata(tableName);
            if (metaOpt.isPresent()) {
                this.metadata = metaOpt.get();
            } else {
                // エラーにすべきところ。
                System.out.println("******** error not metadata *********");
            }
            var sql = configuration.getDataset().getQuery();
            var variables = TgBindVariables.of();
            for (var col : metadata.getLowColumnList()) {
                variables.addInt(col.getName());
            }
            var parameterMapping = TgParameterMapping.of(variables);
            this.ps = this.session.createStatement(sql, parameterMapping);
            var setting = TgTmSetting.ofAlways(TgTxOption.ofOCC());
            this.tm = session.createTransactionManager(setting);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(" url:" + configuration.getDataset().getDatastore().getUrl()
                + " user:" + configuration.getDataset().getDatastore().getUsername()
                + " pass:" + configuration.getDataset().getDatastore().getPassword()
                + " query:" + configuration.getDataset().getQuery());
        System.out.println("metadata from tsurugi");
        for (var col : metadata.getLowColumnList()) {
            System.out.println(col.toString());
        }

    }

    @BeforeGroup
    public void beforeGroup() {
        // if the environment supports chunking this method is called at the beginning
        // if a chunk
        // it can be used to start a local transaction specific to the backend you use
        // Note: if you don't need it you can delete it
        System.out.println("CompanyOutputOutput#beforeGroup");
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput) {
        // this is the method allowing you to handle the input(s) and emit the output(s)
        // after some custom logic you put here, to send a value to next element you can
        // use an
        // output parameter and call emit(value).
        System.out.println("CompanyOutputOutput#onNext");
        System.out.println("defaultInput" + defaultInput.toString());
        try {
            this.tm.execute(transaction -> {
                // TODO StudioのOutputコンポーネントで設定するスキーマのカラム名はInputもOutputもDBと合わせることを前提
                var parameter = TgBindParameters.of();
                for (var col : this.metadata.getLowColumnList()) {
                    if (col.getAtomTypeValue() == 4) {
                        parameter = parameter.addInt(col.getName(),
                                Integer.valueOf(defaultInput.getInt(col.getName())));
                    }
                }
                int ret_i = transaction.executeAndGetCount(this.ps, parameter);
                System.out.println(configuration.getTransactionMode() + " count =" + ret_i);
            });
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @AfterGroup
    public void afterGroup() {
        // symmetric method of the beforeGroup() executed after the chunk processing
        // Note: if you don't need it you can delete it
        System.out.println("CompanyOutputOutput#afterGroup");
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
        System.out.println("CompanyOutputOutput#release");
        try {
            this.session.close();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}