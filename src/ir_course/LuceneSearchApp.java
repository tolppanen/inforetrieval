/*
 * Skeleton class for the Lucene search program implementation
 *
 * Created on 2011-12-21
 * * Jouni Tuominen <jouni.tuominen@aalto.fi>
 * 
 * Modified on 2015-30-12
 * * Esko Ikkala <esko.ikkala@aalto.fi>
 * 
 * Assignment solution on 2016-03-06
 * * Antti Tolppanen 289795 <antti.tolppanen@aalto.fi>
 * 
 * 
 */
package ir_course;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneSearchApp {

	public LuceneSearchApp() {

	}

	StandardAnalyzer analyzer;
	Directory index;
	IndexWriterConfig config;

	public static void addDoc(IndexWriter w, DocumentInCollection feedItem)
			throws IOException {
		Document doc = new Document();
		doc.add(new TextField("title", feedItem.getTitle(), Field.Store.YES));
		doc.add(new TextField("abstract", feedItem.getAbstractText(),
				Field.Store.YES));
		doc.add(new IntField("taskNumber", feedItem.getSearchTaskNumber(),
				Field.Store.YES));
		doc.add(new TextField("query", feedItem.getQuery(), Field.Store.YES));
		doc.add(new TextField("relevance", feedItem.isRelevant() ? "1" : "0",
				Field.Store.YES));
		w.addDocument(doc);
	}

	public void index(List<DocumentInCollection> docs) throws IOException {
		analyzer = new StandardAnalyzer();
		index = new RAMDirectory();
		config = new IndexWriterConfig(analyzer);

		IndexWriter writer = new IndexWriter(index, config);
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).getSearchTaskNumber() == 3)
				addDoc(writer, docs.get(i));
		}
		writer.close();
	}

	public List<String> search(List<String> inTitle, List<String> notInTitle,
			List<String> inAbstract, List<String> notinAbstract,
			Integer taskNr, String endDate, List<String> relevant)
			throws IOException {

		printQuery(inTitle, notInTitle, inAbstract, notinAbstract, taskNr,
				endDate, relevant);

		List<String> results = new LinkedList<String>();
		BooleanQuery bq = new BooleanQuery();

		// InTitle search
		if (inTitle != null) {
			for (int k = 0; k < inTitle.size(); k++) {
				TermQuery tq = new TermQuery(new Term("title", inTitle.get(k)));
				bq.add(tq, BooleanClause.Occur.MUST);
			}
		}

		// NotInTitle search
		if (notInTitle != null) {
			for (int k = 0; k < notInTitle.size(); k++) {
				TermQuery tq = new TermQuery(new Term("title",
						notInTitle.get(k)));
				bq.add(tq, BooleanClause.Occur.MUST_NOT);
			}
		}

		// InAbstract search
		if (inAbstract != null) {
			for (int k = 0; k < inAbstract.size(); k++) {
				TermQuery tq = new TermQuery(new Term("abstract",
						inAbstract.get(k)));
				bq.add(tq, BooleanClause.Occur.MUST);
			}
		}

		// NotinAbstract search
		if (notinAbstract != null) {
			for (int k = 0; k < notinAbstract.size(); k++) {
				TermQuery tq = new TermQuery(new Term("abstract",
						notinAbstract.get(k)));
				bq.add(tq, BooleanClause.Occur.MUST_NOT);
			}
		}

		// Relevancy serach
		if (relevant != null) {
			for (int k = 0; k < relevant.size(); k++) {
				TermQuery tq = new TermQuery(new Term("relevance",
						relevant.get(k)));
				bq.add(tq, BooleanClause.Occur.MUST);
			}
		}

		DirectoryReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(30);

		searcher.search(bq, collector);

		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		for (int i = 0; i < docs.length; i++) {
			Document result = searcher.doc(docs[i].doc);
			results.add("Score:" + " "  + docs[i].score + " - " + result.toString());
		}

		return results;
	}

	public void printQuery(List<String> inTitle, List<String> notInTitle,
			List<String> inAbstract, List<String> notinAbstract,
			Integer taskNr, String endDate, List<String> relevant) {
		System.out.println("Search (");
		if (inTitle != null) {
			System.out.println("in title: " + inTitle);
			if (notInTitle != null || inAbstract != null
					|| notinAbstract != null || taskNr != null
					|| endDate != null)
				System.out.println("; ");
		}
		if (notInTitle != null) {
			System.out.print("not in title: " + notInTitle);
			if (inAbstract != null || notinAbstract != null || taskNr != null
					|| endDate != null)
				System.out.print("; ");
		}
		if (inAbstract != null) {
			System.out.print("in abstract: " + inAbstract);
			if (notinAbstract != null || taskNr != null || endDate != null)
				System.out.print("; ");
		}
		if (notinAbstract != null) {
			System.out.print("not in description: " + notinAbstract);
			if (taskNr != null || endDate != null)
				System.out.print("; ");
		}
		if (relevant != null) {
			System.out.print("relevancy: " + relevant);
			if (taskNr != null)
				System.out.print("; ");
		}

	}

	public void printResults(List<String> results) {
		if (results.size() > 0) {
			//Collections.sort(results);
			for (int i = 0; i < results.size(); i++) {
				System.out.println(" " + (i + 1) + ". " + results.get(i));
				System.out.println("=========================================");
			}
		} else
			System.out.println(" no results");
	}
	


	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			LuceneSearchApp engine = new LuceneSearchApp();

			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> docs = parser.getDocuments();

			engine.index(docs);

			List<String> inTitle;
			List<String> notInTitle;
			List<String> inAbstract;
			List<String> notinAbstract;
			List<String> results;
			List<String> isRelevant;

			inAbstract = new LinkedList<String>();
			isRelevant = new LinkedList<String>();
			inAbstract.add("content");
			inAbstract.add("based");
			inAbstract.add("video");
			inAbstract.add("annotation");
			isRelevant.add("1");

			results = engine.search(null, null, inAbstract, null, null, null,
					isRelevant);
			engine.printResults(results);

		} else
			System.out
					.println("ERROR: the path of a RSS Feed file has to be passed as a command line argument.");
	}
}
