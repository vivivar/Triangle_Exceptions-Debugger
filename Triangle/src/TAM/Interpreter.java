/*
 * @(#)Interpreter.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 *
 */

package TAM;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import TAM.Instruction;
import TAM.DebuggerListener;

import javax.swing.*;
import java.awt.*;

public class Interpreter {
    

    
  public Interpreter(Instruction[] code) {
    this.code = code;
  }


  static String objectName;
    public static int PC;
    public static int CB;
    public static int SB;
    public static int ST;
    public static int LB;
  private static Instruction[] code;

  private static DebuggerWindow debugger;


// DATA STORE

  private static final int datasize = 4096;  
  private static int[] data = new int[datasize];


// DATA STORE REGISTERS AND OTHER REGISTERS

  final static int
    HB = 1024;  // = upper bound of data array + 1

 public static int
    CT, CP, HT, status;

  // status values
  final static int
    running = 0, halted = 1, failedDataStoreFull = 2, failedInvalidCodeAddress = 3,
    failedInvalidInstruction = 4, failedOverflow = 5, failedZeroDivide = 6,
    failedIOError = 7;

  static long
    accumulator;

  static int content (int r) {
    // Returns the current content of register r,
    // even if r is one of the pseudo-registers L1..L6.

    switch (r) {
      case Machine.CBr:
        return CB;
      case Machine.CTr:
        return CT;
      case Machine.PBr:
        return Machine.PB;
      case Machine.PTr:
        return Machine.PT;
      case Machine.SBr:
        return SB;
      case Machine.STr:
        return ST;
      case Machine.HBr:
        return HB;
      case Machine.HTr:
        return HT;
      case Machine.LBr:
        return LB;
      case Machine.L1r:
        return data[LB];
      case Machine.L2r:
        return data[data[LB]];
      case Machine.L3r:
        return data[data[data[LB]]];
      case Machine.L4r:
        return data[data[data[data[LB]]]];
      case Machine.L5r:
        return data[data[data[data[data[LB]]]]];
      case Machine.L6r:
        return data[data[data[data[data[data[LB]]]]]];
      case Machine.CPr:
        return CP;
      default:
        return 0;
    }
  }
  
public void initializeMachine() {
    CP = 0;
    ST = 0;
    SB = 0;
    LB = 0;
    HT = Machine.HEAPSIZE;
    status = running;

    for (int i = 0; i < data.length; i++) {
        data[i] = 0;
    }
}
  
  
    


// PROGRAM STATUS

  static void dump() {
    // Writes a summary of the machine state.
    int
      addr, staticLink, dynamicLink,
      localRegNum;

    System.out.println ("");
    System.out.println ("State of data store and registers:");
    System.out.println ("");
    if (HT == HB)
      System.out.println("            |--------|          (heap is empty)");
    else {
      System.out.println("       HB-->");
      System.out.println("            |--------|");
      for (addr = HB - 1; addr >= HT; addr--) {
        System.out.print(addr + ":");
        if (addr == HT)
          System.out.print(" HT-->");
        else
          System.out.print("      ");
        System.out.println("|" + data[addr] + "|");
      }
      System.out.println("            |--------|");
    }
    System.out.println("            |////////|");
    System.out.println("            |////////|");
    if (ST == SB)
      System.out.println("            |--------|          (stack is empty)");
    else {
      dynamicLink = LB;
      staticLink = LB;
      localRegNum = Machine.LBr;
      System.out.println("      ST--> |////////|");
      System.out.println("            |--------|");
      for (addr = ST - 1; addr >= SB; addr--) {
        System.out.print(addr + ":");
        if (addr == SB)
          System.out.print(" SB-->");
        else if (addr == staticLink) {
          switch (localRegNum) {
            case Machine.LBr:
              System.out.print(" LB-->");
              break;
            case Machine.L1r:
              System.out.print(" L1-->");
              break;
            case Machine.L2r:
              System.out.print(" L2-->");
              break;
            case Machine.L3r:
              System.out.print(" L3-->");
              break;
            case Machine.L4r:
              System.out.print(" L4-->");
              break;
            case Machine.L5r:
              System.out.print(" L5-->");
              break;
            case Machine.L6r:
              System.out.print(" L6-->");
              break;
          }
          staticLink = data[addr];
          localRegNum = localRegNum + 1;
        } else
          System.out.print("      ");
        if ((addr == dynamicLink) && (dynamicLink != SB))
          System.out.print("|SL=" + data[addr] + "|");
        else if ((addr == dynamicLink + 1) && (dynamicLink != SB))
          System.out.print("|DL=" + data[addr] + "|");
        else if ((addr == dynamicLink + 2) && (dynamicLink != SB))
          System.out.print("|RA=" + data[addr] + "|");
        else
          System.out.print("|" + data[addr] + "|");
        System.out.println ("");
        if (addr == dynamicLink) {
          System.out.println("            |--------|");
          dynamicLink = data[addr + 1];
        }
      }
    }
    System.out.println ("");
  }

