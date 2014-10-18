package edu.cmu.lti.f14.hw3.hw3_xinyunzh.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_xinyunzh.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xinyunzh.typesystems.Token;

import java.util.HashSet;

import edu.cmu.lti.f14.hw3.hw3_xinyunzh.utils.Utils;
import edu.cmu.lti.f14.hw3.hw3_xinyunzh.typesystems.*;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	/** Global word dictionary **/
	HashMap<Integer, HashSet<String>> listHsDict;

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		listHsDict = new HashMap<Integer, HashSet<String>>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas = aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

		if (it.hasNext()) {
			Document doc = (Document) it.next();

			// Make sure that your previous annotators have populated this in
			// CAS
			FSList fsTokenList = doc.getTokenList();
			// System.out.println(fsTokenList);
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());

			// Do something useful here

			HashSet<String> hsDict;

			if (doc.getRelevanceValue() == 99) {
				hsDict = new HashSet<String>();
				listHsDict.put(doc.getQueryID(), hsDict);
			} else {
				hsDict = listHsDict.get(doc.getQueryID());
				Iterator<Token> tokIter = tokenList.iterator();
				while (tokIter.hasNext()) {
					hsDict.add(tokIter.next().getText());
				}
			}
		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure

		// TODO :: compute the rank of retrieved sentences

		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
		
		// Test for print out the dictionary
//		Set<Entry<Integer, HashSet<String>>> entryDic = listHsDict.entrySet();
//		Iterator<Entry<Integer, HashSet<String>>> dicIterator = entryDic.iterator();
//		while (dicIterator.hasNext()) {
//			System.out.println(dicIterator.next());
//		}
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		// TODO :: compute cosine similarity between two sentences

		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr = 0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection

		return metric_mrr;
	}

}
