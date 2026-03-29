package madlang;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Driver class to run test cases for the type checker.
 */
public final class Main {

  /**
   * Main method to run all the tests.
   * 
   * @param args Unused.
   */
  public static void main(String[] args) {
    runAll();
  }

  /**
   * Helper method to run all of the provided test cases.
   */
  public static void runAll() {
    // Basic well-typed tests
    runNormal("builtin_io_and_call", testBuiltinIOAndCall());
    runNormal("block_shadowing", testBlockShadowing());
    runNormal("branch_scope_without_block", testBranchScopeWithoutBlock());
    runNormal("recursive_function", testRecursiveFunction());
    runNormal("nested_function_capture", testNestedFunctionCapture());
    runNormal("function_shadowing", testFunctionShadowing());
    runNormal("global_literal_initializer", testGlobalLiteralInitializer());

    // Corner/error tests from README
    runErr("var_initializer_type_mismatch", testVarInitializerTypeMismatch(), MismatchedTypes.class);
    runErr("assignment_type_mismatch", testAssignmentTypeMismatch(), MismatchedTypes.class);
    runErr("if_condition_not_bool", testIfConditionNotBool(), MismatchedTypes.class);
    runErr("while_condition_not_bool", testWhileConditionNotBool(), MismatchedTypes.class);
    runErr("return_type_mismatch", testReturnTypeMismatch(), MismatchedTypes.class);
    runErr("wrong_argument_count", testWrongArgumentCount(), WrongArgumentCount.class);
    runErr("illegal_application", testIllegalApplication(), IllegalApplication.class);
    runErr("unbound_reference", testUnboundReference(), UnboundReference.class);
    runErr("call_before_declaration", testCallBeforeDeclaration(), UnboundReference.class);

    // Tests for unused identifier analysis
    runUnused("unused_local_var", testUnusedLocalVar(), Set.of("x"));
    runUnused("unused_no_recursive_usage", testUnusedNoRecursiveUsage(), Set.of("y"));
    runUnused("unused_shadowing_same_name", testUnusedShadowingSameName(), Set.of("x"));
    runUnused("unused_self_recursive_function", testUnusedSelfRecursiveFunction(), Set.of("f"));

    System.out.println("All tests passed!");
  }

  /**
   * Helper method to run normal tests that should not error.
   * 
   * @param name Name of the test being run.
   * @param program Program that contains the list of statements to be tested.
   */
  private static void runNormal(String name, List<Stmt> program) {
    run(program, false, null);
    System.out.println("PASS " + name);
  }

  /**
   * Helper method to run tests that are expected to error.
   * 
   * @param name Name of the test being run.
   * @param program Program that contains the list of statements to be tested.
   * @param expectedError The type of error expected from the test case.
   */
  private static void runErr(String name, List<Stmt> program, Class<? extends TypeError> expectedError) {
    run(program, true, expectedError);
    System.out.println("PASS " + name);
  }

  /**
   * Helper method that runs the type checker on a program.
   * 
   * @param program Program that contains the list of statements to be tested.
   * @param expectErr Flag to indicate that an error is expected.
   * @param expectedError The type of error expected. If no error is expected, this can be null.
   */
  private static void run(List<Stmt> program, boolean expectErr, Class<? extends TypeError> expectedError) {
    try {
      // Create the type checker and run it on the program
      TypeChecker checker = new TypeChecker();
      checker.visitProgram(program);

      // If we expect the program to error, then we should not reach here
      if (expectErr) {
        throw new AssertionError(
            "Expected error of type " + expectedError.getSimpleName() + " but program completed.");
      }

    } catch (TypeError e) {
      // If we do not expect the program to error, then throw
      if (!expectErr) {
        throw new AssertionError("Expected program to type check, but got: " + e.getMessage(), e);
      }

      // Check if the error type matches the one we expect for the program
      if (!expectedError.isInstance(e)) {
        throw new AssertionError(
            "Expected error of type " + expectedError.getSimpleName()
                + " but got " + e.getClass().getSimpleName()
                + ": " + e.getMessage(), e);
      }
    }
  }

  /**
   * Helper method to run tests for the unused identifier analysis.
   * 
   * @param name Name of the test being run.
   * @param program Program that contains the list of statements to be tested.
   * @param expectedUnused The expected set of unused identifiers.
   */
  private static void runUnused(String name, List<Stmt> program, Set<String> expectedUnused) {
    UnusedIdentifierAnalysis analysis = new UnusedIdentifierAnalysis();
    Set<String> actualUnused = analysis.unused(program);

    if (!actualUnused.equals(expectedUnused)) {
      throw new AssertionError(
          "Expected unused set " + expectedUnused + " but got " + actualUnused);
    }

    System.out.println("PASS " + name);
  }