  static void showStatus () {
    // Writes an indication of whether and why the program has terminated.
    System.out.println ("");
    switch (status) {
      case running:
        System.out.println("Program is running.");
        break;
      case halted:
        System.out.println("Program has halted normally.");
        break;
      case failedDataStoreFull:
        System.out.println("Program has failed due to exhaustion of Data Store.");
        break;
      case failedInvalidCodeAddress:
        System.out.println("Program has failed due to an invalid code address.");
        break;
      case failedInvalidInstruction:
        System.out.println("Program has failed due to an invalid instruction.");
        break;
      case failedOverflow:
        System.out.println("Program has failed due to overflow.");
        break;
      case failedZeroDivide:
        System.out.println("Program has failed due to division by zero.");
        break;
      case failedIOError:
        System.out.println("Program has failed due to an IO error.");
        break;
    }
    if (status != halted)
      dump();
  }


// INTERPRETATION

  static void checkSpace (int spaceNeeded) {
    // Signals failure if there is not enough space to expand the stack or
    // heap by spaceNeeded.

    if (HT - ST < spaceNeeded)
      status = failedDataStoreFull;
  }

  static boolean isTrue (int datum) {
    // Tests whether the given datum represents true.
    return (datum == Machine.trueRep);
  }

  static boolean equal (int size, int addr1, int addr2) {
    // Tests whether two multi-word objects are equal, given their common
    // size and their base addresses.

    boolean eq;
    int index;

    eq = true;
    index = 0;
    while (eq && (index < size))
      if (data[addr1 + index] == data[addr2 + index])
        index = index + 1;
      else
        eq = false;
    return eq;
  }

  static int overflowChecked (long datum) {
    // Signals failure if the datum is too large to fit into a single word,
    // otherwise returns the datum as a single word.

    if ((-Machine.maxintRep <= datum) && (datum <= Machine.maxintRep))
      return (int) datum;
    else {
      status = failedOverflow;
      return 0;
    }
  }

  static int toInt(boolean b) {
    return b ? Machine.trueRep : Machine.falseRep;
  }

  static int currentChar;

  static int readInt() throws java.io.IOException {
    int temp = 0;
    int sign = 1;

    do {
      currentChar = System.in.read();
    } while (Character.isWhitespace((char) currentChar));

    if ((currentChar == '-') || (currentChar == '+'))
      do {
        sign = (currentChar == '-') ? -1 : 1;
        currentChar = System.in.read();
      } while ((currentChar == '-')  || currentChar == '+');

    if (Character.isDigit((char) currentChar))
      do {
        temp = temp * 10 + (currentChar - '0');
        currentChar = System.in.read();
      } while (Character.isDigit((char) currentChar));

    return sign * temp;
  }

