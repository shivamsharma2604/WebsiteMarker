package view;

import model.Assignment;
import model.Model;
import model.TestResult;
import model.Testable;
import org.junit.Test;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.lang.reflect.Array;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Displays the results of the tests.
 */
public class ResultsPane extends JPanel {

    Model model;
    JFrame frame;
    Controller controller;

    JTable studentTable;
    JTable resultsTable;
    JTextPane evidencePane;

    int selectedStudent = 0;
    int selectedTestResult = 0;

    public ResultsPane(Model model, JFrame frame, Controller controller) {
        this.model = model;
        this.frame = frame;
        this.controller = controller;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        studentTable = createStudentTable();
        resultsTable = createResultTable();
        redraw();
    }

    public void redraw() {
        removeAll();

        //ScrollPane
        JScrollPane studentPane = new JScrollPane();
        studentTable.setModel(getStudentData());
        studentPane.setViewportView(studentTable);
        studentPane.setPreferredSize(new Dimension(UI.WIDTH * 2 / 3, UI.HEIGHT * 1/2)); //The scroll bars appear when preferred size <
        this.add(studentPane);

        JScrollPane resultsPane = new JScrollPane();
        resultsTable.setModel(getResultData());
        resultsPane.setViewportView(resultsTable);
        resultsPane.setPreferredSize(new Dimension(UI.WIDTH * 2 / 3, UI.HEIGHT * 1/2));
        this.add(resultsPane);

        JScrollPane scrollPane = new JScrollPane();
        evidencePane = createEvidencePane();

        DefaultCaret caret = (DefaultCaret) evidencePane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        evidencePane.setText(getEvidenceString());
        scrollPane.setViewportView(evidencePane);
        scrollPane.setPreferredSize(new Dimension(UI.WIDTH * 2 / 3, UI.HEIGHT * 1/2));
        this.add(scrollPane);

        //this.add(this.createOverallGradePane());


        revalidate();
        repaint();
    }

    private JTable createStudentTable() {
        JTable table = new JTable(getStudentData());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            selectedStudent = table.getSelectedRow();
            DefaultTableModel model = getResultData();
            resultsTable.setModel(model);
            evidencePane.setText(getEvidenceString());
            resultsTable.changeSelection(selectedTestResult, 0, true, false);
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedStudent = table.rowAtPoint(evt.getPoint());
                DefaultTableModel model = getResultData();
                resultsTable.setModel(model);
                evidencePane.setText(getEvidenceString());
                resultsTable.changeSelection(selectedTestResult, 0, true, false);
            }
        });
        return table;
    }

    private JTable createResultTable() {
        DefaultTableModel resultsData = getResultData();
        JTable table = new JTable();
        table.setModel(resultsData);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            if(table.getSelectedRow() == -1) return;
            selectedTestResult =  table.getSelectedRow();
            evidencePane.setText(getEvidenceString());
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedTestResult = table.rowAtPoint(evt.getPoint());
                evidencePane.setText(getEvidenceString());
            }
        });
        return table;
    }

    /**
     * Creates evidence pane. Not necessary, but keeps convention of calling a create method.
     * @return  the created text pane
     */
    private JTextPane createEvidencePane() {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        return pane;
    }

    private DefaultTableModel getResultData() {
        ArrayList<Assignment> assignments = model.getAssignments();
        ArrayList<TestResult> testResults = new ArrayList<>();

        if(selectedStudent == -1) selectedStudent = 0;  //Ensures at least one student is always open

        if (!assignments.isEmpty() && !assignments.get(0).getResults().isEmpty()){
            testResults = assignments.get(selectedStudent).getResults();
        }

        String[][] data = new String[testResults.size()][2];
        for (int i = 0; i < testResults.size(); i++) {
            data[i][0] = testResults.get(i).getTestName();    //get name
            data[i][1] = String.valueOf(testResults.get(i).getResult());   //get result
        }

        String[] columns = new String[]{"Test", "Result"};
        return new DefaultTableModel(data, columns);
    }

    private DefaultTableModel getStudentData() {
        ArrayList<Assignment> assignments = model.getAssignments();

        String[][] data = new String[assignments.size()][2];

        for (int i = 0; i < assignments.size(); i++) {
            //System.out.println(assignments.get(i).getNameID());
            data[i][0] = assignments.get(i).getNameID();    //get name
            data[i][1] = String.valueOf(assignments.get(i).getTotalPercentage());
        }

        String[] columns = new String[]{"Student", "Total"};

        //DefaultTableModel studentData = new DefaultTableModel();
        //studentData.addColumn("Name-ID", data);
        return new DefaultTableModel(data, columns);
    }

    private String getEvidenceString(){
        if(model.getAssignments().isEmpty() || selectedStudent == -1 || model.getAssignments().get(selectedStudent).getResults().isEmpty()) return "";

        TestResult result = model.getAssignments().get(selectedStudent).getResults().get(selectedTestResult);

        String evidence = "";
        evidence += result.getTestName() + "\n\n";
        evidence += result.getEvidenceLog();
        return evidence;
    }
}
