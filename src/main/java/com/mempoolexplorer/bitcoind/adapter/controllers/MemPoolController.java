package com.mempoolexplorer.bitcoind.adapter.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.bitcoind.adapter.components.AppState;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.changes.TxPoolChangesContainer;
import com.mempoolexplorer.bitcoind.adapter.controllers.errors.ErrorDetails;
import com.mempoolexplorer.bitcoind.adapter.controllers.exceptions.TransactionNotFoundInMemPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.AppStateEnum;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

@RestController
@RequestMapping("/memPool")
public class MemPoolController {

	@Autowired
	private TxPoolContainer memPoolContainer;

	@Autowired
	private TxPoolChangesContainer memPoolChangesContainer;

	@Autowired
	private AppState appState;

	@GetMapping("/{txId}")
	public Transaction getTx(@PathVariable("txId") String txId) throws TransactionNotFoundInMemPoolException {

		Transaction tx = memPoolContainer.getTxPool().getTx(txId);
		if (null == tx) {
			throw new TransactionNotFoundInMemPoolException("Transaction id: " + txId + " not found");
		}
		return tx;
	}

	@GetMapping("")
	public Set<String> getMemPool() {
		return memPoolContainer.getTxPool().getTxIdSet();
	}

	@ExceptionHandler(TransactionNotFoundInMemPoolException.class)
	public ResponseEntity<?> onTransactionNotFound(TransactionNotFoundInMemPoolException e) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorMessage(e.getMessage());
		errorDetails.setErrorCode(HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	// Use with care!
	@GetMapping("/full")
	public Map<String, Transaction> getFullMemPool() {
		return memPoolContainer.getTxPool().getFullTxPool();
	}

	@GetMapping("/changes")
	public List<TxPoolChanges> getMemPoolChanges() {
		// Return all changes
		return memPoolChangesContainer.getLastChangesFrom(Instant.ofEpochMilli(1));
	}

	@GetMapping("/changesFrom/{changeCounter}")
	public List<TxPoolChanges> getMemPoolChanges(@PathVariable("changeCounter") Integer changeCounter) {
		return memPoolChangesContainer.getLastChangesFrom(changeCounter);
	}

	@GetMapping("/changesFrom/{epochSecond}/{nano}")
	public List<TxPoolChanges> getMemPoolChanges(@PathVariable("epochSecond") Integer epochSecond,
			@PathVariable("nano") Integer nano) {
		Instant instant = Instant.ofEpochSecond(epochSecond, nano);
		return memPoolChangesContainer.getLastChangesFrom(instant);
	}

	@GetMapping("/state")
	public AppStateEnum getAppState() {
		return appState.getState();
	}
}
