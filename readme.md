# Talend custom component alpja for Tsurugi DB

7/24 Tsurugi への SELECT 実行と tLogRow コンポーネントへの連結のみ実行可能。ただし製品販のみでTOSでは Tsurugi へのアクセスは行えているが、Studio内でのコンポーネント連携でエラーとなる。原因はTOSでは実行時のcomponent-sdk関連のjarがStudio7.3当時のもので実行されるためと思われる。1.37.0

## prepare
### Talend Studio
- 事前の設定変更
  - TalendStudio8.0.1にて検証。有償版のため購入が推奨。
  - カスタムコンポーネント更新時にStudioの再起動を省くためにStudioのconfig.iniに以下を追加しておく。
  ファイル； /Applications/TOSDI-8.0.1/studio/configuration/config.ini
  ```component.environment=dev```

- 未解決
  - xmiファイルの置き換え。GitHub/tdi-studio-se のファイルと置き換える。->効果なし！
  ファイル： /Applications/TOSDI-8.0.1/studio/configuration/MavenUriIndex.xml
  - TOS8.0.1ではStudioでの実行時にエラー。古いComponentKitの1.37で実行されるため。1.55へのアップデート方法が不明。

### 開発環境
- VisualStudioCode
- （補足）サイトではIntellJを推奨。最初の雛形はIntellJのpluginまたはTalendのサイトで生成する。

## Build　Deploy
```shell
# build
company-component@nkzwMBP2020$ ./mvnw clean talend-component:dependencies install
# to studio
company-component@nkzwMBP2020$ ./mvnw talend-component:deploy-in-studio -Dtalend.component.studioHome="/Applications/TalendStudio-8.0.1/studio"
# to TOS（実行時にエラー）
company-component@nkzwMBP2020$ ./mvnw talend-component:deploy-in-studio -Dtalend.component.studioHome="/Applications/TOSDI-8.0.1/studio"
```

## 動作説明
### Input component
CompanyInputSource#init
- 外部から取得したデータを保持

CompanyInputSource#next
- １回のCallで保持しているデータから１レコードを返す。
- 保持しているデータがなくなったらnullを返す。

### Output component
CompanyOutputOutput#init()
- 最初に１回呼ばれる。（何の最初か？）

CompanyOutputOutput#beforeGroup()
- 最初に１回呼ばれる。（何の最初か？）

CompanyOutputOutput#onNext(Record)
- １レコードごとにCallされる。

CompanyOutputOutput#afterGroup()
- 最後に１回呼ばれる。（何の最後か？）

CompanyOutputOutput#release()
- 最後に１回呼ばれる。（何の最後か？）

サンプルログ
```shell
[statistics] connecting to socket on port 3437
[statistics] connected
init
A|A+A # tLogRowの出力
beforeGroup
{"newColumn":"A","newColumn1":"A+A"}
B|B+B
{"newColumn":"B","newColumn1":"B+B"}
C|C+C
{"newColumn":"C","newColumn1":"C+C"}
afterGroup
release
[statistics] disconnected
```

## Tips
### 外部jar(　iceaxe,　tsubakuro　)　
#### 読み込み
- dependencies に追加。scode は compile
- 現在はmaven のローカル・リポジトリに手動で配置。
#### コンポーネントへの組み込み
maven plugin に talend-dependencies を追加し、実行する。
target/classes/TALEND-INF/dependecies.txtが生成されコンポーネントに組み込まれる。

### input コンポーネントに接続されたスキーマの取得
本来は List<Object> で取得できるはずであるが、現在は List<String> でカラム名のみを取得している。そのため型は全て String となっている。
https://talend.github.io/component-runtime/main/latest/studio-schema.html

