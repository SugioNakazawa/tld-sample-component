package com.company.talend.components.source;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Record.Builder;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import com.tsurugidb.iceaxe.TsurugiConnector;
import com.tsurugidb.iceaxe.metadata.TgTableMetadata;
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

    private TgTableMetadata metadata;
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
                + " query:" + configuration.getDataset().getQuery());
        System.out.println("outgoingSchema: " + configuration.getOutgoing());

        var endpoint = URI.create("tcp://" + configuration.getDataset().getDatastore().getUrl());
        var credential = new UsernamePasswordCredential("user", "password");
        var connector = TsurugiConnector.of(endpoint, credential);
        var tableName = configuration.getDataset().getTableName();

        var sql = configuration.getDataset().getQuery();

        try (var session = connector.createSession()) {
            var metaOpt = session.findTableMetadata(tableName);
            if (metaOpt.isPresent()) {
                this.metadata = metaOpt.get();
            } else {
                // エラーにすべきところ。
                System.out.println("******** error not metadata *********");
            }
            try (var ps = session.createQuery(sql)) {
                var setting = TgTmSetting.ofAlways(TgTxOption.ofOCC());
                var tm = session.createTransactionManager(setting);
                tm.execute(transaction -> {
                    List<TsurugiResultEntity> list = transaction.executeAndGetList(ps);
                    // just info
                    if (list.size() > 0) {
                        System.out.println("db colums: " + list.get(0).getNameList());
                    }
                    // store datas
                    for (var entity : list) {
                        // System.out.println("pk: " + entity.getLong("pk"));
                        // System.out.println("c1: " + entity.getString("c1"));
                        this.dataList.add(entity);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // dummy
        // this.dataList.add("A");
        // this.dataList.add("B");
        // this.dataList.add("C");
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
        // TODO 以下はもっと綺麗に
        var builder = builderFactory.newRecordBuilder();
        // TODO とりあえず順番通りに文字列でデータを生成。SchemaInfoを利用しないと適切な方への変換ができない。
        int i = 0;
        for (var columnName : configuration.getOutgoing()) {
            if (i < record.getNameList().size()) {
                builder = addColumn(builder, columnName, record);
            }
        }
        return builder.build();
    }

    private Builder addColumn(Builder builder, String columnName, TsurugiResultEntity record) {
        switch (findColumnAtomTypeValue(columnName)) {
            case 4:
                builder = builder.withInt(columnName, record.getInt(columnName, 0));
                break;
            case 9:
                builder = builder.withString(columnName, record.getString(columnName, null));
                break;
            case 15:
                builder = builder.withDateTime(columnName, date2localDate(record.getDate(columnName, null)));
                break;
            case -1:
                System.out.println("not found in db-schema is " + columnName);
                break;
            default:
                System.out.println("not support AtomType = " + findColumnAtomTypeValue(columnName));
        }
        return builder;
    }

    private Date date2localDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private int findColumnAtomTypeValue(String colName) {
        for (var col : metadata.getLowColumnList()) {
            if (colName.equalsIgnoreCase(col.getName())) {
                return col.getAtomTypeValue();
            }
        }
        return -1;
    }

    @PreDestroy
    public void release() {
        System.out.println("CompanyInputSource#init#int#release");
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
    }
}