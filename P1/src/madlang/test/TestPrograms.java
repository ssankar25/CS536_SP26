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

    /// INNER FUNCTION /// 

    Ast.Param yParam = new Ast.Param("y", Ast.Type.INT);

    // r = r + 10
    Expr.Binary innerAssignRExpr = new Expr.Binary(new Expr.Var("r"), Expr.BinOp.ADD, new Expr.IntLit(10));
    Stmt.Assign innerAssignR = new Stmt.Assign("r", innerAssignRExpr);

    // return y * y;
    Expr.Binary innerReturnExpr = new Expr.Binary(new Expr.Var("y"), Expr.BinOp.MUL, new Expr.Var("y"));
    Stmt.Return innerReturn = new Stmt.Return(innerReturnExpr);

    // { r = r + 10; return y * y; }
    Stmt.Block innerBody = new Stmt.Block(Arrays.asList(innerAssignR, innerReturn));

    // fn Inner(y: int): int { r = r + 10; return y * y; }
    Ast.FunDecl innerDecl = new Ast.FunDecl("Inner", Arrays.asList(yParam), Ast.Type.INT, innerBody);

    // Need function as statement so it can go inside the Outer function
    Stmt.FunDef innerDef = new Stmt.FunDef(innerDecl);

    /// WHILE LOOP /// 

    // (i < x)
    Expr.Binary whileCondExpr = new Expr.Binary(new Expr.Var("i"), Expr.BinOp.LT, new Expr.Var("x"));

    // i % 2
    Expr.Binary ifCondExprMod = new Expr.Binary(new Expr.Var("i"), Expr.BinOp.MOD, new Expr.IntLit(2));
    // (i % 2 == 0)
    Expr.Binary ifCondExpr = new Expr.Binary(ifCondExprMod, Expr.BinOp.EQ, new Expr.IntLit(0));

    // Inner(i)
    Expr.Call callInner = new Expr.Call("Inner", Arrays.asList(new Expr.Var("i")));
    // r + Inner(i)
    Expr.Binary rPlusInner = new Expr.Binary(new Expr.Var("r"), Expr.BinOp.ADD, callInner);
    // r = r + Inner(i)
    Stmt.Assign thenAssignR = new Stmt.Assign("r", rPlusInner);
    Stmt.Block thenBlock = new Stmt.Block(Arrays.asList(thenAssignR));

    // if (i % 2 == 0) { 
    //     r = r + Inner(i); 
    // } 
    Stmt.If ifStmt = new Stmt.If(ifCondExpr, thenBlock, null);

    // i = i + 1
    Expr.Binary iPlusOne = new Expr.Binary(new Expr.Var("i"), Expr.BinOp.ADD, new Expr.IntLit(1));
    Stmt.Assign incI = new Stmt.Assign("i", iPlusOne);

    // while (i < x) { 
    //     if (i % 2 == 0) { 
    //         r = r + Inner(i); 
    //     } 
    //     i = i + 1; 
    // }
    Stmt.Block whileBody = new Stmt.Block(Arrays.asList(ifStmt, incI));
    Stmt.While whileStmt = new Stmt.While(whileCondExpr, whileBody);

    /// OUTER FUNCTION ///

    Ast.Param xParam = new Ast.Param("x", Ast.Type.INT);

    // Function-level variables and return statement
    Stmt.VarDef outerDefR = new Stmt.VarDef("r", Ast.Type.INT, new Expr.IntLit(0));
    Stmt.VarDef outerDefI = new Stmt.VarDef("i", Ast.Type.INT, new Expr.IntLit(0));
    Stmt.Return outerReturn = new Stmt.Return(new Expr.Var("r"));

    // Outer function declaration
    Stmt.Block outerBody = new Stmt.Block(Arrays.asList(outerDefR, outerDefI, innerDef, whileStmt, outerReturn));
    Ast.FunDecl outerDecl = new Ast.FunDecl("Outer", Arrays.asList(xParam), Ast.Type.INT, outerBody);


    /// MAIN FUNCTION /// 

    // r: int = Outer(5)
    Expr.Call callOuter = new Expr.Call("Outer", Arrays.asList(new Expr.IntLit(5)));
    Stmt.VarDef mainDefR = new Stmt.VarDef("r", Ast.Type.INT, callOuter);

    // output(r)
    Stmt.ExprStmt outputR = new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.Var("r"))));

    // return 0
    Stmt.Return mainReturn = new Stmt.Return(new Expr.IntLit(0));
    
    Stmt.Block mainBody = new Stmt.Block(Arrays.asList(mainDefR, outputR, mainReturn));
    Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.asList(), Ast.Type.INT, mainBody);
    
    return new Ast.Program(Arrays.<Ast.Decl>asList(outerDecl, mainDecl));
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
 * 
 * fn fact(x: int) int {
 *   if (x < 0)
 *     return -1;
 *   if (x == 0) {
 *     return 1;
 *   }
 *   else {
 *     {
 *       return x * fact(x - 1);
 *     }
 *   }
 * }
 * 
 * fn main(): int {
 *   sankar5: int = fact(123);
 *   output(sankar5);
 *   return 0;
 * }
 * 
 * TODO: Need to test multiple parameters, uninitialized global variable declaration, standalone block, multiple params in Expr.Call (function call w multiple params)
 */
