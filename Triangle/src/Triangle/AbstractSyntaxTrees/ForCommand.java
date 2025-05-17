package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class ForCommand extends Command {
    
  public Vname I;
  public Expression E1, E2;
  public Command C;
  public boolean IsDescending;

    public ForCommand (Vname i, Expression e1, Expression e2, Command cAST,boolean isDescending, SourcePosition thePosition) {
        super (thePosition);
        I = i;
        E1 = e1;
        E2 = e2;
        C = cAST;
        IsDescending = isDescending;
    }

    public Object visit (Visitor v, Object o) {
        return v.visitForCommand(this, o);
    }

}