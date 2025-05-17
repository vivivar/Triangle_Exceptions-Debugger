/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Triangle.AbstractSyntaxTrees;
import Triangle.SyntacticAnalyzer.SourcePosition;
/**
 *
 * @author henge
 */
public class ThrowCommand extends Command {
  public Expression expression;

  public ThrowCommand(Expression expr, SourcePosition pos) {
    super(pos);
    this.expression = expr;
  }

  public Object visit(Visitor v, Object o) {
    return v.visitThrowCommand(this, o);
  }
}
