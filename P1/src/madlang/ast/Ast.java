package madlang.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Core AST definitions that are shared across Expr/Stmt:
 * - Span (optional source location)
 * - Types
 * - Program + top-level declarations (global vars, functions)
 */
public final class Ast {
    private Ast() {}

    /** Optional source location for diagnostics. May be null. */
    public static final class Span {
        public final int line;
        public final int col;

        public Span(int line, int col) {
            this.line = line;
            this.col = col;
        }
    }

    public enum Type {
        INT, BOOL;

        public String toSource() {
            return this == INT ? "int" : "bool";
        }
    }

    public static final class Program {
        public final List<Decl> decls;
        public final Span span; // may be null

        public Program(List<Decl> decls) {
            this(decls, null);
        }

        public Program(List<Decl> decls, Span span) {
            this.decls = unmodifiableCopy(decls);
            this.span = span;
        }
    }

    /** Base class for top-level declarations. */
    public static abstract class Decl {
        public final Span span; // may be null
        protected Decl(Span span) { this.span = span; }
    }

    /**
     * Global variable declaration:
     *   x: int;
     *   x: int = expr;
     *
     * initOrNull == null means uninitialized declaration.
     */
    public static final class GlobalVarDecl extends Decl {
        public final String name;
        public final Type type;
        public final Expr initOrNull; // may be null

        public GlobalVarDecl(String name, Type type, Expr initOrNull) {
            this(name, type, initOrNull, null);
        }

        public GlobalVarDecl(String name, Type type, Expr initOrNull, Span span) {
            super(span);
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.initOrNull = initOrNull;
        }
    }

    public static final class Param {
        public final String name;
        public final Type type;
        public final Span span; // may be null

        public Param(String name, Type type) {
            this(name, type, null);
        }

        public Param(String name, Type type, Span span) {
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.span = span;
        }
    }

    /**
     * Function definition (top-level or nested).
     * body is a Stmt.Block.
     */
    public static final class FunDecl extends Decl {
        public final String name;
        public final List<Param> params;
        public final Type returnType;
        public final Stmt.Block body;

        public FunDecl(String name, List<Param> params, Type returnType, Stmt.Block body) {
            this(name, params, returnType, body, null);
        }

        public FunDecl(String name, List<Param> params, Type returnType, Stmt.Block body, Span span) {
            super(span);
            this.name = Objects.requireNonNull(name);
            this.params = unmodifiableCopy(params);
            this.returnType = Objects.requireNonNull(returnType);
            this.body = Objects.requireNonNull(body);
        }
    }

    static <T> List<T> unmodifiableCopy(List<T> xs) {
        Objects.requireNonNull(xs);
        return Collections.unmodifiableList(new ArrayList<T>(xs));
    }
}
