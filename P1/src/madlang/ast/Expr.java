package madlang.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Expressions for MadLang.
 */
public abstract class Expr {
    public final Ast.Span span; // may be null

    protected Expr(Ast.Span span) {
        this.span = span;
    }

    public interface Visitor<R> {
        R visitBinaryExpr(Binary e);
        R visitUnaryExpr(Unary e);
        R visitIntLitExpr(IntLit e);
        R visitBoolLitExpr(BoolLit e);
        R visitVarExpr(Var e);
        R visitCallExpr(Call e);
    }

    public enum UnOp {
        NEG, // -e
        NOT; // !e

        /** Unary operators bind tighter than all binary operators in this language. */
        public int precedence() { return 70; }

        public String toSource() {
            return this == NEG ? "-" : "!";
        }
    }

    /**
     * Binary operators in MadLang, ordered by descending precedence.
     *
     * Higher entries bind more tightly than lower ones.
     * Operators at the same precedence level are left-associative.
     *
     * Precedence (high → low):
     *   1. Multiplicative: *, /, %
     *   2. Additive:       +, -
     *   3. Comparison:     <, <=, >, >=, ==, !=
     *   4. Logical AND:    &&   (LAND)
     *   5. Logical OR:     ||   (LOR)
     *
     * This ordering matches C.
     */
    public enum BinOp {
        // Multiplicative
        MUL, DIV, MOD,
        // Additive
        ADD, SUB,
        // Relational
        LT, LE, GT, GE, EQ, NE,
        // Logical (short-circuit)
        LAND,
        LOR;

        /** Higher value = binds tighter (higher precedence). */
        public int precedence() {
            switch (this) {
                case MUL: case DIV: case MOD: return 60;
                case ADD: case SUB:           return 50;
                case LT: case LE: case GT: case GE: return 40;
                case EQ: case NE:             return 30;
                case LAND:                    return 20;
                case LOR:                     return 10;
                default: throw new IllegalStateException("unreachable");
            }
        }

        public String toSource() {
            switch (this) {
                case MUL:  return "*";
                case DIV:  return "/";
                case MOD:  return "%";
                case ADD:  return "+";
                case SUB:  return "-";
                case LT:   return "<";
                case LE:   return "<=";
                case GT:   return ">";
                case GE:   return ">=";
                case EQ:   return "==";
                case NE:   return "!=";
                case LAND: return "&&";
                case LOR:  return "||";
                default: throw new IllegalStateException("unreachable");
            }
        }
    }

    public static final class Binary extends Expr {
        public final Expr left;
        public final BinOp op;
        public final Expr right;

        public Binary(Expr left, BinOp op, Expr right) {
            this(left, op, right, null);
        }

        public Binary(Expr left, BinOp op, Expr right, Ast.Span span) {
            super(span);
            this.left = Objects.requireNonNull(left);
            this.op = Objects.requireNonNull(op);
            this.right = Objects.requireNonNull(right);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitBinaryExpr(this); }
    }

    public static final class Unary extends Expr {
        public final UnOp op;
        public final Expr expr;

        public Unary(UnOp op, Expr expr) {
            this(op, expr, null);
        }

        public Unary(UnOp op, Expr expr, Ast.Span span) {
            super(span);
            this.op = Objects.requireNonNull(op);
            this.expr = Objects.requireNonNull(expr);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitUnaryExpr(this); }
    }

    public static final class IntLit extends Expr {
        public final int value;

        public IntLit(int value) {
            this(value, null);
        }

        public IntLit(int value, Ast.Span span) {
            super(span);
            this.value = value;
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitIntLitExpr(this); }
    }

    public static final class BoolLit extends Expr {
        public final boolean value;

        public BoolLit(boolean value) {
            this(value, null);
        }

        public BoolLit(boolean value, Ast.Span span) {
            super(span);
            this.value = value;
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitBoolLitExpr(this); }
    }

    public static final class Var extends Expr {
        public final String name;

        public Var(String name) {
            this(name, null);
        }

        public Var(String name, Ast.Span span) {
            super(span);
            this.name = Objects.requireNonNull(name);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitVarExpr(this); }
    }

    /**
     * Function call: f(e1, ..., en)
     * callee is a function identifier, not an expression.
     */
    public static final class Call extends Expr {
        public final String callee;
        public final List<Expr> args;

        public Call(String callee, List<Expr> args) {
            this(callee, args, null);
        }

        public Call(String callee, List<Expr> args, Ast.Span span) {
            super(span);
            this.callee = Objects.requireNonNull(callee);
            this.args = unmodifiableCopy(args);
        }

        @Override public <R> R accept(Visitor<R> v) { return v.visitCallExpr(this); }
    }

    public abstract <R> R accept(Visitor<R> visitor);

    private static <T> List<T> unmodifiableCopy(List<T> xs) {
        Objects.requireNonNull(xs);
        return Collections.unmodifiableList(new ArrayList<T>(xs));
    }
}
