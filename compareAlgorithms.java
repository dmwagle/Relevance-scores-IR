package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.LinkedHashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {

	// parsing the queries
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
	
	//calculating the top document scores using a language model
	public static void find(IndexSearcher searcher,String s,Analyzer analyzer,String sim) throws ParseException, IOException
	{
		File file1 = new File("C:\\Users\\Disha\\Desktop\\"+sim+"shortQuery.txt");
		FileOutputStream fos1=new FileOutputStream(file1);
		PrintWriter pw1=new PrintWriter(fos1);
		File file2 = new File("C:\\Users\\Disha\\Desktop\\"+sim+"longQuery.txt");
		FileOutputStream fos2=new FileOutputStream(file2);
		PrintWriter pw2=new PrintWriter(fos2);

		int fromIndex=0;
		int x=51;
		String comp2="<narr>";
		int a,id=0;
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
			
			String field="TEXT";
			QueryParser qp=new QueryParser(field,analyzer);
			Query q1=qp.parse(title);
			Query q2=qp.parse(desc);
			//a1.add(q1);
			//a2.add(q2);
			TopDocs results1 = searcher.search(q1, 1000);
			TopDocs results2 = searcher.search(q2, 1000);
			ScoreDoc[] hits1 = results1.scoreDocs;
			ScoreDoc[] hits2 = results2.scoreDocs;
			for(int i=0;i<hits1.length;i++){	
				Document doc=searcher.doc(hits1[i].doc);	
				pw1.println(x+" 0 "+doc.get("DOCNO")+" "+(i+1)+" "+hits1[i].score+" "+sim+"_short");
			}
			
			for(int i=0;i<hits2.length;i++){	
				Document doc=searcher.doc(hits2[i].doc);
				pw2.println(x+" 0 "+doc.get("DOCNO")+" "+(i+1)+" "+hits2[i].score+" "+sim+"_long");
			}
			
			x++;
		

		}
		pw1.flush();
        pw1.close();
        fos1.close();
        pw2.flush();
        pw2.close();
        fos2.close();
	

		
	}
	public static void main(String[] args) throws Exception {
		
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Disha\\Documents\\topics.51-100"));
		StringBuffer str = new StringBuffer();
		String index = "C:\\Users\\Disha\\Documents\\index";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		String currentLine; 
		while ((currentLine = br.readLine()) != null) {
			str.append(currentLine);
			str.append('\n');
		}
	String s=str.toString();
	searcher.setSimilarity(new BM25Similarity());
	find(searcher,s,analyzer,"BM25");
	searcher.setSimilarity(new ClassicSimilarity());
	find(searcher,s,analyzer,"VectorSpace");
	searcher.setSimilarity(new LMDirichletSimilarity());
	find(searcher,s,analyzer,"LMDirichlet");
	searcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
	find(searcher,s,analyzer,"LMJelinekMercer");
		reader.close();
	}
}
