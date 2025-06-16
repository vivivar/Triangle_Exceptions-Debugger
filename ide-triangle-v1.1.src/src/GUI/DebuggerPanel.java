/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import TAM.DebuggerListener;
import TAM.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebuggerPanel extends JPanel implements DebuggerListener{

    private JTextArea codeArea;
    private JTextField cpField, stField, sbField, lbField, htField;
    private JButton runButton;
    private Interpreter interpreter;
    private JTextArea registersArea;
    private boolean ejecutando = false;
    private Instruction[] instructions;

    public DebuggerPanel(Instruction[] instructions) {
        this.instructions = instructions;

        setLayout(new BorderLayout());

        codeArea = new JTextArea();
        codeArea.setEditable(false);
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("Código TAM"));

        registersArea = new JTextArea();
        registersArea.setEditable(false);
        JScrollPane registersScroll = new JScrollPane(registersArea);
        registersScroll.setBorder(BorderFactory.createTitledBorder("Registros"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScroll, registersScroll);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);
    }
    
    public void loadInstructions(Instruction[] instructions) {
        this.instructions = instructions;
        displayInstructions();
    }

    private void displayInstructions() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.length; i++) {
            Instruction instr = instructions[i];
            sb.append(i).append(": ").append(instr.toString()).append("\n");
        }
        codeArea.setText(sb.toString());
    }
    
        // Actualiza los registros en vivo
    public void setRegisters(int pc, int sb, int lb, int st, int ht) {
        StringBuilder sbText = new StringBuilder();
        sbText.append("PC: ").append(pc).append("\n");
        sbText.append("SB: ").append(sb).append("\n");
        sbText.append("LB: ").append(lb).append("\n");
        sbText.append("ST: ").append(st).append("\n");
        sbText.append("HT: ").append(ht).append("\n");
        registersArea.setText(sbText.toString());
    }


    
    @Override
    public void onInstructionExecuted(int cp, int st, int sb, int lb, int ht) {
    SwingUtilities.invokeLater(() -> {
        cpField.setText(String.valueOf(cp));
        stField.setText(String.valueOf(st));
        sbField.setText(String.valueOf(sb));
        lbField.setText(String.valueOf(lb));
        htField.setText(String.valueOf(ht));

        highlightInstruction(cp);
    });
}

    private void highlightInstruction(int cp) {
        try {
            int start = codeArea.getLineStartOffset(cp);
            int end = codeArea.getLineEndOffset(cp);
            codeArea.select(start, end);
        } catch (Exception e) {
        }
    }

    private void mostrarCodigo(Instruction[] code) {
        if (code == null) {
            codeArea.setText("No hay código cargado.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length; i++) {
            if (code[i] != null) {
                sb.append(i).append(": ").append(code[i].toString()).append("\n");
            }
        }
        codeArea.setText(sb.toString());
    }
    public void loadCode(Instruction[] code) {
        mostrarCodigo(code);
    }
    
    public void update(int pc, int sb, int lb, int st, int ht) {
        setRegisters(pc, sb, lb, st, ht);
    }

    private void actualizarRegistros() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cpField.setText(Integer.toString(interpreter.CP));
                stField.setText(Integer.toString(interpreter.ST));
                sbField.setText(Integer.toString(interpreter.SB));
                lbField.setText(Integer.toString(interpreter.LB));
                htField.setText(Integer.toString(interpreter.HT));
            }
        });
    }
}

