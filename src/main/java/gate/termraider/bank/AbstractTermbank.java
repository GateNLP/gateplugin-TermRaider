/*
 *  Copyright (c) 2008--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: AbstractTermbank.java 20039 2017-02-01 06:34:45Z markagreenwood $
 */
package gate.termraider.bank;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Action;

import com.opencsv.ICSVWriter;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.gui.ActionsPublisher;
import gate.termraider.gui.ActionSaveCsv;
import gate.termraider.output.CsvGenerator;
import gate.termraider.util.DocumentIdentifier;
import gate.termraider.util.ScoreType;
import gate.termraider.util.Term;
import gate.termraider.util.TermComparatorByDescendingScore;
import gate.util.GateException;



public abstract class AbstractTermbank extends AbstractBank 
    implements ActionsPublisher  {

  private static final long serialVersionUID = -1044054380153036770L;
  
  // additional CREOLE init parameters
  protected Set<String> inputAnnotationTypes;
  protected String idDocumentFeature;

  // transient to allow serialization
  protected transient List<Action> actionsList;
  
  protected Map<ScoreType, Map<Term, Number>> scores;
  protected Map<Term, Set<DocumentIdentifier>>  termDocuments;
  public static final String RAW_SUFFIX = ".raw";
  
  private List<Term> termsByDescendingScore;
  protected boolean  descendingScoresDone = false;
  
  protected List<ScoreType> scoreTypes;
  private Number minDefaultScore, maxDefaultScore;


  public Resource init() throws ResourceInstantiationException {
    prepare();
    initializeScoreTypes();
    // Above method must be set in each subclass;
    // now we check it has been done.
    if (this.scoreTypes.size() == 0) {
      throw new ResourceInstantiationException("No score types found in " + this.toString());
    }
    resetScores();
    processCorpora();
    calculateScores();
    return this;
  }
  

  public void cleanup() {
    super.cleanup();
  }
  
  
  public List<ScoreType> getScoreTypes() {
    return this.scoreTypes;
  }
  

  public Number getScore(ScoreType type, Term term) {
    Map<Term, Number> ss = this.getScores(type);
    if (ss.containsKey(term)) {
      return ss.get(term);
    }
    
    // implied else
    return 0;
  }


  public Collection<Term> getTerms() {
    return this.getDefaultScores().keySet();
  }
  
  
  public ScoreType getDefaultScoreType() {
    return this.scoreTypes.get(0);
  }
  
  
  public Map<Term, Number> getDefaultScores() {
    return this.scores.get(getDefaultScoreType());
  }
  
  
  protected abstract void initializeScoreTypes();
  
  
  public List<Term> getTermsByDescendingScore() {
    // lazy computation
    if (! descendingScoresDone) {
      termsByDescendingScore = new ArrayList<Term>(this.getTerms());
      Collections.sort(termsByDescendingScore, new TermComparatorByDescendingScore(scores.get(this.getDefaultScoreType())));
      descendingScoresDone = true;
    }
    return this.termsByDescendingScore;
  }
  

  public Map<Term, Set<DocumentIdentifier>> getTermDocuments() {
    return this.termDocuments;
  }
  
  
  public Map<ScoreType, Number> getScoreMap(Term term) {
    Map<ScoreType, Number> result = new HashMap<ScoreType, Number>();
    for (ScoreType st : this.scoreTypes) {
      result.put(st, this.scores.get(st).get(term));
    }
    return result;
  }
  
  
  public Map<Term, Number> getScores(ScoreType st) {
    return this.scores.get(st);
  }
  
  
  public Set<DocumentIdentifier> getDocumentsForTerm(Term term) {
    if (this.termDocuments.containsKey(term)) {
      return this.termDocuments.get(term);
    }
    
    // implied else: empty set
    return new HashSet<DocumentIdentifier>();
  }
  
  
  private void findMinAndMaxDefaultScores() {
    Collection<Number> values = this.getDefaultScores().values();
    if (values.isEmpty()) {
      minDefaultScore = 0;
      maxDefaultScore = 1;
    }
    else {
      minDefaultScore = values.iterator().next();
      maxDefaultScore = values.iterator().next();
      for (Number n : values) {
        if (n.doubleValue() < minDefaultScore.doubleValue()) {
          minDefaultScore = n;
        }
        
        if (n.doubleValue() > maxDefaultScore.doubleValue()) {
          maxDefaultScore = n;
        }
      }
    }
  }
  
  
  public Number getMinScore() {
    // lazy calculation
    if (minDefaultScore == null) {
      findMinAndMaxDefaultScores();
    }
    return minDefaultScore;
  }

  
  public Number getMaxScore() {
    // lazy calculation
    if (maxDefaultScore == null) {
      findMinAndMaxDefaultScores();
    }
    return maxDefaultScore;
  }
  
  
  protected void prepare() throws ResourceInstantiationException {
    if ( (corpora == null) || (corpora.size() == 0) ) {
      throw new ResourceInstantiationException("No corpora given");
    }
    
    this.types = new TreeSet<String>();
    this.languages = new TreeSet<String>();
  }
  
  protected void createActions() {
    actionsList = new ArrayList<Action>();
    actionsList.add(new ActionSaveCsv("Save as CSV...", this));
  }
  
  
  protected void processCorpora() {
    for (Corpus corpus : corpora) {
      processCorpus(corpus);
      if (debugMode) {
        System.out.println("Termbank " + this.getName() + ": added corpus " + corpus.getName() + " with " + corpus.size() + " documents");
      }
    }
  }
  
  
  protected void processCorpus(Corpus corpus) {
    for (int i=0 ; i < corpus.size() ; i++) {
      boolean wasLoaded = corpus.isDocumentLoaded(i);
      Document document = corpus.get(i);
      
      processDocument(document, i);

      // datastore safety
      if (! wasLoaded) {
        corpus.unloadDocument(document);
        Factory.deleteResource(document);
      }
    }
  }
  
  

  /* BEHOLD THE GUBBINS to distinguish the various types of Termbanks */

  protected abstract void resetScores();

  protected abstract void processDocument(Document document, int index);
  
  /**
   * This also needs to fill types and languages
   */
  protected abstract void calculateScores();
  
  public abstract Map<String, String> getMiscDataForGui();
  
  
  /* Methods for saving as CSV */
  
  public void saveAsCsv(Number threshold, File outputFile, boolean documentDetails) throws GateException {
    CsvGenerator.generateAndSaveCsv(this, threshold, outputFile, documentDetails);
  }

  /**
   * Convenience method to save everything in the termbank.
   * @param outputFile the file to save the termbank into
   * @throws GateException if an error occurs saving the termbank
   */
  public void saveAsCsv(File outputFile, boolean documentDetails) throws GateException {
    saveAsCsv(this.getMinScore(), outputFile, documentDetails);
  }
  
  
  @Override
  public List<Action> getActions() {
    // lazy instantiation because actionsList is transient
    if (actionsList == null) {
      createActions();
    }
    
    return this.actionsList;
  }

  public void writeCSVHeader(ICSVWriter csvWriter, boolean documentDetails) {
    List<String> row = new ArrayList<String>();
	  
    row.add("Term");
    row.add("Lang");
    row.add("Type");
    
    for (ScoreType type : this.scoreTypes) {
        row.add(type.toString());
    }
    
    if (documentDetails) {
        row.add("documentID");
        row.add("termFrequency");
    }
    
    csvWriter.writeNext(row.toArray(new String[row.size()]),false);
    
    writeCSVSubHeader(csvWriter);
  }

  /**
   * TODO: This is not right (columns).
   * Should be overridden as necessary, for totals etc.
   */
  protected void writeCSVSubHeader(ICSVWriter csvWriter) {
	  
	String[] row = new String[] {"","","","",""};
	row[1] = "_TOTAL_DOCS_";
	row[4] = Integer.toString(this.getDocumentCount());
	
	csvWriter.writeNext(row,false);
  }

  public void writeCSVTermData(ICSVWriter csvWriter, Term term) {
	  
	  List<String> row = new ArrayList<String>();
	  
	  row.add(term.getTermString());
	  row.add(term.getLanguageCode());
	  row.add(term.getType());
	  
	  for (ScoreType type : this.scoreTypes) {
		  row.add(this.getScore(type, term).toString());
	  }
	  
	  csvWriter.writeNext(row.toArray(new String[row.size()]),false);
  }
  
public void writeCSVTermDocumentData(ICSVWriter csvWriter, Term term) {
	  
	  for (DocumentIdentifier docID : getTermDocuments().get(term)) {
	  
		  List<String> row = new ArrayList<String>();
		  
		  row.add(term.getTermString());
		  row.add(term.getLanguageCode());
		  row.add(term.getType());
		  
		  for (ScoreType type : this.scoreTypes) {
			  row.add(this.getScore(type, term).toString());
		  }
		  
		  row.add(docID.getIdentifier());
		  row.add(Integer.toString(docID.getIndex()));
		  
		  csvWriter.writeNext(row.toArray(new String[row.size()]),false);
	  }
  }

  @CreoleParameter(comment = "input annotation types",
          defaultValue = "SingleWord;MultiWord")
  public void setInputAnnotationTypes(Set<String> names) {
    this.inputAnnotationTypes = names;
  }
  
  public Set<String> getInputAnnotationTypes() {
    return this.inputAnnotationTypes;
  }
  

  @CreoleParameter(comment = "doc feature to use for identification (blank = use sourceURL)",
          defaultValue = "")
  public void setIdDocumentFeature(String name) {
    this.idDocumentFeature = name;
  }
  
  public String getIdDocumentFeature() {
    return this.idDocumentFeature;
  }

  
  
}
