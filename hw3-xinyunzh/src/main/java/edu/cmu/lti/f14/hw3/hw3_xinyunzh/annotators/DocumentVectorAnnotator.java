package edu.cmu.lti.f14.hw3.hw3_xinyunzh.annotators;

import java.io.StringReader;
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
import edu.cmu.lti.f14.hw3.hw3_xinyunzh.utils.StanfordLemmatizer;
import edu.cmu.lti.f14.hw3.hw3_xinyunzh.utils.Utils;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	private static int NLPTOK = 1;
	private static int STALEM = 2;
	private static int WHISPA = 0;
	
	

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
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
	 * This method will tokenize by using one of three different tokenizer (Stanford CoreNLP, 
	 * Stanford Lemma, Naive White Spaced) according to parameter 'option' 
	 * 
	 * @param docText The untokenized text of document.
	 * 
	 * @param options The option to specify the target tokenizer
	 * 
	 * @return The output list of tokens.
	 */
	private List<String> tokenProcess(String docText, int options) {
		List<String> tokenStringList = null;
		switch (options) {
		case 0: {
			tokenStringList = tokenize0(docText);
		}
			break;
		case 1: {
			TokenizerFactory<Word> factory = PTBTokenizerFactory
					.newTokenizerFactory();

			Tokenizer<Word> tokenizer = factory.getTokenizer(new StringReader(
					docText));

			List<Word> tokenWordList = tokenizer.tokenize();
			Iterator<Word> tkIter = tokenWordList.iterator();
			tokenStringList = new ArrayList<String>();
			while (tkIter.hasNext()) {
				tokenStringList.add(tkIter.next().word());
			}
		}
		case 2: {
			String stemTextInfo = StanfordLemmatizer.stemText(docText);
			String [] stemTextArray = stemTextInfo.split(" ");
			tokenStringList = new ArrayList<String>();
			for (String stem : stemTextArray) {
				tokenStringList.add(stem);
			}
		}
		}
		return tokenStringList;
	}

	/**
	 * This function will create a Term Frequency Vector according to the time
	 * that one token occurs in one sentence.
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();

		List<String> tokenStringList;

		HashMap<String, Integer> tokHM = new HashMap<String, Integer>();
		tokenStringList = tokenProcess(docText, WHISPA);
		
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
		FSList theTokenList = Utils.fromCollectionToFSList(jcas,
				tokenListUnconv);
		// System.out.println(theTokenList);

		doc.setTokenList(theTokenList);
		theTokenList.addToIndexes(jcas);
	}

}
