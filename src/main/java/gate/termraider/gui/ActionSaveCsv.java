/*
 *  Copyright (c) 2008--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: ActionSaveCsv.java 17718 2014-03-20 20:40:06Z adamfunk $
 */
package gate.termraider.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import gate.gui.MainFrame;
import gate.termraider.bank.AbstractBank;
import gate.termraider.bank.AbstractPairbank;
import gate.termraider.gui.CsvFileSelectionActionListener.Mode;
import gate.termraider.util.Utilities;


/**
 * Action class for saving CSV from the GATE GUI.
 */
public class ActionSaveCsv
    extends AbstractAction {
    
  private static final long serialVersionUID = 8086944391438384470L;

  private AbstractBank termbank;

  public ActionSaveCsv(String label, AbstractBank termbank) {
    super(label);
    this.termbank = termbank;
  }

  public void actionPerformed(ActionEvent ae) {
    JDialog saveDialog = new JDialog(MainFrame.getInstance(), "Save Termbank as CSV", true);
    MainFrame.getGuiRoots().add(saveDialog);
    saveDialog.setLayout(new BorderLayout());
    SliderPanel sliderPanel = new SliderPanel(termbank, "save", true, null);
    
    JCheckBox cbDocuments = new JCheckBox("Include Per Document Counts");
    
    cbDocuments.setEnabled(!(termbank instanceof AbstractPairbank));
    
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.setOpaque(false);
    
    toolbar.add(sliderPanel);
    toolbar.add(cbDocuments);
    
    saveDialog.add(toolbar, BorderLayout.CENTER);

    JPanel chooserPanel = new JPanel();
    chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.Y_AXIS));
    chooserPanel.add(new JSeparator());
    
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", Utilities.EXTENSION_CSV);
    chooser.setFileFilter(filter);
    chooser.setApproveButtonText("Save");
    chooser.addActionListener(new CsvFileSelectionActionListener(chooser, termbank, sliderPanel, cbDocuments, saveDialog, Mode.SAVE));
    chooserPanel.add(chooser);
    saveDialog.add(chooserPanel, BorderLayout.SOUTH);
    saveDialog.pack();
    saveDialog.setLocationRelativeTo(saveDialog.getOwner());
    saveDialog.setVisible(true);
  }
}


