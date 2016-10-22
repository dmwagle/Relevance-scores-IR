package search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;


public class easySearch {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		System.out.println("Enter a Query: ");
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		String query=br.readLine();
		query=query.replaceAll("/", " ");
		Directory idir=FSDirectory.open(Paths.get("C:\\Users\\Disha\\Documents\\index"));
		Analyzer analyzer = new StandardAnalyzer();
		IndexReader reader=DirectoryReader.open(idir);
		IndexSearcher searcher=new IndexSearcher(reader);
		String field="TEXT";
		QueryParser qp=new QueryParser(field,analyzer);
		Query q=qp.parse(query);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(q, false).extractTerms(queryTerms);
		int ctd=0; 
		float l=0,kt=0;
		// N -> total number of documents
		int N=reader.maxDoc();
		int numberOfDoc=0,startDocNo=0;
		double tf = 0,idf,fqdoc=0;
		ClassicSimilarity dSimi=new ClassicSimilarity();
		// DocLength -> document length for all documents
		ArrayList<Float> DocLength=new ArrayList<Float>();
		// term -> hashmap of hashmap where inner hashmap has (document ID, frequency) and outer hashmap has (term, inner hashmap)
		HashMap<String,HashMap<Integer,Integer>> term=new HashMap<String,HashMap<Integer,Integer>>();
		// did -> document numbers(like AP-890101-001) for each document in the leaves
		ArrayList<String> did=new ArrayList<String>();
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		
		for (int i = 0; i < leafContexts.size(); i++) {
			LeafReaderContext leafContext = leafContexts.get(i);
			startDocNo = leafContext.docBase;
			numberOfDoc = leafContext.reader().maxDoc();
			for (int docId = 0; docId < numberOfDoc; docId++) {
				// Get normalized length (1/sqrt(numOfTokens)) of the document
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
				// Get length of the document
				float docLeng = 1 / (normDocLeng * normDocLeng);
				DocLength.add(docLeng);
				did.add(searcher.doc(docId+startDocNo).get("DOCNO"));

			}
					
			for(Term t : queryTerms){
			
			PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
			HashMap<Integer,Integer> freq=term.get(t.text());
			if(freq==null)
			{
			term.put(t.text(), freq=new HashMap<Integer,Integer>());
			}
			
			
			int doc;
			if (de != null) {
				while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
							
								freq.put(de.docID()+startDocNo, de.freq());
							
				}
			
			}}}
			for(int docId = 0; docId < N; docId++){
				fqdoc=0;
				for(Term t: queryTerms){
					// kt -> total number of documents having the term t
					kt=reader.docFreq(new Term(field, t.text()));
					// idf -> inverse document frequency
					idf=Math.log10(1+(N/kt));
					//ctd -> count of term t in document doc
					if(term.get(t.text()).get(docId)!=null)
						ctd=term.get(t.text()).get(docId);
					else ctd=0;
					// l -> length of each document 
					l=DocLength.get(docId);
					// tf -> term frequency	
					tf=ctd/l;
					double tfidf=tf*idf;
					// fqdoc -> F(q,doc)
					fqdoc+=(tfidf);
					
					System.out.println((docId+1)+" DocID: "+did.get(docId)+" for term: "+ t.text()+" F(t,doc):"+(tfidf));
					
				}
				System.out.print("For entire query...");
					System.out.println((docId+1)+" DocID: "+did.get(docId)+"  F(q,doc):"+(fqdoc));
			}
		
		
		
	}}



