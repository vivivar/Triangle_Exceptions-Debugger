/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import TAM.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebuggerPanel extends JPanel {

    private JTextArea codeArea;
    private JTextField cpField, stField, sbField, lbField, htField;
    private JButton runButton;
    private Interpreter interpreter;

    public DebuggerPanel(Instruction[] code) {
        setLayout(new BorderLayout());

        // Panel superior: Código TAM
        codeArea = new JTextArea(20, 40);
        codeArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(codeArea);
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior: registros
        JPanel registersPanel = new JPanel(new GridLayout(1, 10));
        registersPanel.add(new JLabel("CP:"));
        cpField = new JTextField(5); cpField.setEditable(false);
        registersPanel.add(cpField);
        registersPanel.add(new JLabel("ST:"));
        stField = new JTextField(5); stField.setEditable(false);
        registersPanel.add(stField);
        registersPanel.add(new JLabel("SB:"));
        sbField = new JTextField(5); sbField.setEditable(false);
        registersPanel.add(sbField);
        registersPanel.add(new JLabel("LB:"));
        lbField = new JTextField(5); lbField.setEditable(false);
        registersPanel.add(lbField);
        registersPanel.add(new JLabel("HT:"));
        htField = new JTextField(5); htField.setEditable(false);
        registersPanel.add(htField);
        add(registersPanel, BorderLayout.SOUTH);

        // Panel de botones
        runButton = new JButton("Run TAM");
        add(runButton, BorderLayout.NORTH);

        // Inicializamos el interpreter
        interpreter = new Interpreter(code);
        interpreter.initializeMachine();

        // Mostrar el código TAM
        mostrarCodigo(code);

        // Acción del botón
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread runThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        interpreter.interpretProgram();
                        actualizarRegistros();
                    }
                });
                runThread.start();
            }
        });
    }

    private void mostrarCodigo(Instruction[] code) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length; i++) {
            if (code[i] != null) {
                sb.append(i).append(": ").append(code[i].toString()).append("\n");
            }
        }
        codeArea.setText(sb.toString());
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

