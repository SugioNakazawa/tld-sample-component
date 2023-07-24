package com.company.talend.components.source;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import com.tsurugidb.iceaxe.TsurugiConnector;
import com.tsurugidb.iceaxe.sql.result.TsurugiResultEntity;
import com.tsurugidb.iceaxe.transaction.manager.TgTmSetting;
import com.tsurugidb.iceaxe.transaction.option.TgTxOption;
import com.tsurugidb.tsubakuro.channel.common.connection.UsernamePasswordCredential;

import com.company.talend.components.service.CompanyComponentService;

@Documentation("TODO fill the documentation for this source")
public class CompanyInputSource implements Serializable {
    private final CompanyInputMapperConfiguration configuration;
    private final CompanyComponentService service;
    private final RecordBuilderFactory builderFactory;
    // get datas from DB in init
    private final List<TsurugiResultEntity> dataList;
    // data iterator (accessor)
    private Iterator<TsurugiResultEntity> listIt;

    public CompanyInputSource(@Option("configuration") final CompanyInputMapperConfiguration configuration,
            final CompanyComponentService service,
            final RecordBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
        this.dataList = new ArrayList<TsurugiResultEntity>();
        this.listIt = null;
    }

    @PostConstruct
    public void init() {
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        // temporary prepare datas
        System.out.println("CompanyInputSource#init#int");
        System.out.println(" url:" + configuration.getDataset().getDatastore().getUrl()
                + " user:" + configuration.getDataset().getDatastore().getUsername()
                + " pass:" + configuration.getDataset().getDatastore().getPassword()
                + " query:" + configuration.getDataset().getQuery()
        );
        System.out.println("outgoingSchema: " + configuration.getOutgoing());

        var endpoint = URI.create("tcp://" + configuration.getDataset().getDatastore().getUrl());
        var credential = new UsernamePasswordCredential("user", "password");
        var connector = TsurugiConnector.of(endpoint, credential);

        var sql = configuration.getDataset().getQuery();

        try (var session = connector.createSession()) {
            try (var ps = session.createQuery(sql)) {
                var setting = TgTmSetting.ofAlways(TgTxOption.ofOCC());
                var tm = session.createTransactionManager(setting);
                tm.execute(transaction -> {
                    List<TsurugiResultEntity> list = transaction.executeAndGetList(ps);
                    //  just info
                    if (list.size() > 0){
                        System.out.println("db colums: " + list.get(0).getNameList());
                    }
                    //  store datas
                    for (var entity : list) {
//                        System.out.println("pk: " + entity.getLong("pk"));
//                        System.out.println("c1: " + entity.getString("c1"));
                        this.dataList.add(entity);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //  dummy
//        this.dataList.add("A");
//        this.dataList.add("B");
//        this.dataList.add("C");
    }

    @Producer
    public Record next() {
        // this is the method allowing you to go through the dataset associated
        // to the component configuration
        //
        // return null means the dataset has no more data to go through
        // you can use the builderFactory to create a new Record.
        if (listIt == null) {
            listIt = dataList.iterator();
        }
        if (!listIt.hasNext()) {
            return null;
        }
        var record = listIt.next();
        //TODO  以下はもっと綺麗に
        var ret = builderFactory.newRecordBuilder();
        //TODO  とりあえず順番通りに文字列でデータを生成。SchemaInfoを利用しないと適切な方への変換ができない。
        int i = 0;
        for(var col:configuration.getOutgoing()){
            if(i < record.getNameList().size()) {
                // ret = ret.withString(col.getLabel(), record.getString(record.getName(i++)));
                ret = ret.withString(col, record.getString(record.getName(i++)));
            }
        }
        return ret.build();
    }

    @PreDestroy
    public void release() {
        System.out.println("CompanyInputSource#init#int#release");
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
    }
}