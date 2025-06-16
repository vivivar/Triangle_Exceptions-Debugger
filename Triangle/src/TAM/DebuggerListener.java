/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TAM;

/**
 *
 * @author Asus
 */
public interface DebuggerListener {
    void onInstructionExecuted(int cp, int st, int sb, int lb, int ht);
    void update(int pc, int sb, int lb, int st, int ht);
}
