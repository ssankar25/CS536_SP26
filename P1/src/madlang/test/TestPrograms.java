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

    System.out.println("== test4 ==");
    System.out.print(PrettyPrinter.pretty(test4()));

    System.out.println("== test5 ==");
    System.out.print(PrettyPrinter.pretty(test5()));

    System.out.println("== test6 ==");
    System.out.print(PrettyPrinter.pretty(test6()));

    System.out.println("== test7 ==");
    System.out.print(PrettyPrinter.pretty(test7()));

    System.out.println("== test8 ==");
    System.out.print(PrettyPrinter.pretty(test8()));
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
   * Hard-coded AST for the example in README.md.
   * 
   * Example:
   * 
   * fn Outer(x: int): int {
   *   r: int = 0;
   *   i: int = 0;
   * 
   *   fn Inner(y: int): int {
   *     r = r + 10;
   *     return y * y;
   *   }
   * 
   *   while (i < x) {
   *     if (i % 2 == 0) {
   *       r = r + Inner(i);
   *     }
   *     i = i + 1;
   *   }
   *   return r;
   * }
   * 
   * fn main(): int {
   *   r: int = Outer(5);
   *   output(r);
   *   return 0;
   * }
   * 
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
   * My hard-coded AST for a program that calculates the factorial.
   * 
   * This includes some corner cases for how bodies of if/else statements can
   * be formatted, which include as not a block at all, as a block, or within 
   * a standalone block.
   * 
   * The requirements below are met.
   *
   * Requirements:
   * 1. The constructed AST must represent a valid MadLang program.
   * 2. The program must contain your own NetID as an identifier
   *    (e.g., variable name, function name, or parameter name).
   * 3. Both the source program and its pretty-printed output should
   *    contain no more than 20 lines of code.
   * 
   * Program:
   * 
   * fn fact(x: int): int {
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

  /**
   * (Corner cases: uninitialized global, bool global, multi-arg call)
   *
   * g: int;
   * flag: bool = true;
   *
   * fn add3(a: int, b: int, c: int): int {
   *   return a + b + c;
   * }
   *
   * fn main(): int {
   *   x: int = add3(1, 2, g);
   *   if (flag) {
   *     output(x);
   *   }
   *   else {
   *     output(0);
   *   }
   *   return 0;
   * }
   */
  public static Ast.Program test4() {
    Ast.GlobalVarDecl gDecl = new Ast.GlobalVarDecl("g", Ast.Type.INT, null);
    Ast.GlobalVarDecl flagDecl = new Ast.GlobalVarDecl("flag", Ast.Type.BOOL, new Expr.BoolLit(true));

    Ast.Param aParam = new Ast.Param("a", Ast.Type.INT);
    Ast.Param bParam = new Ast.Param("b", Ast.Type.INT);
    Ast.Param cParam = new Ast.Param("c", Ast.Type.INT);

    Expr add3Expr =
      new Expr.Binary(
        new Expr.Binary(new Expr.Var("a"), Expr.BinOp.ADD, new Expr.Var("b")),
        Expr.BinOp.ADD,
        new Expr.Var("c")
      );
    Stmt.Block add3Body = new Stmt.Block(Arrays.asList(new Stmt.Return(add3Expr)));
    Ast.FunDecl add3Decl = new Ast.FunDecl("add3", Arrays.asList(aParam, bParam, cParam), Ast.Type.INT, add3Body);

    Expr.Call callAdd3 = new Expr.Call("add3", Arrays.asList(new Expr.IntLit(1), new Expr.IntLit(2), new Expr.Var("g")));
    Stmt.VarDef xDef = new Stmt.VarDef("x", Ast.Type.INT, callAdd3);

    Stmt.Block thenBlk = new Stmt.Block(Arrays.asList(
      new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.Var("x"))))
    ));
    Stmt.Block elseBlk = new Stmt.Block(Arrays.asList(
      new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.IntLit(0))))
    ));
    Stmt.If ifStmt = new Stmt.If(new Expr.Var("flag"), thenBlk, elseBlk);

    Stmt.Return ret0 = new Stmt.Return(new Expr.IntLit(0));
    Stmt.Block mainBody = new Stmt.Block(Arrays.asList(xDef, ifStmt, ret0));
    Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.<Ast.Param>asList(), Ast.Type.INT, mainBody);

    return new Ast.Program(Arrays.<Ast.Decl>asList(gDecl, flagDecl, add3Decl, mainDecl));
  }

  /**
   * (Corner cases: nested function, call with 0 args, while with single-stmt body)
   *
   * fn counter(n: int): int {
   *   x: int = 0;
   *
   *   fn inc(): int {
   *     x = x + 1;
   *     return x;
   *   }
   *
   *   while (x < n)
   *     x = inc();
   *
   *   return x;
   * }
   *
   * fn main(): int {
   *   r: int = counter(3);
   *   output(r);
   *   return 0;
   * }
   */
  public static Ast.Program test5() {
    Ast.Param nParam = new Ast.Param("n", Ast.Type.INT);

    Stmt.VarDef xDef = new Stmt.VarDef("x", Ast.Type.INT, new Expr.IntLit(0));

    Expr xPlus1 = new Expr.Binary(new Expr.Var("x"), Expr.BinOp.ADD, new Expr.IntLit(1));
    Stmt.Assign incAssign = new Stmt.Assign("x", xPlus1);
    Stmt.Return incReturn = new Stmt.Return(new Expr.Var("x"));
    Stmt.Block incBody = new Stmt.Block(Arrays.asList(incAssign, incReturn));
    Ast.FunDecl incDecl = new Ast.FunDecl("inc", Arrays.<Ast.Param>asList(), Ast.Type.INT, incBody);
    Stmt.FunDef incDef = new Stmt.FunDef(incDecl);

    Expr whileCond = new Expr.Binary(new Expr.Var("x"), Expr.BinOp.LT, new Expr.Var("n"));
    Expr.Call callInc = new Expr.Call("inc", Arrays.<Expr>asList());
    Stmt.Assign whileBody = new Stmt.Assign("x", callInc);
    Stmt.While whileStmt = new Stmt.While(whileCond, whileBody);

    Stmt.Return retX = new Stmt.Return(new Expr.Var("x"));
    Stmt.Block counterBody = new Stmt.Block(Arrays.asList(xDef, incDef, whileStmt, retX));
    Ast.FunDecl counterDecl = new Ast.FunDecl("counter", Arrays.asList(nParam), Ast.Type.INT, counterBody);

    Expr.Call callCounter = new Expr.Call("counter", Arrays.asList(new Expr.IntLit(3)));
    Stmt.VarDef rDef = new Stmt.VarDef("r", Ast.Type.INT, callCounter);
    Stmt.ExprStmt outR = new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.Var("r"))));
    Stmt.Return ret0 = new Stmt.Return(new Expr.IntLit(0));
    Stmt.Block mainBody = new Stmt.Block(Arrays.asList(rDef, outR, ret0));
    Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.<Ast.Param>asList(), Ast.Type.INT, mainBody);

    return new Ast.Program(Arrays.<Ast.Decl>asList(counterDecl, mainDecl));
  }

  /**
   * (Corner cases: bool params/locals, standalone block stmt, if with non-block branches)
   *
   * fn logic(a: bool, b: bool): bool {
   *   return a && !b || b;
   * }
   *
   * fn main(): int {
   *   t: bool = true;
   *   f: bool = false;
   *   {
   *     z: bool = logic(t, f);
   *     if (z)
   *       output(1);
   *     else
   *       output(0);
   *   }
   *   return 0;
   * }
   */
  public static Ast.Program test6() {
    Ast.Param aParam = new Ast.Param("a", Ast.Type.BOOL);
    Ast.Param bParam = new Ast.Param("b", Ast.Type.BOOL);

    Expr notB = new Expr.Unary(Expr.UnOp.NOT, new Expr.Var("b"));
    Expr aAndNotB = new Expr.Binary(new Expr.Var("a"), Expr.BinOp.LAND, notB);
    Expr logicExpr = new Expr.Binary(aAndNotB, Expr.BinOp.LOR, new Expr.Var("b"));
    Stmt.Block logicBody = new Stmt.Block(Arrays.asList(new Stmt.Return(logicExpr)));
    Ast.FunDecl logicDecl = new Ast.FunDecl("logic", Arrays.asList(aParam, bParam), Ast.Type.BOOL, logicBody);

    Stmt.VarDef tDef = new Stmt.VarDef("t", Ast.Type.BOOL, new Expr.BoolLit(true));
    Stmt.VarDef fDef = new Stmt.VarDef("f", Ast.Type.BOOL, new Expr.BoolLit(false));

    Expr.Call callLogic = new Expr.Call("logic", Arrays.asList(new Expr.Var("t"), new Expr.Var("f")));
    Stmt.VarDef zDef = new Stmt.VarDef("z", Ast.Type.BOOL, callLogic);

    Stmt.ExprStmt out1 = new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.IntLit(1))));
    Stmt.ExprStmt out0 = new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.IntLit(0))));
    Stmt.If ifZ = new Stmt.If(new Expr.Var("z"), out1, out0);

    Stmt.Block innerBlock = new Stmt.Block(Arrays.asList(zDef, ifZ));

    Stmt.Return ret0 = new Stmt.Return(new Expr.IntLit(0));
    Stmt.Block mainBody = new Stmt.Block(Arrays.asList(tDef, fDef, innerBlock, ret0));
    Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.<Ast.Param>asList(), Ast.Type.INT, mainBody);

    return new Ast.Program(Arrays.<Ast.Decl>asList(logicDecl, mainDecl));
  }

  /**
   * (Corner cases: nested calls, call with 0 args, ExprStmt-only main body)
   *
   * fn main(): int {
   *   output(input());
   *   return 0;
   * }
   */
  public static Ast.Program test7() {
    Expr.Call callInput = new Expr.Call("input", Arrays.<Expr>asList());
    Expr.Call callOutput = new Expr.Call("output", Arrays.asList(callInput));
    Stmt.ExprStmt outInput = new Stmt.ExprStmt(callOutput);

    Stmt.Return ret0 = new Stmt.Return(new Expr.IntLit(0));
    Stmt.Block mainBody = new Stmt.Block(Arrays.asList(outInput, ret0));
    Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.<Ast.Param>asList(), Ast.Type.INT, mainBody);

    return new Ast.Program(Arrays.<Ast.Decl>asList(mainDecl));
  }

  /**
   * (Precedence chain stress: !, *, /, %, +, -, <, !=, &&, ||)
   *
   * fn prec(a: int, b: int, c: int, d: int, e: int, f: int, g: int, h: int, i: int, j: int): int {
   *   if (!(a + b * c - d / e % f < g + h) && (i + 1) != j || a * b == c + d) {
   *     return 1;
   *   }
   *   else {
   *     return 0;
   *   }
   * }
   *
   * fn main(): int {
   *   r: int = prec(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
   *   output(r);
   *   return 0;
   * }
   */
  public static Ast.Program test8() {
    // params for prec
    Ast.Param aParam = new Ast.Param("a", Ast.Type.INT);
    Ast.Param bParam = new Ast.Param("b", Ast.Type.INT);
    Ast.Param cParam = new Ast.Param("c", Ast.Type.INT);
    Ast.Param dParam = new Ast.Param("d", Ast.Type.INT);
    Ast.Param eParam = new Ast.Param("e", Ast.Type.INT);
    Ast.Param fParam = new Ast.Param("f", Ast.Type.INT);
    Ast.Param gParam = new Ast.Param("g", Ast.Type.INT);
    Ast.Param hParam = new Ast.Param("h", Ast.Type.INT);
    Ast.Param iParam = new Ast.Param("i", Ast.Type.INT);
    Ast.Param jParam = new Ast.Param("j", Ast.Type.INT);

    // a + b * c - d / e % f
    Expr mulBC = new Expr.Binary(new Expr.Var("b"), Expr.BinOp.MUL, new Expr.Var("c"));
    Expr addA = new Expr.Binary(new Expr.Var("a"), Expr.BinOp.ADD, mulBC);

    Expr divDE = new Expr.Binary(new Expr.Var("d"), Expr.BinOp.DIV, new Expr.Var("e"));
    Expr modDEF = new Expr.Binary(divDE, Expr.BinOp.MOD, new Expr.Var("f"));

    Expr arithLeft = new Expr.Binary(addA, Expr.BinOp.SUB, modDEF);

    // g + h
    Expr addGH = new Expr.Binary(new Expr.Var("g"), Expr.BinOp.ADD, new Expr.Var("h"));

    // (a + b*c - d/e%f) < (g + h)
    Expr ltExpr = new Expr.Binary(arithLeft, Expr.BinOp.LT, addGH);

    // !( ... )
    Expr notLt = new Expr.Unary(Expr.UnOp.NOT, ltExpr);

    // (i + 1) != j
    Expr iPlus1 = new Expr.Binary(new Expr.Var("i"), Expr.BinOp.ADD, new Expr.IntLit(1));
    Expr neExpr = new Expr.Binary(iPlus1, Expr.BinOp.NE, new Expr.Var("j"));

    // !(...) && ((i+1) != j)
    Expr andExpr = new Expr.Binary(notLt, Expr.BinOp.LAND, neExpr);

    // a * b == c + d
    Expr mulAB = new Expr.Binary(new Expr.Var("a"), Expr.BinOp.MUL, new Expr.Var("b"));
    Expr addCD = new Expr.Binary(new Expr.Var("c"), Expr.BinOp.ADD, new Expr.Var("d"));
    Expr eqExpr = new Expr.Binary(mulAB, Expr.BinOp.EQ, addCD);

    // (!(...) && ...) || (a*b == c+d)
    Expr cond = new Expr.Binary(andExpr, Expr.BinOp.LOR, eqExpr);

    // if (...) { return 1; } else { return 0; }
    Stmt.Block thenBlk = new Stmt.Block(Arrays.asList(new Stmt.Return(new Expr.IntLit(1))));
    Stmt.Block elseBlk = new Stmt.Block(Arrays.asList(new Stmt.Return(new Expr.IntLit(0))));
    Stmt.If ifStmt = new Stmt.If(cond, thenBlk, elseBlk);

    Stmt.Block precBody = new Stmt.Block(Arrays.asList(ifStmt));
    Ast.FunDecl precDecl = new Ast.FunDecl(
      "prec",
      Arrays.asList(aParam, bParam, cParam, dParam, eParam, fParam, gParam, hParam, iParam, jParam),
      Ast.Type.INT,
      precBody
    );

    // main
    Expr.Call callPrec = new Expr.Call(
      "prec",
      Arrays.asList(
        new Expr.IntLit(1), new Expr.IntLit(2), new Expr.IntLit(3), new Expr.IntLit(4), new Expr.IntLit(5),
        new Expr.IntLit(6), new Expr.IntLit(7), new Expr.IntLit(8), new Expr.IntLit(9), new Expr.IntLit(10)
      )
    );
    Stmt.VarDef rDef = new Stmt.VarDef("r", Ast.Type.INT, callPrec);
    Stmt.ExprStmt outR = new Stmt.ExprStmt(new Expr.Call("output", Arrays.asList(new Expr.Var("r"))));
    Stmt.Return ret0 = new Stmt.Return(new Expr.IntLit(0));

    Stmt.Block mainBody = new Stmt.Block(Arrays.asList(rDef, outR, ret0));
    Ast.FunDecl mainDecl = new Ast.FunDecl("main", Arrays.<Ast.Param>asList(), Ast.Type.INT, mainBody);

    return new Ast.Program(Arrays.<Ast.Decl>asList(precDecl, mainDecl));
  }

}
