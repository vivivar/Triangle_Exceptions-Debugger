package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public final class TryCommand extends Command {
  public Command tryPart;
  public Identifier identifier;
  public TypeDenoter type; // <-- nuevo campo
  public Command catchPart;

  public TryCommand(Command tryPart, Identifier identifier, TypeDenoter type, Command catchPart, SourcePosition pos) {
    super(pos);
    this.tryPart = tryPart;
    this.identifier = identifier;
    this.type = type;
    this.catchPart = catchPart;
  }

  public Object visit(Visitor v, Object o) {
    return v.visitTryCommand(this, o);
  }
}
