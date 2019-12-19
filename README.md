# BitcoindAdapter

This is a spring boot project to mantain a REST-queriable memPool. It works as a bitcoind adapter which queries bitcoind for mempool transactions, The main difference with bitcoind is that transaction data includes input and output addresses, so you don't have to do several queries, bitcoindAdapter does it for you. This is quite useful for a memPoolExplorer. Also serves as memPool cache to avoid overloading bitcoind.

bitcoindAdapter uses mainly these bitcoind RPC calls: 
  * getrawmempool (verbose) to obtain the mempool, and then for each txInput in each transaction:
  * getRawTransaction (decoded) is called. 

Mempool changes and new blocks are sent through a configurable kafka topic (memPool.tx.events). Mempool tx changes are guaranteed not to be sent when a new block is mined. That is, if a new block is detected after checking mempool changes, these changes are not sent or stored. They wait for the next refresh round to be sent and stored.

This ensures a message order in which message consumers can store the new block transactions before they are removed from mempool in the next message, enabling consumers to compare mined block txs with mempool txs.  

Also, it uses a mongodb database for (optionally) storing the mempool.

## Requirements

* bitcoind 0.15
* mongodb
* java 9

## Usage

first start mongodb with:
`sudo service mongodb start`

run `maven install` with `prod` or `dev` profiles and execute with `java -jar bitcoindAdapter.jar --spring.profiles.active=prod,mainNet`

## Working modes (profiles)
Default spring profiles are `prod` and `mainNet` but you can also use `prod` and `testNet`. there are one `application-{profile}.properties` for each profile

## bitcoind configuration

You *must* have bitcoind running with `txindex=1` option and basic authentication. Use a bitcoind.conf file like this in your `~/.bitcoin/bitcoin.conf` folder.

```# Generated by https://jlopp.github.io/bitcoin-core-config-generator/

# This config should be placed in following path:
# ~/.bitcoin/bitcoin.conf

# [rpc]
# Username for JSON-RPC connections
rpcuser=myuser
# Password for JSON-RPC connections
rpcpassword=mypassword
# Maintain a full transaction index, used by the getrawtransaction rpc call.
txindex=1
```
## .properties configuration

.properties file are almost auto-explicative. remarks on the following properties:

* `spring.data.mongodb.uri=mongodb://127.0.0.1:27017/memPool`
Uri of mongodb collection to use for storing the mempool

* `bitcoindadapter.loadDBOnStart={true|false}`
Decides if bitcoindAdapter must try to load mempool from db when starting. If not, it will query all mempool txs to bitcoind (it can takes hours). Normally mempool in db will be in part outdated. bitcoindAdapter will use txs which are still in mempool and discard the old ones. This saves a lot of time.

* `bitcoindadapter.saveDBOnStart={true|false}`
Decides if bitcoindAdapter must save mempool when fully loaded and updated at start.

* `bitcoindadapter.saveDBOnRefresh={true|false}`
Saves mempool changes in db for each mempool refresh the application does.

* `bitcoindadapter.maxMemPoolSizeReturnedInTxNumber=300`
REST API `/memPool/full`  returns all mempool, if tx number in mempool is above this number a http 413 error, "Payload Too Large" is returned. `/memPool/full` is used mainly in `testNet`

* `bitcoindadapter.memPoolChangesSize=10`
REST API `/memPool/changes` returns last 10 changes in memPool.

* `bitcoindadapter.newBlockListSize=3`
REST API `/lastBlocks` return last 3 blocks

## REST API
* `/lastBlocks` Returns a list with all last blocks recevived (normally last 3)
* `/lastBlocksFrom/{epochSecond}/{nano}` Returns last blocks from epochSecond/nano
* `/lastBlocksFrom/{height}` Returns last blocks from {height}
* `/memPool` Returns a list with all txIds in mempool
* `/memPool/full` Returns all mempool if `bitcoindadapter.maxMemPoolSizeReturnedInTxNumber` property allows it.
* `/memPool/{txId}` Returns decoded tx with txId given as parameter
* `/memPool/changes` Returns last N changes in memPool, N defined in `bitcoindadapter.memPoolChangesSize` property.
* `/memPool/changesFrom/{changeCounter}` Returns last changes from changeCounter, each change is identified by a changeCounter and a epochSecond/nano.
* `/memPool/changesFrom/{epochSecond}/{nano}` Returns last changes from epochSecond/nano, each change is identified by a changeCounter and a epochSecond/nano.
* `/memPool/state` Returns memPool state  {STARTING|LOADINGFROMDB|LOADINGFROMBITCOINCLIENT|SAVINGTODB|STARTED}
* `/actuator` Access to spring boot actuator endpoint. Be aware that actuator port is defined in `management.port` property.
* `/actuator/metrics` Access to metrics like `mempool.transaction.count` or `mempool.refresh.time` to monitor app performance.

## ACTUATOR API

You can enter actuator via port `8081` `/actuator/ENDPOINT NAME` (i.e. `health, info`... etc)