  static void callPrimitive (int primitiveDisplacement) {
    // Invokes the given primitive routine.

    int addr, size;
    char ch;

    switch (primitiveDisplacement) {
      case Machine.idDisplacement:
        break; // nothing to be done
      case Machine.notDisplacement:
        data[ST - 1] = toInt(!isTrue(data[ST - 1]));
        break;
      case Machine.andDisplacement:
        ST = ST - 1;
        data[ST - 1] = toInt(isTrue(data[ST - 1]) & isTrue(data[ST]));
        break;
      case Machine.orDisplacement:
        ST = ST - 1;
        data[ST - 1] = toInt(isTrue(data[ST - 1]) | isTrue(data[ST]));
        break;
      case Machine.succDisplacement:
        data[ST - 1] = overflowChecked(data[ST - 1] + 1);
        break;
      case Machine.predDisplacement:
        data[ST - 1] = overflowChecked(data[ST - 1] - 1);
        break;
      case Machine.negDisplacement:
        data[ST - 1] = -data[ST - 1];
        break;
      case Machine.addDisplacement:
        ST = ST - 1;
        accumulator = data[ST - 1];
        data[ST - 1] = overflowChecked(accumulator + data[ST]);
        break;
      case Machine.subDisplacement:
        ST = ST - 1;
        accumulator = data[ST - 1];
        data[ST - 1] = overflowChecked(accumulator - data[ST]);
        break;
      case Machine.multDisplacement:
        ST = ST - 1;
        accumulator = data[ST - 1];
        data[ST - 1] = overflowChecked(accumulator * data[ST]);
        break;
      case Machine.divDisplacement:
        ST = ST - 1;
        accumulator = data[ST - 1];
        if (data[ST] != 0)
          data[ST - 1] = (int) (accumulator / data[ST]);
        else
          status = failedZeroDivide;
        break;
      case Machine.modDisplacement:
        ST = ST - 1;
        accumulator = data[ST - 1];
        if (data[ST] != 0)
          data[ST - 1] = (int) (accumulator % data[ST]);
        else
          status = failedZeroDivide;
        break;
      case Machine.ltDisplacement:
        ST = ST - 1;
        data[ST - 1] = toInt(data[ST - 1] < data[ST]);
        break;
      case Machine.leDisplacement:
        ST = ST - 1;
        data[ST - 1] = toInt(data[ST - 1] <= data[ST]);
        break;
      case Machine.geDisplacement:
        ST = ST - 1;
        data[ST - 1] = toInt(data[ST - 1] >= data[ST]);
        break;
      case Machine.gtDisplacement:
        ST = ST - 1;
        data[ST - 1] = toInt(data[ST - 1] > data[ST]);
        break;

      case Machine.neDisplacement:
        size = data[ST - 1]; // size of each comparand
        ST = ST - 2 * size;
        data[ST - 1] = toInt(! equal(size, ST - 1, ST - 1 + size));
        break;
      case Machine.eolDisplacement:
        data[ST] = toInt(currentChar == '\n');
        ST = ST + 1;
        break;
      case Machine.eofDisplacement:
        data[ST] = toInt(currentChar == -1);
        ST = ST + 1;
        break;
      case Machine.getDisplacement:
        ST = ST - 1;
        addr = data[ST];
        try {
          currentChar = System.in.read();
        } catch (java.io.IOException s) {
          status = failedIOError;
        }
        data[addr] = (int) currentChar;
        break;
      case Machine.putDisplacement:
        ST = ST - 1;
        ch = (char) data[ST];
        System.out.print(ch);
        break;
      case Machine.geteolDisplacement:
        try {
          while ((currentChar = System.in.read()) != '\n');
        } catch (java.io.IOException s) {
          status = failedIOError;
        }
        break;
      case Machine.puteolDisplacement:
        System.out.println ("");
        break;
      case Machine.getintDisplacement:
        ST = ST - 1;
        addr = data[ST];
        try {
          accumulator = readInt();
        } catch (java.io.IOException s) {
          status = failedIOError;
        }
        data[addr] = (int) accumulator;
        break;
      case Machine.putintDisplacement:
        ST = ST - 1;
        accumulator = data[ST];
        System.out.print(accumulator);
        break;
      case Machine.newDisplacement:
        size = data[ST - 1];
        checkSpace(size);
        HT = HT - size;
        data[ST - 1] = HT;
        break;
      case Machine.eqDisplacement:
        size = data[ST - 1]; 
        ST = ST - 2 * size;
        data[ST - 1] = toInt(equal(size, ST - 1, ST - 1 + size));
        break;
      case Machine.disposeDisplacement:
        ST = ST - 1; // no action taken at present
        break;
    }
  }
  
