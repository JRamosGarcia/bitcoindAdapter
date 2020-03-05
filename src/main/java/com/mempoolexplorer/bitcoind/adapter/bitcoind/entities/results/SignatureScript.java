package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An object describing the signature script of this input. Not present if this
 * is a coinbase transaction
 * 
 * @author tomillo
 *
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
public abstract class SignatureScript {

	private String asm;// The signature script in decoded form with non-data-pushing opcodes listed
	private String hex;// The signature script encoded as hex

}
