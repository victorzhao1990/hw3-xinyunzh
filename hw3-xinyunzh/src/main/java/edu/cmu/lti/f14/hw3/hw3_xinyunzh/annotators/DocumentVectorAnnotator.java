package edu.cmu.lti.f14.hw3.hw3_xinyunzh.annotators;

import java.util.*;
import java.util.Map.Entry;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_xinyunzh.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xinyunzh.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_xinyunzh.utils.Utils;


public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			// System.out.println(doc.getQueryID() + " " + doc.getRelevanceValue() + " " + doc.getText());
			createTermFreqVector(jcas, doc);
		}

	}

	/**
	 * A basic white-space tokenizer, it deliberately does not split on
	 * punctuation!
	 *
	 * @param doc
	 *            input text
	 * @return a list of tokens.
	 */

	List<String> tokenize0(String doc) {
		List<String> res = new ArrayList<String>();

		for (String s : doc.split("\\s+"))
			res.add(s);
		return res;
	}

	/**
	 * This function will create a Term Frequency Vector according to the time that one token occurs in one sentence.
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();

		// TO DO: construct a vector of tokens and update the tokenList in CAS
		// TO DO: use tokenize0 from above
		List<String> tokenStringList = tokenize0(doc.getText());
		
		HashMap<String, Integer> tokHM = new HashMap<String, Integer>();
		Iterator<String> tkIter = tokenStringList.iterator();
		List<Token> tokenListUnconv = new LinkedList<Token>();
		
		while (tkIter.hasNext()) {
			String tokenInSen = tkIter.next();
			if (!tokHM.containsKey(tokenInSen)) {
				tokHM.put(tokenInSen, 1);
			} else {
				tokHM.put(tokenInSen, tokHM.get(tokenInSen) + 1);
			}
		} 
		Set<Entry<String, Integer>> tisEntry = tokHM.entrySet();
		Iterator<Entry<String, Integer>> tisEnItr = tisEntry.iterator();
		while (tisEnItr.hasNext()) {
			Entry<String, Integer> entry = tisEnItr.next();
			Token token = new Token(jcas);
			token.setFrequency(entry.getValue());
			token.setText(entry.getKey());
			tokenListUnconv.add(token);
		}
		FSList theTokenList = Utils.fromCollectionToFSList(jcas, tokenListUnconv);
		// System.out.println(theTokenList);
		
		doc.setTokenList(theTokenList);
		theTokenList.addToIndexes(jcas);
	}

}