  public static void resetData() {
        data = new int[datasize];  
    }

  public static void interpretProgram() {
    // Runs the program in code store.
    System.out.println(">>> Iniciando interpretProgram");
    System.out.println("CP=" + CP + ", ST=" + ST + ", SB=" + SB + ", HT=" + HT);
    Instruction currentInstr;
    int op, r, n, d, addr, index;
    

    // Initialize registers ...
    ST = SB;
    HT = HB;
    LB = SB;
    CP = CB;
    status = running;
    //System.out.println("ANTES DEL DO: Ejecutando instrucción CP=" + CP + " ST=" + ST);
    do {
       //System.out.println("Entró al DO");
      // Fetch instruction ...
      currentInstr = Machine.code[CP];
      // Decode instruction ...
      op = currentInstr.op;
      r = currentInstr.r;
      n = currentInstr.n;
      d = currentInstr.d;
      // Execute instruction ...
      //System.out.println("Antes del switch: CP=" + CP + " ST=" + ST);
      switch (op) {
        case Machine.LOADop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          addr = d + content(r);
          checkSpace(n);
          for (index = 0; index < n; index++)
            data[ST + index] = data[addr + index];
          ST = ST + n;
          CP = CP + 1;
          break;
        case Machine.LOADAop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          addr = d + content(r);
          checkSpace(1);
          data[ST] = addr;
          ST = ST + 1;
          CP = CP + 1;
          break;
        case Machine.LOADIop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = ST - 1;
          addr = data[ST];
          checkSpace(n);
          for (index = 0; index < n; index++)
            data[ST + index] = data[addr + index];
          ST = ST + n;
          CP = CP + 1;
          break;
        case Machine.LOADLop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          checkSpace(1);
          data[ST] = d;
          ST = ST + 1;
          CP = CP + 1;
          break;
        case Machine.STOREop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          addr = d + content(r);
          ST = ST - n;
          for (index = 0; index < n; index++)
            data[addr + index] = data[ST + index];
          CP = CP + 1;
          break;
        case Machine.STOREIop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = ST - 1;
          addr = data[ST];
          ST = ST - n;
          for (index = 0; index < n; index++)
            data[addr + index] = data[ST + index];
          CP = CP + 1;
          break;
        case Machine.CALLop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          addr = d + content(r);
          if (addr >= Machine.PB) {
            callPrimitive(addr - Machine.PB);
            CP = CP + 1;
          } else {
            checkSpace(3);
            if ((0 <= n) && (n <= 15))
              data[ST] = content(n); // static link
            else
              status = failedInvalidInstruction;
            data[ST + 1] = LB; // dynamic link
            data[ST + 2] = CP + 1; // return address
            LB = ST;
            ST = ST + 3;
            CP = addr;
          }
          break;
        case Machine.CALLIop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = ST - 2;
          addr = data[ST + 1];
          if (addr >= Machine.PB) {
            callPrimitive(addr - Machine.PB);
            CP = CP + 1;
          } else {
            // data[ST] = static link already
            data[ST + 1] = LB; // dynamic link
            data[ST + 2] = CP + 1; // return address
            LB = ST;
            ST = ST + 3;
            CP = addr;
          }
          break;
        case Machine.RETURNop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          addr = LB - d;
          CP = data[LB + 2];
          LB = data[LB + 1];
          ST = ST - n;
          for (index = 0; index < n; index++)
            data[addr + index] = data[ST + index];
          ST = addr + n;
          break;
        case Machine.PUSHop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          checkSpace(d);
          ST = ST + d;
          CP = CP + 1;
          break;
        case Machine.POPop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          addr = ST - n - d;
          ST = ST - n;
          for (index = 0; index < n; index++)
            data[addr + index] = data[ST + index];
          ST = addr + n;
          CP = CP + 1;
          break;
        case Machine.JUMPop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          CP = d + content(r);
          break;
        case Machine.JUMPIop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = ST - 1;
          CP = data[ST];
          break;
        case Machine.JUMPIFop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = ST - 1;
          if (data[ST] == n)
            CP = d + content(r);
          else
            CP = CP + 1;
          break;
        case Machine.JUMPINDop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = ST - 1;
          CP = data[ST]; 
          break;
        case Machine.HALTop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          status = halted;
          break;
        case Machine.SETSTop:
          System.out.println("Ejecutando instrucción: op=" + op + ", CP=" + CP + ", ST=" + ST);
          ST = data[ST - 1]; 
          CP = CP + 1;
          break;
      }
      if ((CP < CB) || (CP >= CT)){
          System.out.println("Entró al IF de invalid code address: CP=" + CP + " CT=" + CT + " CB=" + CB);
          status = failedInvalidCodeAddress;
      }
        
    } while (status == running);
    System.out.println("Saliendo de interpretProgram(), status = " + status);
  }


// LOADING

