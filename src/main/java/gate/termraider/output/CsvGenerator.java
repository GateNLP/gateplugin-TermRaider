/*
 *  Copyright (c) 2010--2021, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: CsvGenerator.java 19571 2016-09-09 07:07:57Z markagreenwood $
 */
package gate.termraider.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

import gate.termraider.bank.AbstractBank;
import gate.termraider.bank.AbstractTermbank;
import gate.termraider.util.Term;
import gate.util.GateException;


public class CsvGenerator {
  
  public static void generateAndSaveCsv(AbstractTermbank bank, 
          Number threshold, File outputFile) throws GateException {
        
    RFC4180Parser parser = new RFC4180ParserBuilder()
        .withSeparator((char) ',')
        .withQuoteChar((char) '"')
        .build();

    try (ICSVWriter csvWriter = new CSVWriterBuilder(new FileWriter(outputFile))
        .withParser(parser)
	    .withLineEnd(ICSVWriter.RFC4180_LINE_END)
	    .build();) {

        addComment(bank, "threshold = " + threshold);
        List<Term> sortedTerms = bank.getTermsByDescendingScore();
    
        addComment(bank, "Unfiltered nbr of terms = " + sortedTerms.size());
        int written = 0;
        bank.writeCSVHeader(csvWriter);
    
        for (Term term : sortedTerms) {
            Number score = bank.getDefaultScores().get(term);
            if (score.doubleValue() >= threshold.doubleValue()) {
            	bank.writeCSVTermData(csvWriter, term);
                written++;
            }
            else {  // the rest must be lower
                break;
            }
        }
        addComment(bank, "Filtered nbr of terms = " + written);
    } catch (IOException e) {
		throw new GateException(e);
	}
  }

  
  private static void addComment(AbstractBank termbank, String commentStr) {
    if (termbank.getDebugMode()) {
      System.out.println(commentStr);
    }
  }  
}
