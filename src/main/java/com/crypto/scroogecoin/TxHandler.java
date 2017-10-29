package com.crypto.scroogecoin;

import java.util.HashSet;
import java.util.Set;

public class TxHandler {
	
	private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENTED THIS
    	this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENTED THIS
    	UTXOPool unspentTxs = new UTXOPool();
    	double txOutSum = 0;
        double txInSum = 0;
        int index = 0;
        
        for (Transaction.Input input: tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            
            if (!utxoPool.contains(utxo)) 
            	return false;
            
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(index), input.signature))
                return false;
            
            if (unspentTxs.contains(utxo)) 
            	return false;
            
            unspentTxs.addUTXO(utxo, output);
            txOutSum += output.value;
            index++;
        }
        
        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value < 0) 
            	return false;
            
            txInSum += out.value;
        }
        
        return txOutSum >= txInSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENTED THIS
    	
    	 Set<Transaction> validTxs = new HashSet<>();

         for (Transaction tx : possibleTxs) {
             
        	 if (isValidTx(tx)) {
                 validTxs.add(tx);
                 
                 for (Transaction.Input in : tx.getInputs()) {
                     UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                     utxoPool.removeUTXO(utxo);
                 }
                 
                 
                 for (int index = 0; index < tx.numOutputs(); index++) {
                     Transaction.Output out = tx.getOutput(index);
                     UTXO utxo = new UTXO(tx.getHash(), index);
                     utxoPool.addUTXO(utxo, out);
                 }
             }
         }

         Transaction[] validTxArray = new Transaction[validTxs.size()];
         return validTxs.toArray(validTxArray);
    }

}
