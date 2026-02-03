package madlang.test;

import java.util.Arrays;

import madlang.PrettyPrinter;
import madlang.ast.Ast;
import madlang.ast.Expr;
import madlang.ast.Stmt;

public final class TestPrograms {
  private TestPrograms() {}

  public static void runAll() {
    System.out.println("== test1 ==");
    System.out.print(PrettyPrinter.pretty(test1()));

    System.out.println("== test2 ==");
    System.out.print(PrettyPrinter.pretty(test2()));

    System.out.println("== test_netid ==");
    System.out.print(PrettyPrinter.pretty(test3()));
  }

  /**
   * x: int = 123;
   *
   * fn f(a: int, b: int): int {
   *   return -a + a * b - b;
   * }
   */
  public static Ast.Program test1() {
    Ast.GlobalVarDecl xDecl = new Ast.GlobalVarDecl(
      "x",
      Ast.Type.INT,
      new Expr.IntLit(123)
    );
    Ast.Param aParam = new Ast.Param("a", Ast.Type.INT);
    Ast.Param bParam = new Ast.Param("b", Ast.Type.INT);
    Expr expr =
      new Expr.Binary(
        new Expr.Binary(
          new Expr.Unary(
            Expr.UnOp.NEG,
            new Expr.Var("a")
          ),
          Expr.BinOp.ADD,
          new Expr.Binary(
            new Expr.Var("a"),
            Expr.BinOp.MUL,
            new Expr.Var("b")
          )
        ),
        Expr.BinOp.SUB,
        new Expr.Var("b")
      );
    Stmt.Block fBody = new Stmt.Block(
      Arrays.asList(
        new Stmt.Return(expr)
      )
    );
    Ast.FunDecl fDecl = new Ast.FunDecl(
      "f",
      Arrays.asList(aParam, bParam),
      Ast.Type.INT,
      fBody
    );
    return new Ast.Program(
      Arrays.<Ast.Decl>asList(xDecl, fDecl)
    );
  }

  /**
   * TODO: hard-code the AST for the example in README.md.
   */
  public static Ast.Program test2() {
    throw new UnsupportedOperationException(
      "TODO: test2: hard-code the AST for the example in README.md"
    );
  }

/**
 * TODO: Choose a program and hard-code its AST as a test case.
 *
 * Requirements:
 * 1. The constructed AST must represent a valid MadLang program.
 * 2. The program must contain your own NetID as an identifier
 *    (e.g., variable name, function name, or parameter name).
 * 3. Both the source program and its pretty-printed output should
 *    contain no more than 20 lines of code.
 */
public static Ast.Program test3() {
  throw new UnsupportedOperationException(
    "TODO: test3: hard-code an AST that includes your NetID as an identifier"
  );
}

}