 public static void loadObjectProgram(String objectName) {
    FileInputStream objectFile = null;
    DataInputStream objectStream = null;

    try {
        objectFile = new FileInputStream(objectName);
        objectStream = new DataInputStream(objectFile);

        ObjectFileHeader header = new ObjectFileHeader(objectStream);

        for (int addr = Machine.CB; addr < Machine.CB + header.instructionCount; addr++) {
            Machine.code[addr] = Instruction.read(objectStream);
        }

        CT = Machine.CB + header.instructionCount;

        SB = CT;   
        ST = SB;
        LB = SB;
        HT = Machine.PB;

        objectFile.close();
    } catch (FileNotFoundException s) {
        CT = CB;
        System.err.println("Error opening object file: " + s);
    } catch (IOException s) {
        CT = CB;
        System.err.println("Error reading object file: " + s);
    }
}


// RUNNING
 

public static void main(String[] args) {
    System.out.println("********** TAM Interpreter (Java Version 2.1) HOLA MUNDO! **********");

    if (args.length == 1)
        objectName = args[0];
    else
        objectName = "obj.tam";

    // Cargar el programa TAM
    loadObjectProgram(objectName);

    // Mostrar el código TAM desensamblado
    for (int i = CB; i < CT; i++) {
        System.out.println(i + ": " + Machine.code[i].toString());
    }

    // Ejecutar el programa normalmente (este sí ejecuta putint, throw, etc)
    if (CT != CB) {
        interpretProgram();
        System.out.println("Llamando a showStatus()");
        showStatus();
    }

    // Mostrar el DebuggerWindow con el paso a paso visual
    Instruction[] code = Machine.code;
    DebuggerWindow debugger = new DebuggerWindow();

    PC = 0; // Reiniciamos PC solo para visualización
    while (PC < CT) {

        // Construimos el código visual para el debugger
        String[] codeLines = new String[CT];
        for (int i = 0; i < CT; i++) {
            Instruction instr = code[i];
            String asm = instructionToString(instr);
            String raw = "Memory: " + instr.op + " " + instr.r + " " + instr.n + " " + instr.d;

            if (i == PC)
                codeLines[i] = ">> " + asm + "\n   " + raw;
            else
                codeLines[i] = "   " + asm + "\n   " + raw;
        }

        // Actualizamos el debugger visual (sin ejecutar TAM nuevamente)
        debugger.updateDebugger(codeLines, PC, CB, SB, ST, LB, HT, status);

        // Pausa para visualizar paso a paso
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PC++; // Solo avanzamos la visualización
    }
}
    private static String instructionToString(Instruction instr) {
        String mnemonic;
        switch (instr.op) {
            case 0: mnemonic = "LOAD"; break;
            case 1: mnemonic = "LOADA"; break;
            case 2: mnemonic = "LOADI"; break;
            case 3: mnemonic = "LOADL"; break;
            case 4: mnemonic = "STORE"; break;
            case 5: mnemonic = "STOREI"; break;
            case 6: mnemonic = "CALL"; break;
            case 7: mnemonic = "CALLI"; break;
            case 8: mnemonic = "RETURN"; break;
            case 9: mnemonic = "PUSH"; break;
            case 10: mnemonic = "POP"; break;
            case 11: mnemonic = "JUMP"; break;
            case 12: mnemonic = "JUMPI"; break;
            case 13: mnemonic = "JUMPIF"; break;
            case 14: mnemonic = "JUMPIND"; break;
            case 15: mnemonic = "HALT"; break;
            case 16: mnemonic = "SETST"; break;
            default: mnemonic = "UNKNOWN"; break;
        }
        return mnemonic + " " + instr.r + " " + instr.n + " " + instr.d;
    }

