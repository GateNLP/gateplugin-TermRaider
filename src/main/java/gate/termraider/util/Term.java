/*
 *  Copyright (c) 2010--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: Term.java 20039 2017-02-01 06:34:45Z markagreenwood $
 */
package gate.termraider.util;

import gate.Annotation;
import gate.Document;
import gate.FeatureMap;

import java.io.Serializable;


public class Term  implements Comparable<Term>, Serializable {
  
  private static final long serialVersionUID = -4849144989013687570L;
  
  private String termString, languageCode, type;
  private int hashCode;
  private String toString;
  

  public Term(String termString, String languageCode, String type) {
    this.termString = termString;
    this.languageCode = languageCode;
    this.type = type;
    this.setup();
  }

  
  public  Term(Annotation annotation, Document document, String languageFeature,
      String stringFeature) {
    this.type = annotation.getType();
    this.termString = Term.getFeatureOrString(document, annotation, stringFeature);
    this.languageCode = Term.getLanguage(annotation, languageFeature);
    this.setup();
  }


  private void setup() {
    if (languageCode == null) {
      languageCode = "";
    }

    hashCode = termString.hashCode() + languageCode.hashCode() + type.hashCode();

    if (languageCode.isEmpty()) {
      toString = termString + " (" + type + ")";
    }
    else {
      toString = termString + " (" + languageCode + "," + type + ")";
    }
  }
  

  public String toString() {
    return toString;
  }
  
  public String getTermString() {
    return this.termString;
  }
  
  public String getLanguageCode() {
    return this.languageCode;
  }
  
  public String getType() {
    return this.type;
  }
  
  
  public boolean equals(Object other) {
    return (other instanceof Term) && 
      this.termString.equals(((Term) other).termString) &&
      this.languageCode.equals(((Term) other).languageCode) &&
      this.type.equals(((Term) other).type);
  }
  
  public int hashCode() {
    return hashCode;
  }
  

  /**
   * To determine whether a match from a DF table is usable.
   * Type and string must match; language code is ignored if either
   * is blank, but must match if both are non-blank.
   * @param other the other Term to compare against
   * @return true if the terms match close enough, false otherwise
   */
  public boolean closeMatch(Term other) {
    if (! this.termString.equals(other.termString)) {
      return false;
    }
    
    if (! this.type.equals(other.type)) {
      return false;
    }
    
    if ("".equals(this.languageCode) || "".equals(other.languageCode) ) {
      return true;
    }
    
    return this.languageCode.equals(other.languageCode);
  }
  
  
  
  /**
   * This is used for alphabetical sorting.  The Term instance
   * does not know what its score is.
   */
  public int compareTo(Term other)  {
    int comp = this.getTermString().compareTo(other.getTermString());
    if (comp != 0) {
      return comp;
    }

    comp = this.getLanguageCode().compareTo(other.getLanguageCode());
    if (comp != 0) {
      return comp;
    }
    
    comp = this.getType().compareTo(other.getType());
    return comp;
  }
  
  
  
  public static String getLanguage(Annotation annotation, String languageFeature) {
    String language = "";
    if (annotation.getFeatures().containsKey(languageFeature)) {
      language = annotation.getFeatures().get(languageFeature).toString();
    }
    return language;
  }

  
  public static String getFeatureOrString(Document document, Annotation annotation, String key) {
    FeatureMap fm = annotation.getFeatures();
    if (fm.containsKey(key)) {
      return fm.get(key).toString();
    }
    // implied else
    return gate.Utils.cleanStringFor(document, annotation);
  }
  
}
