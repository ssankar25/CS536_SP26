package madlang;

import madlang.ast.*;
import java.util.*;

/**
 * Pretty printer entry point for CS536 MadLang.
 *
 * P1 requirement: implement PrettyPrinter.print(program) to return a canonical
 * source representation of the given AST.
 */
public final class PrettyPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    // Contains final, pretty-printed version of AST
    private final StringBuilder out = new StringBuilder();
    private int indent = 0; // Increment to indent further for blocks

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

    private void printProgram(Ast.Program program) {
        // Program = Collection of declarations TODO: Chk this comment
        for (int i = 0; i < program.decls.size(); i++) {
            printDecl(program.decls.get(i));
        }
    }

    private void printDecl(Ast.Decl decl) {
        // TODO: Check if this can be done without checking instaceof for each
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

    private void printIndent() {
        for (int i = 0; i < indent; i++) out.append("  ");
    }

    // LeadingIndent flag passed in to take care of in-line blocks vs. standalone blocks
    private void printBlock(Stmt.Block b, boolean leadingIndent) {
        if (leadingIndent) printIndent();
        out.append("{\n");
        indent++;
        for (Stmt s : b.stmts) s.accept(this);
        indent--;
        printIndent();
        out.append("}\n");
    }

    // Prints as a block if the statement is a block, otherwise it is a single statement
    // printed on the next line
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

    @Override
    public String visitBlockStmt(Stmt.Block s) {
        printBlock(s, true);
        return null;
    }

    @Override
    public String visitReturnStmt(Stmt.Return s) {
        printIndent();
        // return;\n
        out.append("return ").append(s.value.accept(this)).append(";\n");
        return null;
    }

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

    @Override 
    public String visitFunDefStmt(Stmt.FunDef s) { 
        // printIndent is in FunDecl (by convention, any printIndent is in the innermost object)
        printFunDecl(s.fun);
        return null;
    }

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

    @Override
    public String visitExprStmt(Stmt.ExprStmt s) {
        printIndent();
        out.append(s.expr.accept(this)).append(";\n");
        return null;
    }

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
    
    @Override
    public String visitWhileStmt(Stmt.While s)   { 
        printIndent();
        out.append("while (").append(s.cond.accept(this)).append(") ");
        printBody(s.body);
        return null;
    }

    // Expression visitors return strings so that they can be used in the Statement visitors

    @Override
    public String visitIntLitExpr(Expr.IntLit e) {
        return Integer.toString(e.value);
    }

    @Override
    public String visitVarExpr(Expr.Var e) {
        return e.name;
    }

    // Precedence logic -> Grab precedence from either binary or unary, or default to high value since we only
    // insert parentheses when child < parent
    private static int precedenceOf(Expr e) {
        if (e instanceof Expr.Binary bin) return bin.op.precedence();
        if (e instanceof Expr.Unary un)  return un.op.precedence();
        return 100; // IntLit, BoolLit, Var, and Call get "high" precedence so there are no parentheses
    }

    // Used to insert parentheses around an expression after checking the precedence
    // Define the "parent" as the operator that "contains" this expression
    // The "child" is then the predence of the current expression we are trying to print
    // We insert a parentheses around the child when it has LOWER precedence than the parent, because
    // this means the AST bound the lower-precedence operator together, meaning parentheses need to 
    // be around these
    // The other case where we insert parentheses is when both the parent and child are of the same
    // precedence, but the expression passed in is on the right in its parent expression.
    // This means parentheses needs to be around this right expression since it is lower prec by default
    private String printExpr(Expr e, int parentPrec, boolean isRightChild) {
        // Recursively build parentheses based on precedence
        String childString = e.accept(this);
        // childPrec = Precedence of expression passed in
        int childPrec = precedenceOf(e);

        // Case 1: Insert parenthese when expression passed in has lower precedence than its parent
        boolean needParens = childPrec < parentPrec;

        // Case 2: If the child is a binary expression at the SAME precedence, and it's on the right of its
        // parent expression, parentheses are needed to ensure this expression is evaluated first as in the AST.
        if (!needParens && isRightChild && (e instanceof Expr.Binary) && childPrec == parentPrec) {
            needParens = true;
        }

        return needParens ? "(" + childString + ")" : childString;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary e) {
        // Unary binds tighter than all binary ops, so parenthesize when the expression
        // for the operator has a lower precedence (Ex. -(a + b))
        String inner = printExpr(e.expr, e.op.precedence(), false);
        return e.op.toSource() + inner;
    }

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

    @Override 
    public String visitBoolLitExpr(Expr.BoolLit e) {
        return Boolean.toString(e.value);
    }

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
