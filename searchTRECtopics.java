package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import java.util.Comparator;


public class searchTRECtopics {
	
	// term -> hashmap of hashmap where inner hashmap has (document ID, frequency) and outer hashmap has (term, inner hashmap)
	static HashMap<String,HashMap<Integer,Integer>> term=new HashMap<String,HashMap<Integer,Integer>>();
	// result -> hashmap of (document number, score) 
	static HashMap<Integer,Double> result=null; 
	// DocLength -> document length for all documents
	static ArrayList<Float> DocLength=new ArrayList<Float>();
				
	//calculating the score for each query in arraylist a for every document and storing the result
	public static void getScore(ArrayList<LinkedHashSet<Term>> a,IndexReader reader,int N,int j) throws IOException
	{
		result=new HashMap<Integer,Double>();
		for(int docId = 0; docId < N; docId++){
			double fqdoc=0;
		for(Term t:a.get(j)){
			// kt -> total number of documents having the term t
			float kt=reader.docFreq(new Term("TEXT", t.text()));
			// idf -> inverse document frequency
			double idf=Math.log10(1+(N/kt));
			//ctd -> count of term t in document doc
			float ctd;
			if(term.get(t.text()).get(docId)!=null)
				ctd=term.get(t.text()).get(docId);
			else ctd=0;
			// l -> length of each document 
			float l=DocLength.get(docId);
			// tf -> term frequency	
			double tf=ctd/l;
			
			double tfidf=tf*idf;
			// fqdoc -> F(q,doc)
			fqdoc+=(tfidf);
			
		}
		result.put(docId,fqdoc);
		
		
	}
	}
	
	// finding the frequency of each term in each document and storing it 
	public static void findFreq(ArrayList<LinkedHashSet<Term>> a,LeafReaderContext leafContext, int startDocNo) throws IOException
	{
		int j=0;
		while(j<a.size()){
		for(Term t:a.get(j)){
		
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
			}
		}
		j++;
		}
		
	}
	
	//comparator function to sort end results and give top 1000 scores
	public static HashMap<Integer,Double> Comparator(HashMap<Integer,Double> mp){
		List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(mp.entrySet());
		Collections.sort(list, new Comparator<Entry<Integer, Double>>()
        {
            public int compare(Entry<Integer, Double> o1,Entry<Integer,Double> o2)
            {
            	 return o2.getValue().compareTo(o1.getValue());
            	
	}});
		 HashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
	        for (Entry<Integer, Double> entry : list)
	        {
	            sortedMap.put(entry.getKey(), entry.getValue());
	        }

	        return sortedMap;
		}
	// parser to parse queries
	public static String parseDoc(String f1,String f2,String DocText){
		int fromIndex = 0,a,b;
		String finalString = new String();
		while (DocText.indexOf(f1, fromIndex) >= 0) {
			a = DocText.indexOf(f1, fromIndex);
			b = DocText.indexOf(f2, fromIndex);
			a = a + f1.length();
			String t = DocText.substring(a, b);
			finalString += " " + t;
			fromIndex = b + f2.length(); 
		}
		return finalString;
	}

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		    BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Disha\\Documents\\topics.51-100"));
			StringBuffer str = new StringBuffer();
			Directory idir=FSDirectory.open(Paths.get("C:\\Users\\Disha\\Documents\\index"));
			IndexReader reader=DirectoryReader.open(idir);
			IndexSearcher searcher=new IndexSearcher(reader);
			// a1 -> query terms(linked hash set) for each short query
			ArrayList<LinkedHashSet<Term>> a1=new ArrayList<LinkedHashSet<Term>>();
			// a2 -> query terms(linked hash set) for each long query
			ArrayList<LinkedHashSet<Term>> a2=new ArrayList<LinkedHashSet<Term>>();
			// did -> document numbers(like AP-890101-001) for each document in the leaves
			ArrayList<String> did=new ArrayList<String>();
			File file1 = new File("C:\\Users\\Disha\\Desktop\\ClassicshortQuery.txt");
			FileOutputStream fos1=new FileOutputStream(file1);
			PrintWriter pw1=new PrintWriter(fos1);
			File file2 = new File("C:\\Users\\Disha\\Desktop\\ClassiclongQuery.txt");
			FileOutputStream fos2=new FileOutputStream(file2);
			PrintWriter pw2=new PrintWriter(fos2);

			String currentLine; 
			while ((currentLine = br.readLine()) != null) {
				str.append(currentLine);
				str.append('\n');
			}
		String s=str.toString();
		int x=51;
		String comp2="<narr>";
		LinkedHashSet<Term> shortQueryTerms = null;
		LinkedHashSet<Term> longQueryTerms = null;
		int a;
		while(x<=100)
		{
			if(x!=100)
				a = s.indexOf("0"+Integer.toString(x));
			else
				a =  s.indexOf(Integer.toString(x));
			int b = s.indexOf(comp2, a); 
			String DocText = s.substring(a + 5, b);
			String title =parseDoc("<title>","<desc>",DocText);
			String desc=parseDoc("<desc>","<smry>",DocText);
			title=title.replaceAll(" Topic: ", "");
			title=title.replaceAll("/", " ");
			desc=desc.replaceAll("Description:", "");
			desc=desc.replaceAll("/", " ");
			Analyzer analyzer = new StandardAnalyzer();
			String field="TEXT";
			QueryParser qp=new QueryParser(field,analyzer);
			Query q1=qp.parse(title);
			Query q2=qp.parse(desc);
			shortQueryTerms = new LinkedHashSet<Term>();
			longQueryTerms = new LinkedHashSet<Term>();
			a1.add(shortQueryTerms);
			a2.add(longQueryTerms);
			searcher.createNormalizedWeight(q1, false).extractTerms(shortQueryTerms);
			searcher.createNormalizedWeight(q2, false).extractTerms(longQueryTerms);
			x++;
		
	
		}
		
		int ctd=0;
		float l=0,kt=0;
		// N -> total number of documents
		int N=reader.maxDoc();
		int numberOfDoc=0,startDocNo=0;
		double tf = 0,idf,fqdoc=0;
		ClassicSimilarity dSimi=new ClassicSimilarity();
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
			
			findFreq(a1,leafContext,startDocNo);
			findFreq(a2,leafContext,startDocNo);
		
		}
		
				
		
			int j=0;
			while(j<a1.size()){
				
				getScore(a1,reader,N,j);
				HashMap<Integer,Double> newmap=Comparator(result);
				int re=0;
				//output to file for short queries
					for(HashMap.Entry<Integer,Double> m :newmap.entrySet()){
			            pw1.println((j+51)+" 0 "+did.get(m.getKey())+" "+(re+1)+" "+m.getValue()+" "+"Classic_short");
			            re++;
			            if(re==1000)
			            	break;
			        }

			      
				
				j++;
			
			}
			pw1.flush();
	        pw1.close();
	        fos1.close();
		
	        j=0;
			while(j<a2.size()){
				getScore(a2,reader,N,j);
				HashMap<Integer,Double> newmap=Comparator(result);
				int re=0;
					//output to file for long queries
					for(HashMap.Entry<Integer,Double> m :newmap.entrySet()){
			            pw2.println((j+51)+" 0 "+did.get(m.getKey())+" "+(re+1)+" "+m.getValue()+" "+"Classic_long");
			            re++;
			            if(re==1000)
			            	break;
			        }	      
				j++;
			}
			pw2.flush();
	        pw2.close();
	        fos2.close();

		
		
	}}



