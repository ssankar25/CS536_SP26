package madlang.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Statements for MadLang.
 * Includes nested function definitions as a statement (FunDef).
 */
public abstract class Stmt {
    public final Ast.Span span; // may be null

    protected Stmt(Ast.Span span) {
        this.span = span;
    }

    public interface Visitor<R> {
        R visitBlockStmt(Block s);
        R visitVarDefStmt(VarDef s);
        R visitFunDefStmt(FunDef s);
        R visitAssignStmt(Assign s);
        R visitIfStmt(If s);
        R visitWhileStmt(While s);
        R visitReturnStmt(Return s);
        R visitExprStmt(ExprStmt s);
    }

    /** { stmt* } */
    public static final class Block extends Stmt {
        public final List<Stmt> stmts;

        public Block(List<Stmt> stmts) {
            this(stmts, null);
        }

        public Block(List<Stmt> stmts, Ast.Span span) {
            super(span);
            this.stmts = unmodifiableCopy(stmts);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitBlockStmt(this); }
    }

    /** Local variable definition: x: int;  or  x: int = expr; */
    public static final class VarDef extends Stmt {
        public final String name;
        public final Ast.Type type;
        public final Expr initOrNull; // may be null

        public VarDef(String name, Ast.Type type, Expr initOrNull) {
            this(name, type, initOrNull, null);
        }

        public VarDef(String name, Ast.Type type, Expr initOrNull, Ast.Span span) {
            super(span);
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.initOrNull = initOrNull;
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitVarDefStmt(this); }
    }

    /** Nested function definition statement: fn f(...): T { ... } */
    public static final class FunDef extends Stmt {
        public final Ast.FunDecl fun;

        public FunDef(Ast.FunDecl fun) {
            this(fun, null);
        }

        public FunDef(Ast.FunDecl fun, Ast.Span span) {
            super(span);
            this.fun = Objects.requireNonNull(fun);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitFunDefStmt(this); }
    }

    /** Assignment statement: name = rhs; */
    public static final class Assign extends Stmt {
        public final String name;
        public final Expr rhs;

        public Assign(String name, Expr rhs) {
            this(name, rhs, null);
        }

        public Assign(String name, Expr rhs, Ast.Span span) {
            super(span);
            this.name = Objects.requireNonNull(name);
            this.rhs = Objects.requireNonNull(rhs);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitAssignStmt(this); }
    }

    public static final class If extends Stmt {
        public final Expr cond;
        public final Stmt thenBranch;
        public final Stmt elseBranchOrNull; // null means no else

        public If(Expr cond, Stmt thenBranch, Stmt elseBranchOrNull) {
            this(cond, thenBranch, elseBranchOrNull, null);
        }

        public If(Expr cond, Stmt thenBranch, Stmt elseBranchOrNull, Ast.Span span) {
            super(span);
            this.cond = Objects.requireNonNull(cond);
            this.thenBranch = Objects.requireNonNull(thenBranch);
            this.elseBranchOrNull = elseBranchOrNull;
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitIfStmt(this); }
    }

    public static final class While extends Stmt {
        public final Expr cond;
        public final Stmt body;

        public While(Expr cond, Stmt body) {
            this(cond, body, null);
        }

        public While(Expr cond, Stmt body, Ast.Span span) {
            super(span);
            this.cond = Objects.requireNonNull(cond);
            this.body = Objects.requireNonNull(body);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitWhileStmt(this); }
    }

    /** return expr; (expr is mandatory; no void returns) */
    public static final class Return extends Stmt {
        public final Expr value;

        public Return(Expr value) {
            this(value, null);
        }

        public Return(Expr value, Ast.Span span) {
            super(span);
            this.value = Objects.requireNonNull(value);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitReturnStmt(this); }
    }

    /** Expression statement, e.g., output(x); */
    public static final class ExprStmt extends Stmt {
        public final Expr expr;

        public ExprStmt(Expr expr) {
            this(expr, null);
        }

        public ExprStmt(Expr expr, Ast.Span span) {
            super(span);
            this.expr = Objects.requireNonNull(expr);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitExprStmt(this); }
    }

    public abstract <R> R accept(Visitor<R> visitor);

    private static <T> List<T> unmodifiableCopy(List<T> xs) {
        Objects.requireNonNull(xs);
        return Collections.unmodifiableList(new ArrayList<T>(xs));
    }
}
