package edu.cmu.lti.f14.hw3.hw3_xinyunzh.casconsumers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
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
	
	int counter = 0;
	
	/** The output path for report.txt **/
	private File outputPath;

	/** File Writer variable **/
	private FileWriter outFW = null;

	/** BufferWrite variable **/
	private BufferedWriter outBW = null;

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	/** Global Word Frequency for each Sentence **/

	ArrayList<ArrayList<QueryDocInfo>> wordFreqSentGlob;

	/** Word Freq for each group of query and answer **/
	ArrayList<QueryDocInfo> groupQueryDocInfo;

	/** Word Freq for each group of query and answer and rel **/

	/** Global Sentence Content Storage **/
	ArrayList<String> globSentCont;

	/**
	 * The array that used to store rank information for each answer in the
	 * query
	 **/
	ArrayList<Integer> rank = new ArrayList<Integer>();

	/** Current Qid **/
	int currQid;

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		wordFreqSentGlob = new ArrayList<ArrayList<QueryDocInfo>>();

		globSentCont = new ArrayList<String>();

		outputPath = new File("report.txt");

		currQid = 0;

		try {
			outFW = new FileWriter(outputPath, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		outBW = new BufferedWriter(outFW);

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {
		int lastQid = 0;
		/** Use to keep the docs of same query **/

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
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(
					fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());

			// Do something useful here

			// The current doc is a query.
			if (doc.getRelevanceValue() == 99) {
				// Since we have encounter another group of query, therefore we
				// add the whole group into the array.
					wordFreqSentGlob.add(groupQueryDocInfo);
				currQid++;
				groupQueryDocInfo = new ArrayList<QueryDocInfo>();
			}
			QueryDocInfo qDI = new QueryDocInfo();

			qDI.setDocIndex(groupQueryDocInfo.size());
			qDI.setRel(doc.getRelevanceValue());
			qDI.setText(doc.getText());
			qDI.setTokenFreq(tokenList);
			qDI.setQid(currQid);
			groupQueryDocInfo.add(qDI);
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

		// Display the sentence information
		wordFreqSentGlob.add(groupQueryDocInfo);
		wordFreqSentGlob.remove(0);

		// TODO :: compute the cosine similarity measure
		for (ArrayList<QueryDocInfo> groupSentFreq : wordFreqSentGlob) {
			Map<String, Integer> queryVector = null;
			for (QueryDocInfo qDI : groupSentFreq) {
				HashMap<String, Integer> docVector = null;
				if (qDI.getRel() == 99) {
					queryVector = qDI.getTokenFreq();
				} else {
					docVector = qDI.getTokenFreq();
				}
				if (queryVector != null && docVector != null) {
					qDI.setCoSim(computeCosineSimilarity(queryVector, docVector));

				}
			}
		}

		// TODO :: compute the rank of retrieved sentences

		// Also write this information into the file, include the sentence.

		/** The corresponding sentence content **/
		for (ArrayList<QueryDocInfo> groupSentFreq : wordFreqSentGlob) {
			ArrayList<QueryDocInfo> tempList = new ArrayList<QueryDocInfo>(groupSentFreq);
			tempList.remove(0);
			Collections.sort(tempList);
			for (QueryDocInfo qID : groupSentFreq) {
				qID.setRank(tempList.indexOf(qID) + 1);
			}
		}

		// Make the 4 digit format
		DecimalFormat df = new DecimalFormat("0.0000");
		for (int i = 0; i < wordFreqSentGlob.size(); i++) {
			ArrayList<QueryDocInfo> groupSentFreq = wordFreqSentGlob.get(i);
			for (QueryDocInfo qID : groupSentFreq) {
				if (qID.getRel() == 1) {
					String linePrint = new String("cosine="
							+ df.format(qID.getCoSim()) + "\t" + "rank="
							+ qID.getRank() + "\t" + "qid="
							+ qID.getQid() + "\t" + "rel=" + qID.getRel()
							+ "\t" + qID.getText());
					System.out.println(linePrint);
					rank.add(qID.getRank());
					outBW.write(linePrint + "\n");
				}
			}
		}
		
		// TODO :: compute the metric:: mean reciprocal rank
		
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::"
				+ df.format(metric_mrr));
		outBW.write("MRR=" + df.format(metric_mrr));
		outBW.close();
	}

	/**
	 * This method is used to calculate the cosine_similarity for two input
	 * vector, which are Query Vector and Doc Vector, respectively.
	 * 
	 * @param queryVector
	 *            Query Vector
	 * 
	 * @param docVector
	 *            Document Vector
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
		Set<Entry<String, Integer>> entrySetQV = queryVector.entrySet();
		for (Entry<String, Integer> entryQV : entrySetQV) {
			String commonString = entryQV.getKey();
			if (docVector.containsKey(commonString)) {
				cosine_similarity += entryQV.getValue()
						* docVector.get(commonString);
			}
		}
		cosine_similarity = cosine_similarity / (qVLen * dVLen);

		return (double) Math.round(cosine_similarity * 10000) / 10000;
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
	 * This method use a global variable that stores all of the rank information
	 * to calculate the MRR.
	 * 
	 * @return mrr The value of MRR
	 */
	private double compute_mrr() {
		double metric_mrr = 0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for (int r : rank) {
			metric_mrr += 1 / (double) r;
		}
		metric_mrr = metric_mrr / rank.size();
		return metric_mrr;
	}
}
