/*
 *  Copyright (c) 2008--2021, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: PairCsvGenerator.java 18609 2015-03-25 15:50:04Z adamfunk $
 */
package gate.termraider.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

import gate.termraider.bank.AbstractPairbank;
import gate.termraider.util.Term;
import gate.termraider.util.TermPairComparatorByDescendingScore;
import gate.termraider.util.UnorderedTermPair;
import gate.util.GateException;

public class PairCsvGenerator {

	private boolean debugMode;

	public void generateAndSaveCsv(AbstractPairbank pairbank, Number threshold, File outputFile) throws GateException {

		this.debugMode = pairbank.getDebugMode();

		RFC4180Parser parser = new RFC4180ParserBuilder()
				.withSeparator(',')
				.withQuoteChar('"')
				.build();

		try (ICSVWriter csvWriter = new CSVWriterBuilder(new FileWriter(outputFile))
				.withParser(parser)
				.withLineEnd(ICSVWriter.RFC4180_LINE_END)
				.build();) {

			Map<UnorderedTermPair, Double> scores = pairbank.getScores();
			List<UnorderedTermPair> pairs = new ArrayList<UnorderedTermPair>(scores.keySet());
			Collections.sort(pairs, new TermPairComparatorByDescendingScore(scores));
			addComment("threshold = " + threshold);
			addComment("Unfiltered nbr of pairs = " + pairs.size());
			int written = 0;
			writeHeader(csvWriter, pairbank);
			for (UnorderedTermPair pair : pairs) {
				double score = scores.get(pair);
				if (score < threshold.doubleValue()) {
					break;
				}
				written++;
				
				writePairData(csvWriter, pair.getTerm0(), pair.getTerm1(), score, pairbank.getDocumentCount(pair),
						pairbank.getPairCount(pair));
			}

			addComment("Filtered nbr of pairs = " + written);

			addComment("Pairbank: saved CSV in " + outputFile.getAbsolutePath());

		} catch (IOException e) {
			throw new GateException(e);
		}
	}

	private void addComment(String commentStr) {
		if (debugMode) {
			System.err.println(commentStr);
		}
	}
	
	private void writePairData(ICSVWriter writer, Term t0, Term t1, Double score, Integer documents, Integer frequency) {

		String[] row = new String[9];

		row[0] = t0.getTermString();
		row[1] = t0.getLanguageCode();
		row[2] = t0.getType();
		row[3] = t1.getTermString();
		row[4] = t1.getLanguageCode();
		row[5] = t1.getType();
		row[6] = score.toString();
		row[7] = documents.toString();
		row[8] = frequency.toString();

		writer.writeNext(row, false);
	}

	private void writeHeader(ICSVWriter writer, AbstractPairbank pairbank) {
		writer.writeNext(new String[] { "Term", "Lang", "Type", "Term", "Lang", "Type", pairbank.getScoreProperty(),
				"DocFrequency", "Frequency" }, false);
	}

}