public static Ast.Program test3() {

  Ast.Param xParam = new Ast.Param("x", Ast.Type.INT);

  // if (x < 0) return -1;
  Stmt.Return negOneReturn = new Stmt.Return(new Expr.Unary(Expr.UnOp.NEG, new Expr.IntLit(1)));
  Expr.Binary firstIfCond = new Expr.Binary(new Expr.Var("x"), Expr.BinOp.LT, new Expr.IntLit(0));
  Stmt.If firstIf = new Stmt.If(firstIfCond, negOneReturn, null);

  // return x * fact(x - 1); 
  Expr.Binary xMinusOne = new Expr.Binary(new Expr.Var("x"), Expr.BinOp.SUB, new Expr.IntLit(1));
  Expr.Call callFactRecur = new Expr.Call("fact", Arrays.asList(xMinusOne));
  Expr.Binary recurCall = new Expr.Binary(new Expr.Var("x"), Expr.BinOp.MUL, callFactRecur);
  Stmt.Return recurReturn = new Stmt.Return(recurCall);

  // else {...}
  Stmt.Block elseReturnBlock = new Stmt.Block(Arrays.asList(new Stmt.Block(Arrays.asList(recurReturn)))); // Multiple levels of blocks

  // if (x == 0) { return 1 } else {{...}}
  Expr.Binary secondIfCond = new Expr.Binary(new Expr.Var("x"), Expr.BinOp.EQ, new Expr.IntLit(0));
  Stmt.Return oneReturn = new Stmt.Return(new Expr.IntLit(1));
  Stmt.Block secondThenBlock = new Stmt.Block(Arrays.asList(oneReturn));
  Stmt.If secondIf = new Stmt.If(secondIfCond, secondThenBlock, elseReturnBlock);

  Stmt.Block factBody = new Stmt.Block(Arrays.asList(firstIf, secondIf));
  Ast.FunDecl factDecl = new Ast.FunDecl("fact", Arrays.asList(xParam), Ast.Type.INT, factBody);

  /// MAIN FUNCTION ///

  // sankar5: int = fact(123);
  Expr.Call callFactMain = new Expr.Call("fact", Arrays.asList(new Expr.IntLit(123)));
  Stmt.VarDef mainDefNetID = new Stmt.VarDef("sankar5", Ast.Type.INT, callFactMain);

  // output(sankar5)
  Stmt.ExprStmt outputNetID = new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.Var("sankar5"))));

  // return 0
  Stmt.Return mainReturn = new Stmt.Return(new Expr.IntLit(0));
  
  Stmt.Block mainBody = new Stmt.Block(Arrays.asList(mainDefNetID, outputNetID, mainReturn));
  Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.asList(), Ast.Type.INT, mainBody);
  
  return new Ast.Program(Arrays.<Ast.Decl>asList(factDecl, mainDecl));

}

}