### thsurugi実行時エラーのメッセージ
query文字列エラー：select -> seect
間違ったSQLがログに出されると嬉しい。
```
ジョブMyComponentを16:07 16/07/2023で開始します。
[statistics] connecting to socket on port 3842
[statistics] connected
#init url:localhost:12345 user:admin pass:null data(query):seect c1 from tb1
com.company.talend.components.dataset.CustomDataset@7813cb11
newColumn
com.tsurugidb.iceaxe.transaction.manager.exception.TsurugiTmIOException: ERR_PARSE_ERROR: SQL--0003: error in db_->create_executable(). TsurugiTransaction(OCC{}, iceaxeTxId=1, iceaxeTmExecuteId=1, attempt=0, transactionId=TID-0000000000000028), nextTx=NOT_RETRYABLE(OCC not retry. ERR_PARSE_ERROR: SQL--0003: error in db_->create_executable())
	at com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager.processTransactionException(TsurugiTransactionManager.java:298)
	at com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager.execute(TsurugiTransactionManager.java:217)
	at com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager.execute(TsurugiTransactionManager.java:137)
	at com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager.execute(TsurugiTransactionManager.java:121)
	at com.company.talend.components.source.CompanyInputSource.init(CompanyInputSource.java:72)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:564)
	at org.talend.sdk.component.runtime.base.LifecycleImpl.doInvoke(LifecycleImpl.java:87)
	at org.talend.sdk.component.runtime.base.LifecycleImpl.lambda$start$0(LifecycleImpl.java:49)
	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
	at java.base/java.util.stream.ReferencePipeline$11$1.accept(ReferencePipeline.java:442)
	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:177)
	at java.base/java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:948)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:484)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
	at org.talend.sdk.component.runtime.base.LifecycleImpl.start(LifecycleImpl.java:49)
	at org.talend.sdk.component.runtime.manager.chain.ChainedInput.next(ChainedInput.java:37)
	at talendtools.mycomponent_0_1.MyComponent.CompanyFamilyCompanyInput_1Process(MyComponent.java:790)
	at talendtools.mycomponent_0_1.MyComponent.runJobInTOS(MyComponent.java:1622)
	at talendtools.mycomponent_0_1.MyComponent.main(MyComponent.java:1321)
Caused by: com.tsurugidb.iceaxe.transaction.exception.TsurugiTransactionException: ERR_PARSE_ERROR: SQL--0003: error in db_->create_executable()
	at com.tsurugidb.iceaxe.util.IceaxeIoUtil.getAndCloseFuture(IceaxeIoUtil.java:62)
	at com.tsurugidb.iceaxe.util.IceaxeIoUtil.getAndCloseFutureInTransaction(IceaxeIoUtil.java:50)
	at com.tsurugidb.iceaxe.sql.result.TsurugiQueryResult.getLowResultSet(TsurugiQueryResult.java:155)
	at com.tsurugidb.iceaxe.sql.result.TsurugiQueryResult.getRecord(TsurugiQueryResult.java:215)
	at com.tsurugidb.iceaxe.sql.result.TsurugiQueryResult.getRecordList(TsurugiQueryResult.java:328)
	at com.tsurugidb.iceaxe.transaction.TsurugiTransaction.executeAndGetList(TsurugiTransaction.java:675)
	at com.company.talend.components.source.CompanyInputSource.lambda$init$1(CompanyInputSource.java:73)
	at com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager.lambda$execute$0(TsurugiTransactionManager.java:138)
	at com.tsurugidb.iceaxe.transaction.manager.TsurugiTransactionManager.execute(TsurugiTransactionManager.java:203)
	... 24 more
Caused by: com.tsurugidb.tsubakuro.sql.SqlServiceException: SQL--0003: error in db_->create_executable()
	at com.tsurugidb.tsubakuro.sql.impl.SqlServiceStub$QueryProcessor.doTest(SqlServiceStub.java:460)
	at com.tsurugidb.tsubakuro.sql.impl.SqlServiceStub$QueryProcessor.doTest(SqlServiceStub.java:439)
	at com.tsurugidb.tsubakuro.sql.impl.AbstractResultSetProcessor.test(AbstractResultSetProcessor.java:75)
	at com.tsurugidb.tsubakuro.sql.impl.SqlServiceStub$QueryProcessor.process(SqlServiceStub.java:502)
	at com.tsurugidb.tsubakuro.sql.impl.SqlServiceStub$QueryProcessor.process(SqlServiceStub.java:439)
	at com.tsurugidb.tsubakuro.channel.common.connection.ForegroundFutureResponse.processResult(ForegroundFutureResponse.java:125)
	at com.tsurugidb.tsubakuro.channel.common.connection.ForegroundFutureResponse.get(ForegroundFutureResponse.java:84)
	at com.tsurugidb.iceaxe.util.IceaxeIoUtil.getAndCloseFuture(IceaxeIoUtil.java:60)
	... 32 more
[statistics] disconnected

ジョブMyComponentが16:07 16/07/2023で終了しました。 [終了コード = 0]

```