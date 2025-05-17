package Triangle.SyntacticAnalyzer;

final class Token extends Object {

  protected int kind;
  protected String spelling;
  protected SourcePosition position;

  public Token(int kind, String spelling, SourcePosition position) {

    if (kind == Token.IDENTIFIER) {
      int currentKind = firstReservedWord;
      boolean searching = true;

      while (searching) {
        int comparison = tokenTable[currentKind].compareTo(spelling);
        if (comparison == 0) {
          this.kind = currentKind;
          searching = false;
        } else if (comparison > 0 || currentKind == lastReservedWord) {
          this.kind = Token.IDENTIFIER;
          searching = false;
        } else {
          currentKind++;
        }
      }
    } else
      this.kind = kind;

    this.spelling = spelling;
    this.position = position;
  }

  public static String spell(int kind) {
    return tokenTable[kind];
  }

  public String toString() {
    return "Kind=" + kind + ", spelling=" + spelling +
           ", position=" + position;
  }

  // Token classes...

  public static final int

    // literals, identifiers, operators...
    INTLITERAL   = 0,
    CHARLITERAL  = 1,
    IDENTIFIER   = 2,
    OPERATOR     = 3,

    // reserved words - must be in alphabetical order...
    ARRAY        = 4,
    BEGIN        = 5,
    CATCH        = 6,
    CONST        = 7,
    DO           = 8,
    DOWNTO       = 9,
    ELSE         = 10,
    END          = 11,
    FOR          = 12,
    FUNC         = 13,
    IF           = 14,
    IN           = 15,
    LET          = 16,
    OF           = 17,
    PROC         = 18,
    RECORD       = 19,
    THEN         = 20,
    THROW        = 21,
    TO           = 22,
    TRY          = 23,
    TYPE         = 24,
    VAR          = 25,
    WHILE        = 26,

    // punctuation...
    DOT          = 27,
    COLON        = 28,
    SEMICOLON    = 29,
    COMMA        = 30,
    BECOMES      = 31,
    IS           = 32,

    // brackets...
    LPAREN       = 33,
    RPAREN       = 34,
    LBRACKET     = 35,
    RBRACKET     = 36,
    LCURLY       = 37,
    RCURLY       = 38,

    // special tokens...
    EOT          = 39,
    ERROR        = 40;

  private static String[] tokenTable = new String[] {
    "<int>",       // 0
    "<char>",      // 1
    "<identifier>",// 2
    "<operator>",  // 3
    "array",       // 4
    "begin",       // 5
    "catch",       // 6
    "const",       // 7
    "do",          // 8
    "downto",      // 9
    "else",        // 10
    "end",         // 11
    "for",         // 12
    "func",        // 13
    "if",          // 14
    "in",          // 15
    "let",         // 16
    "of",          // 17
    "proc",        // 18
    "record",      // 19
    "then",        // 20
    "throw",       // 21
    "to",          // 22
    "try",         // 23
    "type",        // 24
    "var",         // 25
    "while",       // 26
    ".",           // 27
    ":",           // 28
    ";",           // 29
    ",",           // 30
    ":=",          // 31
    "~",           // 32
    "(",           // 33
    ")",           // 34
    "[",           // 35
    "]",           // 36
    "{",           // 37
    "}",           // 38
    "",            // 39 (EOT)
    "<error>"      // 40
  };

  private final static int firstReservedWord = Token.ARRAY,
                         lastReservedWord  = Token.WHILE;

}