  /// TESTS ///

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = input();
   *   output(x);
   *   return 0;
   * }
   */
  public static List<Stmt> testBuiltinIOAndCall() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Call("input", List.of())),
            new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("x")))),
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * x: int = 10;
   *
   * fn main(): int {
   *   {
   *     x: int = 20;
   *   }
   *   return x;
   * }
   */
  public static List<Stmt> testBlockShadowing() {
    List<Stmt> program = new ArrayList<>();

    program.add(new Stmt.Var("x", VarType.INT, new Expr.Literal(10)));

    Stmt.Block inner = new Stmt.Block(List.of(
        new Stmt.Var("x", VarType.INT, new Expr.Literal(20))
    ));

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            inner,
            new Stmt.Return(new Expr.Variable("x"))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = 0;
   *   if (true)
   *     x: int = 1;
   *   else
   *     x: int = 2;
   *   return x;
   * }
   *
   * Checks that branches create a new scope even without block syntax.
   */
  public static List<Stmt> testBranchScopeWithoutBlock() {
    List<Stmt> program = new ArrayList<>();

    Stmt.If ifStmt = new Stmt.If(
        new Expr.Literal(true),
        new Stmt.Var("x", VarType.INT, new Expr.Literal(1)),
        new Stmt.Var("x", VarType.INT, new Expr.Literal(2))
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(0)),
            ifStmt,
            new Stmt.Return(new Expr.Variable("x"))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn countDown(n: int): int {
   *   if (n == 0)
   *     return 0;
   *   else
   *     return countDown(n - 1);
   * }
   *
   * fn main(): int {
   *   return countDown(3);
   * }
   *
   * Checks direct recursion.
   */
  public static List<Stmt> testRecursiveFunction() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function countDown = new Stmt.Function(
        "countDown",
        VarType.INT,
        List.of(new Stmt.Parameter("n", VarType.INT)),
        List.of(
            new Stmt.If(
                new Expr.Binary(new Expr.Variable("n"), Operator.EQUAL, new Expr.Literal(0)),
                new Stmt.Return(new Expr.Literal(0)),
                new Stmt.Return(
                    new Expr.Call(
                        "countDown",
                        List.of(
                            new Expr.Binary(new Expr.Variable("n"), Operator.MINUS, new Expr.Literal(1))
                        )
                    )
                )
            )
        )
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Call("countDown", List.of(new Expr.Literal(3))))
        )
    );

    program.add(countDown);
    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn outer(): int {
   *   x: int = 1;
   *   fn inner(): int {
   *     return x;
   *   }
   *   return inner();
   * }
   *
   * fn main(): int {
   *   return outer();
   * }
   *
   * Checks nested function capture.
   */
  public static List<Stmt> testNestedFunctionCapture() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function inner = new Stmt.Function(
        "inner",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Variable("x"))
        )
    );

    Stmt.Function outer = new Stmt.Function(
        "outer",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(1)),
            inner,
            new Stmt.Return(new Expr.Call("inner", List.of()))
        )
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Call("outer", List.of()))
        )
    );

    program.add(outer);
    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn f(x: int): int {
   *   return x + 1;
   * }
   *
   * fn main(): int {
   *   fn f(x: int): int {
   *     return x * 2;
   *   }
   *   return f(10);
   * }
   *
   * Checks function shadowing in nested scopes.
   */
  public static List<Stmt> testFunctionShadowing() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function outerF = new Stmt.Function(
        "f",
        VarType.INT,
        List.of(new Stmt.Parameter("x", VarType.INT)),
        List.of(
            new Stmt.Return(
                new Expr.Binary(new Expr.Variable("x"), Operator.PLUS, new Expr.Literal(1))
            )
        )
    );

    Stmt.Function innerF = new Stmt.Function(
        "f",
        VarType.INT,
        List.of(new Stmt.Parameter("x", VarType.INT)),
        List.of(
            new Stmt.Return(
                new Expr.Binary(new Expr.Variable("x"), Operator.MULTIPLY, new Expr.Literal(2))
            )
        )
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            innerF,
            new Stmt.Return(new Expr.Call("f", List.of(new Expr.Literal(10))))
        )
    );

    program.add(outerF);
    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * x: int = 1 + 2 * 3;
   * flag: bool = !false || (true && false);
   *
   * fn main(): int {
   *   return 0;
   * }
   *
   * Checks legal global literal-only initializers.
   */
  public static List<Stmt> testGlobalLiteralInitializer() {
    List<Stmt> program = new ArrayList<>();

    program.add(new Stmt.Var(
        "x",
        VarType.INT,
        new Expr.Binary(
            new Expr.Literal(1),
            Operator.PLUS,
            new Expr.Binary(new Expr.Literal(2), Operator.MULTIPLY, new Expr.Literal(3))
        )
    ));

    program.add(new Stmt.Var(
        "flag",
        VarType.BOOL,
        new Expr.Binary(
            new Expr.Unary(Operator.NOT, new Expr.Literal(false)),
            Operator.OR,
            new Expr.Binary(new Expr.Literal(true), Operator.AND, new Expr.Literal(false))
        )
    ));

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = true;
   *   return 0;
   * }
   */
  public static List<Stmt> testVarInitializerTypeMismatch() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(true)),
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = 0;
   *   x = false;
   *   return 0;
   * }
   */
  public static List<Stmt> testAssignmentTypeMismatch() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(0)),
            new Stmt.Assign("x", new Expr.Literal(false)),
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   if (1)
   *     return 0;
   *   else
   *     return 1;
   * }
   */
  public static List<Stmt> testIfConditionNotBool() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.If(
                new Expr.Literal(1),
                new Stmt.Return(new Expr.Literal(0)),
                new Stmt.Return(new Expr.Literal(1))
            )
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   while (1)
   *     return 0;
   *   return 1;
   * }
   */
  public static List<Stmt> testWhileConditionNotBool() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.While(
                new Expr.Literal(1),
                new Stmt.Return(new Expr.Literal(0))
            ),
            new Stmt.Return(new Expr.Literal(1))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn f(): bool {
   *   return 1;
   * }
   *
   * fn main(): int {
   *   return 0;
   * }
   */
  public static List<Stmt> testReturnTypeMismatch() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function f = new Stmt.Function(
        "f",
        VarType.BOOL,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Literal(1))
        )
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(f);
    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn f(x: int): int {
   *   return x;
   * }
   *
   * fn main(): int {
   *   return f(1, 2);
   * }
   */
  public static List<Stmt> testWrongArgumentCount() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function f = new Stmt.Function(
        "f",
        VarType.INT,
        List.of(new Stmt.Parameter("x", VarType.INT)),
        List.of(
            new Stmt.Return(new Expr.Variable("x"))
        )
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Call("f", List.of(new Expr.Literal(1), new Expr.Literal(2))))
        )
    );

    program.add(f);
    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   f: int = 7;
   *   return f();
   * }
   *
   * Checks calling a variable as if it were a function.
   */
  public static List<Stmt> testIllegalApplication() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("f", VarType.INT, new Expr.Literal(7)),
            new Stmt.Return(new Expr.Call("f", List.of()))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   return z;
   * }
   */
  public static List<Stmt> testUnboundReference() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Variable("z"))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn f(): int {
   *   return g();
   * }
   *
   * fn g(): int {
   *   return 0;
   * }
   *
   * Checks that a function must be declared before use,
   * except for direct recursion.
   */
  public static List<Stmt> testCallBeforeDeclaration() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function f = new Stmt.Function(
        "f",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Call("g", List.of()))
        )
    );

    Stmt.Function g = new Stmt.Function(
        "g",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(f);
    program.add(g);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = 1;
   *   return 0;
   * }
   *
   * Checks the README example of an unused local variable.
   */
  public static List<Stmt> testUnusedLocalVar() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(1)),
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = 1;
   *   y: int = x + 1;
   *   return 0;
   * }
   *
   * Checks the README rule that recursive usage analysis is not required:
   * x is considered used even though y is unused.
   */
  public static List<Stmt> testUnusedNoRecursiveUsage() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(1)),
            new Stmt.Var(
                "y",
                VarType.INT,
                new Expr.Binary(new Expr.Variable("x"), Operator.PLUS, new Expr.Literal(1))
            ),
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn main(): int {
   *   x: int = 1;
   *   {
   *     x: int = 2;
   *     output(x);
   *   }
   *   return 0;
   * }
   *
   * Checks shadowing: the outer x is unused even though the inner x is used.
   */
  public static List<Stmt> testUnusedShadowingSameName() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Block inner = new Stmt.Block(List.of(
        new Stmt.Var("x", VarType.INT, new Expr.Literal(2)),
        new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("x"))))
    ));

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(1)),
            inner,
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(main);
    return program;
  }

  /**
   * Program:
   *
   * fn f(): int {
   *   return f();
   * }
   *
   * fn main(): int {
   *   return 0;
   * }
   *
   * Checks that self-recursive reference does not count as usage,
   * while global main is always considered used.
   */
  public static List<Stmt> testUnusedSelfRecursiveFunction() {
    List<Stmt> program = new ArrayList<>();

    Stmt.Function f = new Stmt.Function(
        "f",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Call("f", List.of()))
        )
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Return(new Expr.Literal(0))
        )
    );

    program.add(f);
    program.add(main);
    return program;
  }
}