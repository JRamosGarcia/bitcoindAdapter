package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ScriptPubKey extends SignatureScript {
	private Integer reqSigs;// (Optional) The number of signatures required; this is always 1 for P2PK,
							// P2PKH, and P2SH
							// (including P2SH multisig because the redeem script is not available in the
							// pubkey script). It may be greater than 1 for bare multisig. This value will
							// not be returned for nulldata or nonstandard script types (see the type key
							// below)
	private String type;// (optional) The type of script. This will be one of the following:
	// - pubkey for a P2PK script
	// - pubkeyhash for a P2PKH script
	// - scripthash for a P2SH script
	// - multisig for a bare multisig script
	// - nulldata for nulldata scripts
	// - nonstandard for unknown scripts

	private List<String> addresses;// (Optional) The P2PKH or P2SH addresses used in this transaction, or the
									// computed P2PKH address of any pubkeys in this transaction. This array will
									// not be returned for nulldata or nonstandard script types

}