    /*public static void runWithDebugger(DebuggerListener debuggerListener) {
        System.out.println("********** TAM Interpreter con Debugger **********");

        code = Machine.code; 
        PC = 0;
        CB = 0;
        SB = 0;
        ST = 0;
        LB = 0;
        CT = code.length;

        for (int i = 0; i < CT; i++) {
            System.out.println(i + ": " + code[i].toString());
        }

        while (PC < code.length) {
            Instruction instr = code[PC];



            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            PC++;
        }

        System.out.println(">>> Ejecución terminada.");
    }
}*/


    // Método simulado para cargar código
    private static Instruction[] loadSampleCode() {
        Instruction[] sample = new Instruction[5];
        for (int i = 0; i < 5; i++) {
            sample[i] = new Instruction(); // Reemplazar con instancias válidas
        }
        return sample;
    }

    // Método simulado para ejecutar una instrucción
    private static void executeInstruction(Instruction currentInstr) {
        int op = currentInstr.op;
        int r = currentInstr.r;
        int n = currentInstr.n;
        int d = currentInstr.d;
        int addr, index;

        switch (op) {
            case Machine.LOADop:
                addr = d + content(r);
                checkSpace(n);
                for (index = 0; index < n; index++)
                    data[ST + index] = data[addr + index];
                ST = ST + n;
                CP = CP + 1;
                break;
            case Machine.LOADAop:
                addr = d + content(r);
                checkSpace(1);
                data[ST] = addr;
                ST = ST + 1;
                CP = CP + 1;
                break;
            case Machine.LOADIop:
                ST = ST - 1;
                addr = data[ST];
                checkSpace(n);
                for (index = 0; index < n; index++)
                    data[ST + index] = data[addr + index];
                ST = ST + n;
                CP = CP + 1;
                break;
            case Machine.LOADLop:
                checkSpace(1);
                data[ST] = d;
                ST = ST + 1;
                CP = CP + 1;
                break;
            case Machine.STOREop:
                addr = d + content(r);
                ST = ST - n;
                for (index = 0; index < n; index++)
                    data[addr + index] = data[ST + index];
                CP = CP + 1;
                break;
            case Machine.STOREIop:
                ST = ST - 1;
                addr = data[ST];
                ST = ST - n;
                for (index = 0; index < n; index++)
                    data[addr + index] = data[ST + index];
                CP = CP + 1;
                break;
            case Machine.CALLop:
                addr = d + content(r);
                if (addr >= Machine.PB) {
                    callPrimitive(addr - Machine.PB);
                    CP = CP + 1;
                } else {
                    checkSpace(3);
                    if ((0 <= n) && (n <= 15))
                        data[ST] = content(n); // static link
                    else
                        status = failedInvalidInstruction;
                    data[ST + 1] = LB; // dynamic link
                    data[ST + 2] = CP + 1; // return address
                    LB = ST;
                    ST = ST + 3;
                    CP = addr;
                }
                break;
            case Machine.CALLIop:
                ST = ST - 2;
                addr = data[ST + 1];
                if (addr >= Machine.PB) {
                    callPrimitive(addr - Machine.PB);
                    CP = CP + 1;
                } else {
                    data[ST + 1] = LB; // dynamic link
                    data[ST + 2] = CP + 1; // return address
                    LB = ST;
                    ST = ST + 3;
                    CP = addr;
                }
                break;
            case Machine.RETURNop:
                addr = LB - d;
                CP = data[LB + 2];
                LB = data[LB + 1];
                ST = ST - n;
                for (index = 0; index < n; index++)
                    data[addr + index] = data[ST + index];
                ST = addr + n;
                break;
            case Machine.PUSHop:
                checkSpace(d);
                ST = ST + d;
                CP = CP + 1;
                break;
            case Machine.POPop:
                addr = ST - n - d;
                ST = ST - n;
                for (index = 0; index < n; index++)
                    data[addr + index] = data[ST + index];
                ST = addr + n;
                CP = CP + 1;
                break;
            case Machine.JUMPop:
                CP = d + content(r);
                break;
            case Machine.JUMPIop:
                ST = ST - 1;
                CP = data[ST];
                break;
            case Machine.JUMPIFop:
                ST = ST - 1;
                if (data[ST] == n)
                    CP = d + content(r);
                else
                    CP = CP + 1;
                break;
            case Machine.JUMPINDop:
                ST = ST - 1;
                CP = data[ST];
                break;
            case Machine.HALTop:
                status = halted;
                break;
            case Machine.SETSTop:
                ST = data[ST - 1];
                CP = CP + 1;
                break;
        }

        if ((CP < CB) || (CP >= CT)) {
            status = failedInvalidCodeAddress;
        }
    }
    
