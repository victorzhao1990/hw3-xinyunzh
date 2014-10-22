package edu.cmu.lti.f14.hw3.hw3_xinyunzh.casconsumers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.cmu.lti.f14.hw3.hw3_xinyunzh.typesystems.Token;

/*
 * This class is used to store all of the relevant information about each doc in the query.
 */
public class QueryDocInfo implements QueryIdf, Rank,  Bm25, Comparable<QueryDocInfo> {
	private int qid;
	private int rel;
	private int docIndex;
	private String text;
	private double idf;
	private double coSim;
	private int rank;
	HashMap<String, Integer> tokenFreq;
	

	QueryDocInfo() {
		tokenFreq = new HashMap<String, Integer>();
	}

	public void setQid(int qid) {
		this.qid = qid;
	}

	public int getQid() {
		return this.qid;
	}

	public void setRel(int rel) {
		this.rel = rel;
	}

	public int getRel() {
		return this.rel;
	}

	public void setText(String text) {
		this.text = text.toString();
	}

	public String getText() {
		return this.text;
	}

	public void setDocIndex(int i) {
		this.docIndex = i;
	}

	public int getDocIndex() {
		return docIndex;
	}

	/**
	 * the method that use to extract the Word Frequency from tokenlist and put
	 * them into a HashMap
	 **/
	public void setTokenFreq(ArrayList<Token> tokenList) {
		// TODO Auto-generated method stub
		Iterator<Token> tokItr = tokenList.iterator();
		while (tokItr.hasNext()) {
			Token token = tokItr.next();
			tokenFreq.put(token.getText(), token.getFrequency());
		}
	}
	
	public HashMap<String, Integer> getTokenFreq() {
		return tokenFreq;
	}

	public double getIdf() {
		return this.idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}
	public void setCoSim(double coSim) {
		this.coSim = coSim;
	}
	
	public double getCoSim() {
		return coSim;
	}
	
	public void setRank(int rank){
		this.rank = rank;
	}
	
	public int getRank() {
		return rank;
	}
	
	@Override
    public int compareTo(QueryDocInfo other) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than 
        // other and 0 if they are supposed to be equal
		if (this.coSim < other.coSim) 
			return 1; 
		else if (this.coSim == other.coSim) {
			return 0;
		} else {
			return -1;
		}
    }
	public String toString() {
		String printext = new String("Qid=" + qid + " Rel=" + rel + " docIndex="
				+ docIndex + " " + " " + "cosine_sim=" + coSim + " " + "rank="+ rank + text + "\n");
		return printext;
	}
}