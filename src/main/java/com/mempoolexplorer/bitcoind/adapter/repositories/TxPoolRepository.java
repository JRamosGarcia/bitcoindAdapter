package com.mempoolexplorer.bitcoind.adapter.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;

public interface TxPoolRepository extends MongoRepository<Transaction, String> {

}
