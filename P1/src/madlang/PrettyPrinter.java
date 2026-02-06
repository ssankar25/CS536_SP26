package madlang;

import madlang.ast.Ast;
import madlang.ast.Expr;
import madlang.ast.Stmt;

/**
 * Pretty printer entry point for CS536 MadLang.
 *
 * P1 requirement: implement PrettyPrinter.print(program) to return a canonical
 * source representation of the given AST.
 */
public final class PrettyPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    // Contains final, pretty-printed version of the MadLang AST
    private final StringBuilder out = new StringBuilder();

    // Indentation level for the current block, which is incremented and 
    // decremented according to the current scope
    private int indent = 0;

    private PrettyPrinter() {}

    /**
     * Pretty-print a MadLang program AST into a canonical source string.
     *
     * @param program the AST of a whole MadLang program
     * @return the pretty-printed program as a string
     */
    public static String pretty(Ast.Program program) {
        PrettyPrinter p = new PrettyPrinter();
        p.printProgram(program);
        return p.out.toString();
    }

    /**
     * Goes through each declaration in the program and prints them.
     * 
     * @param program the AST of a whole MadLang program
     */
    private void printProgram(Ast.Program program) {
        // Program = Collection of declarations
        for (int i = 0; i < program.decls.size(); i++) {
            printDecl(program.decls.get(i));
        }
    }

    /**
     * Based on whether the declaration is a global variable declaration or 
     * a function declaration, this helper pretty-prints the declaration.
     * 
     * @param decl The current declaration in the AST being processed.
     */
    private void printDecl(Ast.Decl decl) {
        // Check what type of declaration decl is since the Ast class
        // does not have a visitor interface
        if (decl instanceof Ast.GlobalVarDecl globalDecl) {
            printIndent();

            // name: type 
            out.append(globalDecl.name).append(": ").append(globalDecl.type.toSource());
            if (globalDecl.initOrNull != null) {
                // name: type = expr
                out.append(" = ").append(globalDecl.initOrNull.accept(this));
            }

            out.append(";\n");
        }
        else if (decl instanceof Ast.FunDecl funDecl) {
            printFunDecl(funDecl);
        }
        else {
            throw new IllegalStateException("Unknown decl: " + decl.getClass());
        }
    }

    /**
     * Handles the printing of a function declaration. This is done through 
     * this helper method as there is no visitor interface in the Ast class.
     * 
     * @param funDecl The function declaration being printed.
     */
    private void printFunDecl(Ast.FunDecl funDecl) {
        printIndent();

        // fn name(
        out.append("fn ").append(funDecl.name).append("(");
        for (int i = 0; i < funDecl.params.size(); i++) {
            // fn name(params
            Ast.Param param = funDecl.params.get(i);
            out.append(param.name).append(": ").append(param.type.toSource());
            if (i < funDecl.params.size() - 1) out.append(", ");
        }

        // fn name(params): {
        out.append("): ").append(funDecl.returnType.toSource()).append(" ");
        printBlock(funDecl.body, false);
    }

    /**
     * Helper method to print the correct number of spaces based on
     * the current indentation level
     */
    private void printIndent() {
        for (int i = 0; i < indent; i++) out.append("  ");
    }

    /**
     * Helper method that pretty-prints a block of statements. A leading indent is added
     * based on the flag passed in, which is true for standalone blocks in the visitBlockStmt
     * visitor. Otherwise, in other visitors with in-line blocks (ex. If), there is no leading
     * indent.
     * 
     * @param b The block statement being pretty-printed.
     * @param leadingIndent Boolean indicating whether to have an indent before the first brace.
     */
    private void printBlock(Stmt.Block b, boolean leadingIndent) {
        if (leadingIndent) printIndent();
        out.append("{\n");
        indent++;
        for (Stmt s : b.stmts) s.accept(this);
        indent--;
        printIndent();
        out.append("}\n");
    }

    /**
     * Helper method used to handle printing the body of an If and While loop, which may
     * or may not be a block of statements. If it is a block, then we print the block.
     * Otherwise, it is a single statement on the next line, so we call the corresponding
     * visitor.
     * 
     * @param body The statement being pretty-printed
     */
    private void printBody(Stmt body) {
        if (body instanceof Stmt.Block b) {
            // In-line block statement
            printBlock(b, false);
        } else {
            out.append("\n");
            indent++;
            body.accept(this);
            indent--;
        }
    }


    /////////////////////
    /// STMT VISITORS ///
    /////////////////////

    // Stmt visitors append directly to the out stringbuilder, so they
    // all return null.

    /**
     * Pretty-print visitor for the Stmt.Block type.
     * 
     * @param s The Stmt.Block being visited.
     * @return null
     */
    @Override
    public String visitBlockStmt(Stmt.Block s) {
        printBlock(s, true);
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.Return type.
     * 
     * @param s The Stmt.Return being visited.
     * @return null
     */
    @Override
    public String visitReturnStmt(Stmt.Return s) {
        printIndent();
        // return <expr>;\n
        out.append("return ").append(s.value.accept(this)).append(";\n");
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.VarDef type.
     * 
     * @param s The Stmt.VarDef being visited.
     * @return null
     */
    @Override 
    public String visitVarDefStmt(Stmt.VarDef s) { 
        printIndent();
        out.append(s.name).append(": ").append(s.type.toSource());
        if (s.initOrNull != null) {
            out.append(" = ").append(s.initOrNull.accept(this));
        }
        out.append(";\n");
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.FunDef type.
     * 
     * @param s The Stmt.FunDef being visited.
     * @return null
     */
    @Override 
    public String visitFunDefStmt(Stmt.FunDef s) { 
        printFunDecl(s.fun);
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.Assign type.
     * 
     * @param s The Stmt.Assign being visited.
     * @return null
     */
    @Override 
    public String visitAssignStmt(Stmt.Assign s) { 
        printIndent();
        // LHS = RHS;\n
        out.append(s.name)
           .append(" = ")
           .append(s.rhs.accept(this))
           .append(";\n");
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.ExprStmt type.
     * 
     * @param s The Stmt.ExprStmt being visited.
     * @return null
     */
    @Override
    public String visitExprStmt(Stmt.ExprStmt s) {
        printIndent();
        out.append(s.expr.accept(this)).append(";\n");
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.If type.
     * 
     * @param s The Stmt.If being visited.
     * @return null
     */
    @Override
    public String visitIfStmt(Stmt.If s) { 
        printIndent();
        out.append("if (").append(s.cond.accept(this)).append(") ");
        printBody(s.thenBranch);

        if (s.elseBranchOrNull != null) {
            printIndent();
            out.append("else ");
            printBody(s.elseBranchOrNull);
        }
        return null;
    }

    /**
     * Pretty-print visitor for the Stmt.While type.
     * 
     * @param s The Stmt.While being visited.
     * @return null
     */
    @Override
    public String visitWhileStmt(Stmt.While s)   { 
        printIndent();
        out.append("while (").append(s.cond.accept(this)).append(") ");
        printBody(s.body);
        return null;
    }

    //////////////////////////
    /// PRECEDENCE HELPERS ///
    //////////////////////////

    /**
     * Helper method that gets the precedence of the passed-in expression if it is either binary or unary.
     * If the expression is not binary or unary, then we default to a high precedence (100) because we do
     * NOT want to insert parentheses around literals, and we do insert parentheses for low-precedence expressions.
     * 
     * @param e The expression we are getting the precedence of.
     * @return The precedence of e if it is a unary or binary expression, otherwise 100 (a high precedence).
     */
    private static int precedenceOf(Expr e) {
        if (e instanceof Expr.Binary bin) return bin.op.precedence();
        if (e instanceof Expr.Unary un)  return un.op.precedence();
        return 100; // IntLit, BoolLit, Var, and Call get "high" precedence so there are no parentheses
    }

    /**
     * Helper method that first gets the correct String for the passed-in child expression
     * by recursively calling the visitor, and then inserting parentheses around the child
     * based on its precedence. The 2 cases for inserting parentheses are outlined below.
     * 
     * "Parent" - Expression that "contains" the one passed in (parent node in the AST).
     * 
     * Case 1: Insert a parentheses around the child when it has LOWER precedence than the parent, because
     * this means the AST bound the lower-precedence operator together, meaning parentheses need to 
     * be around these.
     * 
     * Case 2: When both the parent and child are of the same precedence, but the expression passed in 
     * is on the right in its parent expression. This means parentheses needs to be around this right 
     * expression since it is lower prec by default
     * 
     * @param e The "child" expression passed in that is being determined for parentheses-wrapping
     * @param parentPrec The precedence of the "parent" expression, as defined above.
     * @param isRightChild Boolean indicating whether e is on the right of the parent expression, needed for case 2.
     * @return The final pretty-printed string of the child expression with or without parentheses based on precedence.
     */
    private String printExpr(Expr e, int parentPrec, boolean isRightChild) {
        // Recursively build parentheses based on precedence
        String childString = e.accept(this);
        // childPrec = Precedence of expression passed in
        int childPrec = precedenceOf(e);

        // Case 1: Insert parentheses when expression passed in has lower precedence than its parent
        boolean needParens = childPrec < parentPrec;

        // Case 2: If the child is a binary expression at the SAME precedence, and it's on the right of its
        // parent expression, parentheses are needed to ensure this expression is evaluated first as in the AST.
        if (!needParens && isRightChild && (e instanceof Expr.Binary) && childPrec == parentPrec) {
            needParens = true;
        }

        return needParens ? "(" + childString + ")" : childString;
    }

    /////////////////////
    /// EXPR VISITORS ///
    /////////////////////

    // Expression visitors return strings so they can be appended on in the statement
    // visitors, as statements contain expressions.

    /**
     * Pretty-print visitor for the Expr.Unary type.
     * 
     * Inserts parentheses around the unary based on its precedence.
     * 
     * @param e The Expr.Unary being visited.
     * @return The pretty-printed expression.
     */
    @Override
    public String visitUnaryExpr(Expr.Unary e) {
        // Unary binds tighter than all binary ops, so parenthesize when the 
        // expression for the operator has a lower precedence (Ex. -(a + b))
        String inner = printExpr(e.expr, e.op.precedence(), false);
        return e.op.toSource() + inner;
    }

    /**
     * Pretty-print visitor for the Expr.Binary type.
     * 
     * Inserts parentheses around each child expression of
     * the binary statement, and then joins the two halves together.
     * 
     * @param e The Expr.Binary being visited.
     * @return The pretty-printed expression.
     */
    @Override
    public String visitBinaryExpr(Expr.Binary e) {

        // "Parent" precedence that will be passed in for each left and right subtree
        int parentPrec = e.op.precedence();

        // Left child: parentheses only if lower precedence than parent
        String l = printExpr(e.left, parentPrec, false);

        // Right child: parentheses if lower precedence than parent OR same precedence
        String r = printExpr(e.right, parentPrec, true);

        // Recursive join for final binary expression
        // No parentheses are added around the root expression
        return l + " " + e.op.toSource() + " " + r;
    }

    /**
     * Pretty-print visitor for the Expr.IntLit type.
     * 
     * @param e The Expr.IntLit being visited.
     * @return The pretty-printed expression.
     */
    @Override
    public String visitIntLitExpr(Expr.IntLit e) {
        return Integer.toString(e.value);
    }

    /**
     * Pretty-print visitor for the Expr.Var type.
     * 
     * @param e The Expr.Var being visited.
     * @return The pretty-printed expression.
     */
    @Override
    public String visitVarExpr(Expr.Var e) {
        return e.name;
    }

    /**
     * Pretty-print visitor for the Expr.BoolLit type.
     * 
     * @param e The Expr.BoolLit being visited.
     * @return The pretty-printed expression.
     */
    @Override 
    public String visitBoolLitExpr(Expr.BoolLit e) {
        return Boolean.toString(e.value);
    }

    /**
     * Pretty-print visitor for the Expr.Call type.
     * 
     * @param e The Expr.Call being visited.
     * @return The pretty-printed expression.
     */
    @Override
    public String visitCallExpr(Expr.Call e) {
        StringBuilder sb = new StringBuilder();

        // Calling function name
        sb.append(e.callee).append("(");
        for (int i = 0; i < e.args.size(); i++) {
            // Add on arguments if they are there
            if (i > 0) sb.append(", ");
            sb.append(e.args.get(i).accept(this));
        }
    
        sb.append(")");

        return sb.toString();
    }
}
