package edu.cmu.lti.f14.hw3.hw3_xinyunzh.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

	/** Global Word Frequency for each Sentence **/
	/**
	 * HashMap<qid, ArrayList<HashMap<Integer, HashMap<rel, HashMap<token,
	 * freq>>>>
	 **/
	HashMap<Integer, ArrayList<HashMap<Integer, HashMap<String, Integer>>>> wordFreqSentGlob;

	/** Word Freq for each group of query and answer **/
	ArrayList<HashMap<Integer, HashMap<String, Integer>>> wordFreqSentGroup;

	/** Word Freq for each group of query and answer and rel **/
	HashMap<Integer, HashMap<String, Integer>> wordFreqSentRel;
	
	/** Global Sentence Content Storage **/
	ArrayList<String> globSentCont;
	
	/** The array that used to store rank information for each answer in the query **/
	ArrayList<Integer> rank = new ArrayList<Integer>();

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		listHsDict = new HashMap<Integer, HashSet<String>>();

		wordFreqSentGlob = new HashMap<Integer, ArrayList<HashMap<Integer, HashMap<String, Integer>>>>();
		
		globSentCont = new ArrayList<String>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {
		int lastQid = 0;
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
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(
					fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());

			// Do something useful here

			HashSet<String> hsDict;

			if (doc.getRelevanceValue() == 99) {
				if (doc.getQueryID() == 1) {
					wordFreqSentGlob.put(1, wordFreqSentGroup);
				} else {
					wordFreqSentGlob.put(doc.getQueryID() - 1,
							wordFreqSentGroup);
				}
				hsDict = new HashSet<String>();
				wordFreqSentGroup = new ArrayList<HashMap<Integer, HashMap<String, Integer>>>();

				wordFreqSentRel = new HashMap<Integer, HashMap<String, Integer>>();

				wordFreqSentRel.put(doc.getRelevanceValue(),
						storeWordFreq(tokenList));

				wordFreqSentGroup.add(wordFreqSentRel);
				// System.out.println(wordFreqSentGroup);

				listHsDict.put(doc.getQueryID(), hsDict);

				Iterator<Token> tokIter = tokenList.iterator();
				HashMap<String, Integer> wordFreqSingSent = new HashMap<String, Integer>();
				while (tokIter.hasNext()) {
					Token token = tokIter.next();
					hsDict.add(token.getText());
					wordFreqSingSent.put(token.getText(), token.getFrequency());
				}

				// System.out.println(doc.getQueryID());
			} else {
				hsDict = listHsDict.get(doc.getQueryID());
				// Iterator<Token> tokIter = tokenList.iterator();
				// HashMap<String, Integer> wordFreqSingSent = new
				// HashMap<String, Integer>();
				// while (tokIter.hasNext()) {
				// Token token = tokIter.next();
				// hsDict.add(token.getText());
				// wordFreqSingSent.put(token.getText(), token.getFrequency());
				// }
				// System.out.println(wordFreqSingSent);
				wordFreqSentRel = new HashMap<Integer, HashMap<String, Integer>>();
				wordFreqSentRel.put(doc.getRelevanceValue(),
						storeWordFreq(tokenList));
				wordFreqSentGroup.add(wordFreqSentRel);
				
				// Add 1answer into arraylist to store
				if (doc.getRelevanceValue() == 1) {
					globSentCont.add(doc.getText());
				}
				// wordFreqSentGroup.add(wordFreqSingSent);

			}
			lastQid = doc.getQueryID();
			// Test for print wordFreqSentGlob
			// Iterator<Entry<Integer, ArrayList<HashMap<String, Integer>>>>
			// globItr =
			// wordFreqSentGlob.entrySet().iterator();
			// while (globItr.hasNext()) {
			// System.out.println(globItr.next());
			// }
		}
		wordFreqSentGlob.put(lastQid, wordFreqSentGroup);

	}

	private HashMap<String, Integer> storeWordFreq(ArrayList<Token> tokenList) {
		Iterator<Token> tokItr = tokenList.iterator();
		HashMap<String, Integer> wordFreqSentce = new HashMap<String, Integer>();
		while (tokItr.hasNext()) {
			Token token = tokItr.next();
			wordFreqSentce.put(token.getText(), token.getFrequency());
		}
		return wordFreqSentce;
	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);
		// Test of Print out the result
		Set<Entry<Integer, ArrayList<HashMap<Integer, HashMap<String, Integer>>>>> entrywfGlob = wordFreqSentGlob
				.entrySet();
		Iterator<Entry<Integer, ArrayList<HashMap<Integer, HashMap<String, Integer>>>>> entryItr = entrywfGlob
				.iterator();
		while (entryItr.hasNext()) {
			Entry<Integer, ArrayList<HashMap<Integer, HashMap<String, Integer>>>> entry = entryItr
					.next();
			System.out.println(entry.getKey() + "***********"
					+ entry.getValue());
		}
		System.out.println("***********");

		// Display the sentence information

		// TODO :: compute the cosine similarity measure
		/** HashMap<Qid, cosineSimGroup(ArrayList<HashMap<rel, Double>>)> cosineSimAll **/
		HashMap<Integer, ArrayList<HashMap<Integer, Double>>> cosineSimAll = new HashMap<Integer, ArrayList<HashMap<Integer, Double>>>();
		
		/** ArrayList<HashMap<rel, Double >>**/
		ArrayList<HashMap<Integer, Double >> cosineSimGroup;

		// Set<Entry<Integer, ArrayList<HashMap<Integer, HashMap<String,
		// Integer>>>>> entrywfGlob = wordFreqSentGlob.entrySet();
		entrywfGlob = wordFreqSentGlob.entrySet();
		// Iterator<Entry<Integer, ArrayList<HashMap<Integer, HashMap<String,
		// Integer>>>>> entryItr = entrywfGlob.iterator();
		entryItr = entrywfGlob.iterator();

		while (entryItr.hasNext()) {
			// System.out.println("***********");
			Entry<Integer, ArrayList<HashMap<Integer, HashMap<String, Integer>>>> entry = entryItr
					.next();
			int qid = entry.getKey();
			ArrayList<HashMap<Integer, HashMap<String, Integer>>> alItem = entry
					.getValue();
			// new ArrayList<HashMap<Integer, HashMap<String, Integer>>>();
			Map<String, Integer> queryVector = null;
			Map<String, Integer> docVector;
			cosineSimGroup = new ArrayList<HashMap<Integer, Double>>();
			Double currSim;
			for (HashMap<Integer, HashMap<String, Integer>> hashMap : alItem) {
				// System.out.println("***********");
				Set<Entry<Integer, HashMap<String, Integer>>> entryMap = hashMap
						.entrySet();
				for (Entry<Integer, HashMap<String, Integer>> entryItem : entryMap) {
					if (entryItem.getKey() == 99) {
						queryVector = entryItem.getValue();
					} else {
						docVector = entryItem.getValue();
						if (queryVector != null) {
							currSim = computeCosineSimilarity(queryVector,
									docVector);
							System.out.println(qid + " " + currSim);
							HashMap<Integer, Double> currSimRel = new HashMap<Integer, Double>();
							currSimRel.put(entryItem.getKey(), currSim);
							cosineSimGroup.add(currSimRel);
						}
					}
				}
			}
			cosineSimAll.put(qid, cosineSimGroup);
			// System.out.println(entry.getKey() + "***********" +
			// entry.getValue());
		}
		
		// TODO :: compute the rank of retrieved sentences
		
		// Also write this information into the file, include the sentence.
		/** The array that used to store rank information for each answer in the query **/
		/** Declared outside of this scope **/
		
		/** The corresponding cosValue array **/
		ArrayList<Double> cosineListOf1 = new ArrayList<Double>();
		/** The corresponding sentence content **/
		// ArrayList<String> senCont = new ArrayList<String>();
		Set<Entry<Integer, ArrayList<HashMap<Integer, Double>>>> EntryCSA = cosineSimAll.entrySet();
		for (Entry<Integer, ArrayList<HashMap<Integer, Double>>> group : EntryCSA) {
			// int qid = group.getKey();
			ArrayList<HashMap<Integer, Double>> answerGroup = group.getValue();
			ArrayList<Double> simList = new ArrayList<Double>();
			double valueOf1 = 0.0;
			for(HashMap<Integer, Double> answer : answerGroup) {
				Entry<Integer, Double> answerEntry = answer.entrySet().iterator().next();
				simList.add(answerEntry.getValue());
				if (answerEntry.getKey() == 1) {
					valueOf1 = answerEntry.getValue();
					cosineListOf1.add(answerEntry.getValue());
				}
			}
			// System.out.println(simList);
			Collections.sort(simList);
			System.out.println(simList);
			rank.add(simList.size() - simList.indexOf(valueOf1));
//			ArrayList<Entry<Integer, Double>> answerGroupEntry = new ArrayList<Entry<Integer, Double>>();
//			for (HashMap<Integer, Double> answer : answerGroup) {
//				answerGroupEntry.add(answer.entrySet().iterator().next());
//				
//			}
			
		}
		System.out.println(cosineListOf1.size() + " " + rank.size() + " " + globSentCont.size());
		for (int i = 0; i < rank.size(); i++) {
			System.out.println("consine" + cosineListOf1.get(i) + " " + "rank=" + rank.get(i) + " " + "rel=1" + " " + globSentCont.get(i));
		}
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);

		// Test for print out the dictionary
		// Set<Entry<Integer, HashSet<String>>> entryDic =
		// listHsDict.entrySet();
		// Iterator<Entry<Integer, HashSet<String>>> dicIterator =
		// entryDic.iterator();
		// while (dicIterator.hasNext()) {
		// System.out.println(dicIterator.next());
		// }
	}

	// private <Integer, Map<String, Integer>>

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		// TODO :: compute cosine similarity between two sentences
		double qVLen, dVLen = 0;
		qVLen = computeVectorLength(queryVector);
		dVLen = computeVectorLength(docVector);

		// Set<Entry<Integer, HashSet<String>>> entryListHsDict =
		// listHsDict.entrySet();
		// System.out.println(queryVector.entrySet());
		// System.out.println(docVector.entrySet());
		Set<Entry<String, Integer>> entrySetQV = queryVector.entrySet();
		for (Entry<String, Integer> entryQV : entrySetQV) {
			String commonString = entryQV.getKey();
			if (docVector.containsKey(commonString)) {
				cosine_similarity += entryQV.getValue()
						* docVector.get(commonString);
			}
		}
		cosine_similarity = cosine_similarity / (qVLen * dVLen);

		// Iterator<Entry<String, Integer>> qVIter =
		// queryVector.entrySet().iterator();

		return cosine_similarity;
	}

	private double computeVectorLength(Map<String, Integer> vector) {
		double vLen = 0;
		for (Integer freq : vector.values()) {
			vLen = freq * freq + vLen;
		}
		vLen = Math.sqrt(vLen);
		return vLen;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr = 0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for (int r : rank){
			metric_mrr += 1 / (double)r; 
		}
		metric_mrr = metric_mrr / rank.size();
		return metric_mrr;
	}

}
