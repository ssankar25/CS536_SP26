package madlang;

import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        runAll();
    }

    public static void runAll() {
        // Basic tests
        runNormal("basic_arithmetic", testBasicArithmetic(), "", "15\n");
        runNormal("while_loop", testWhileLoop(), "", "3\n2\n1\n");
        runNormal("block_shadowing", testBlockShadowing(), "", "10\n20\n10\n");
        runNormal("default_return_value", testDefaultReturnValue(), "", "0\n");
        runNormal("short_circuit_avoids_div0", testShortCircuitAvoidsDiv0(), "", "123\n");

        // Corner/error tests (3 required types from README)
        runErr("type_mismatch", testTypeMismatch(), "", "type mismatch");
        runErr("unbound_reference", testUnboundReference(), "", "unbound reference");
        runErr("arithmetic_error", testArithmeticError(), "", "arithmetic error");

        // Output/input tests
        runNormal("output_can_be_shadowed", testOutputShadowing(), "", "7\n");
        runNormal("builtin_input_echo", testBuiltinInputEcho(), "42\n", "42\n");

        // More corner cases
        runNormal("short_circuit_or_skips_bad_rhs", testShortCircuitOrSkipsBadRHS(), "", "1\n");
        runNormal("shadow_input_with_var", testShadowInputWithVar(), "42\n", "7\n42\n");

        System.out.println("All tests passed!");
    }

    // Method to run normal tests that shouldn't error
    private static void runNormal(String name, List<Stmt> program, String testInput, String expectedOut) {

        // Run the program and get the output
        String out = run(program, testInput, false, null);

        // Check that the output matches after getting rid of the \r output from Windows System.out.println
        if (!out.replace("\r\n", "\n").equals(expectedOut)) {
            throw new AssertionError("FAIL " + name + "\nExpected:\n" + expectedOut + "\nGot:\n" + out);
        }

        System.out.println("PASS " + name);
    }

    // Method to run tests that should have an error message containing mustContain
    private static void runErr(String name, List<Stmt> program, String testInput, String mustContain) {
        run(program, testInput, true, mustContain);
        System.out.println("PASS " + name);
    }

    private static String run(List<Stmt> program, String testInput, boolean expectErr, String mustContain) {

        // Save the standard input/output stream
        PrintStream oldOut = System.out;
        InputStream oldIn = System.in;

        // Redirect program output to go to the customTestOut variable
        ByteArrayOutputStream customTestOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(customTestOut));

        // Redirect any input (if passed in to test input builtin) to be the program's stdin
        ByteArrayInputStream customTestIn = new ByteArrayInputStream(testInput.getBytes());
        System.setIn(customTestIn);

        try {
            // Create the interpreter and run the program
            Interpreter interp = new Interpreter();
            interp.interpret(program);

            // If we expect the program to error, then we should not reach here
            if (expectErr) {
                throw new AssertionError("Expected error containing: " + mustContain + " but program completed.");
            }

            // Return output to check against the expected
            return customTestOut.toString();

        } catch (IllegalStateException e) {

            // If we don't expect the program to error (runNormal) then just throw the exception
            if (!expectErr) throw e;

            // Check if the error message matches the one we expect for the program
            String msg = (e.getMessage() == null) ? "" : e.getMessage().toLowerCase();
            if (!msg.contains(mustContain.toLowerCase())) {
                throw new AssertionError("Expected error containing '" + mustContain + "' but got: " + e.getMessage());
            }

            // If we reach here, then we got the right exception (only in runErr)
            return null;

        } finally {
            // Reset the system stdin and stdout
            System.setOut(oldOut);
            System.setIn(oldIn);
        }
    }


    /// TESTS ///
    
    /**
     * Program:
     *
     * fn main(): int {
     *   a: int = 10;
     *   b: int = 5;
     *   output(a + b);
     *   return 0;
     * }
     */
    public static List<Stmt> testBasicArithmetic() {
        // Program is a list of statements
        List<Stmt> program = new ArrayList<>();

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Var("a", VarType.INT, new Expr.Literal(10)),
                new Stmt.Var("b", VarType.INT, new Expr.Literal(5)),
                new Stmt.Expression(
                    new Expr.Call("output",
                        List.of(new Expr.Binary(new Expr.Variable("a"), Operator.PLUS, new Expr.Variable("b"))))),
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
     *   x: int = 3;
     *   while (x > 0) {
     *     output(x);
     *     x = x - 1;
     *   }
     *   return 0;
     * }
     */
    public static List<Stmt> testWhileLoop() {
        List<Stmt> program = new ArrayList<>();

        Stmt.While loop = new Stmt.While(
            new Expr.Binary(new Expr.Variable("x"), Operator.GREATER, new Expr.Literal(0)),
            new Stmt.Block(List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("x")))),
                new Stmt.Assign("x", new Expr.Binary(new Expr.Variable("x"), Operator.MINUS, new Expr.Literal(1)))
            ))
    );

    Stmt.Function main = new Stmt.Function(
        "main",
        VarType.INT,
        List.of(),
        List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(3)),
            loop,
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
     *   output(x);
     *   {
     *     x: int = 20;
     *     output(x);
     *   }
     *   output(x);
     *   return 0;
     * }
     */
    public static List<Stmt> testBlockShadowing() {
        List<Stmt> program = new ArrayList<>();

        program.add(new Stmt.Var("x", VarType.INT, new Expr.Literal(10)));

        Stmt.Block inner = new Stmt.Block(List.of(
            new Stmt.Var("x", VarType.INT, new Expr.Literal(20)),
            new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("x"))))
        ));

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("x")))),
                inner,
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
     * fn f(): int {
     *   x: int = 99;
     *   // no explicit return
     * }
     *
     * fn main(): int {
     *   output(f());
     *   return 0;
     * }
     *
     * Expected: default return value for int (0)
     */
    public static List<Stmt> testDefaultReturnValue() {
        List<Stmt> program = new ArrayList<>();

        Stmt.Function f = new Stmt.Function(
            "f",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Var("x", VarType.INT, new Expr.Literal(99))
                // no return
            )
        );

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Call("f", List.of())))),
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
     * fn main(): int {
     *   // RHS must NOT be evaluated
     *   false && (1 / 0);
     *   output(123);
     *   return 0;
     * }
     */
    public static List<Stmt> testShortCircuitAvoidsDiv0() {
        List<Stmt> program = new ArrayList<>();

        Expr div0 = new Expr.Binary(new Expr.Literal(1), Operator.DIVIDE, new Expr.Literal(0));
        Expr shortCircuit = new Expr.Binary(new Expr.Literal(false), Operator.AND, div0);

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Expression(shortCircuit),
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Literal(123)))),
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
     *   output(true);   // type mismatch (output expects int)
     *   return 0;
     * }
     */
    public static List<Stmt> testTypeMismatch() {
        List<Stmt> program = new ArrayList<>();

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Literal(true)))),
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
     *   output(z);   // z is unbound
     *   return 0;
     * }
     */
    public static List<Stmt> testUnboundReference() {
        List<Stmt> program = new ArrayList<>();

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("z")))),
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
     *   output(1 / 0);  // arithmetic error
     *   return 0;
     * }
     */
    public static List<Stmt> testArithmeticError() {
        List<Stmt> program = new ArrayList<>();

        Expr bad = new Expr.Binary(new Expr.Literal(1), Operator.DIVIDE, new Expr.Literal(0));

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(bad))),
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
     *   {
     *     fun output(x: int): int { return 0; }  // shadows builtin output
     *     output(999);                           // should NOT print
     *   }
     *   output(7);  // builtin output restored (prints 7)
     *   return 0;
     * }
     */
    public static List<Stmt> testOutputShadowing() {
        List<Stmt> program = new ArrayList<>();

        Stmt.Function shadowOutput = new Stmt.Function(
            "output",
            VarType.INT,
            List.of(new Stmt.Parameter("x", VarType.INT)),
            List.of(
                new Stmt.Return(new Expr.Literal(0))
            )
        );

        Stmt.Block inner = new Stmt.Block(List.of(
            shadowOutput,
            new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Literal(999))))
        ));

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                inner,
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Literal(7)))),
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
     *   x: int = input();
     *   output(x);
     *   return 0;
     * }
     *
     * (stdin provides a valid integer)
     */
    public static List<Stmt> testBuiltinInputEcho() {
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
     * fn main(): int {
     *   b: bool = true || (1 + false);  // RHS must NOT be evaluated
     *   if (b) {
     *     output(1);
     *   } else {
     *     output(0);
     *   }
     *   return 0;
     * }
     */
    public static List<Stmt> testShortCircuitOrSkipsBadRHS() {
        List<Stmt> program = new ArrayList<>();

        Expr badRhs = new Expr.Binary(new Expr.Literal(1), Operator.PLUS, new Expr.Literal(false));
        Expr orExpr = new Expr.Binary(new Expr.Literal(true), Operator.OR, badRhs);

        Stmt.Var bDecl = new Stmt.Var("b", VarType.BOOL, orExpr);

        Stmt.If ifStmt = new Stmt.If(
            new Expr.Variable("b"),
            new Stmt.Block(List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Literal(1))))
            )),
            new Stmt.Block(List.of(
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Literal(0))))
            ))
        );

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                bDecl,
                ifStmt,
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
     *   {
     *     input: int = 7;   // shadows builtin input() inside this block
     *     output(input);    // prints 7
     *   }
     *   x: int = input();   // builtin input() visible again (reads stdin)
     *   output(x);          // prints the read value (stdin provides 42)
     *   return 0;
     * }
     *
     */
    public static List<Stmt> testShadowInputWithVar() {
        List<Stmt> program = new ArrayList<>();

        Stmt.Block inner = new Stmt.Block(List.of(
            new Stmt.Var("input", VarType.INT, new Expr.Literal(7)),
            new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("input"))))
        ));

        Stmt.Function main = new Stmt.Function(
            "main",
            VarType.INT,
            List.of(),
            List.of(
                inner,
                new Stmt.Var("x", VarType.INT, new Expr.Call("input", List.of())),
                new Stmt.Expression(new Expr.Call("output", List.of(new Expr.Variable("x")))),
                new Stmt.Return(new Expr.Literal(0))
            )
        );

        program.add(main);
        return program;
    }
}