    public static void runWithDebugger(DebuggerListener debuggerListener) {
        System.out.println("********** TAM Interpreter con Debugger **********");

        Instruction[] code = Machine.code;

        PC = 0; 
        CB = 0; 
        SB = 0; 
        ST = 0; 
        LB = 0;
        CT = code.length;
        status = running;

        while (PC < code.length && status == running) {
            Instruction instr = code[PC];

            executeInstruction(instr);

            debuggerListener.update(PC, SB, LB, ST, 0);

            try {
                Thread.sleep(200); // Pequeña pausa visual
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(">>> Debugger ha terminado.");
    }
}




class DebuggerWindow extends JFrame {
    private JTextArea codeArea;
    private JTextArea registersArea;

    public DebuggerWindow() {
        setTitle("TAM Debugger");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        codeArea = new JTextArea();
        registersArea = new JTextArea();

        codeArea.setEditable(false);
        registersArea.setEditable(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(codeArea), new JScrollPane(registersArea));
        splitPane.setDividerLocation(400);

        add(splitPane);
        setVisible(true);
    }

    public void updateDebugger(String[] codeLines, int pc, int cb, int sb, int st, int lb, int ht, int status) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLines.length; i++) {
            if (i == pc) {
                code.append(">> ").append(codeLines[i]).append("\n");
            } else {
                code.append("   ").append(codeLines[i]).append("\n");
            }
        }
        codeArea.setText(code.toString());

        StringBuilder regs = new StringBuilder();
        regs.append("PC: ").append(pc).append("\n");
        regs.append("CB: ").append(cb).append("\n");
        regs.append("SB: ").append(sb).append("\n");
        regs.append("ST: ").append(st).append("\n");
        regs.append("LB: ").append(lb).append("\n");
        regs.append("HT: ").append(ht).append("\n");

        registersArea.setText(regs.toString());
    }
    
    private String statusToString(int status) {
    switch (status) {
        case 0: return "Running";
        case 1: return "Halted";
        case 2: return "DataStoreFull";
        case 3: return "InvalidCodeAddress";
        case 4: return "InvalidInstruction";
        case 5: return "Overflow";
        case 6: return "DivideByZero";
        case 7: return "IOError";
        default: return "Unknown(" + status + ")";
    }
}
    
    
}
