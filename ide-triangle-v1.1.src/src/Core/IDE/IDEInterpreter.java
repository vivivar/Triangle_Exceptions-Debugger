/*
 * IDE-Triangle v1.0
 * IDEInterpreter.java
 */

package Core.IDE;
import GUI.FileFrame;
import TAM.Interpreter;
import TAM.Machine;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

/**
 * Just another small class to call the Triangle interpreter.
 *
 * @author Luis Leopoldo Perez <luiperpe@ns.isi.ulatina.ac.cr>
 */
public class IDEInterpreter {
    
    // <editor-fold defaultstate="collapsed" desc=" Methods ">    
    private FileFrame fileFrame;  
    private ActionListener delegate;

    /**
     * Creates a new instance of IDEInterpreter.
     * @param _delegate Event to be fired when the thread stops running.
     */
    public IDEInterpreter(FileFrame fileFrame, ActionListener _delegate) {
        this.delegate = _delegate;
        this.fileFrame = fileFrame;
    }
    
    /**
     * Runs the Interpreter static main method as a separate thread.
     * @param fileName Path to the TAM Object File.
     */       
    public synchronized void Run(final String fileName) {
        new Thread(new Runnable() {
            public void run() {
                TAM.Interpreter.main(new String[] {fileName});
                delegate.actionPerformed(null);
            }
        }).start();
    }      
    
    
}
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Attributes ">
    // </editor-fold>

