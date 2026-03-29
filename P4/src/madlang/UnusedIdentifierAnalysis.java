package madlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// ************
// BONUS ONLY!! Not required for 100% completion
// In this section, you will build a static analyzer that reports warnings for variable and/or function declarations that are not used
// Refer to the README for more details
// ************

// Do not throw any exceptions in the visitor methods
class UnusedIdentifierAnalysis implements Expr.Visitor<Void, RuntimeException>, Stmt.Visitor<Void, RuntimeException> {

  /**
   * Class for storing declaration information for an identifier.
   */
  static final class DeclInfo {
    final String name;
    boolean isUsed;

    DeclInfo(String name) {
      this.name = name;
      this.isUsed = false;
    }
  }

  /**
   * Class for storing identifier declarations in nested lexical scopes.
   */
  static final class Scope {
    private final Map<String, DeclInfo> values = new HashMap<>();
    private final Scope parent;

    Scope() {
      this(null);
    }

    Scope(Scope parent) {
      this.parent = parent;
    }

    /**
     * Lookup the identifier declaration in the current scope chain.
     * 
     * @param name The identifier to lookup.
     * @return The corresponding declaration info, or null if not found.
     */
    DeclInfo lookup(String name) {

      // First check the current scope for the name (can be shadowed)
      if (values.containsKey(name)) return values.get(name);

      // Check the parent scope for the name, if it exists
      if (parent != null) return parent.lookup(name);

      return null;
    }

    /**
     * Add a declaration to the current scope.
     * 
     * @param name The identifier name.
     * @param declInfo The declaration info to store.
     */
    void define(String name, DeclInfo declInfo) {
      values.put(name, declInfo);
    }
  }

  // Current scope for lexical name resolution
  private Scope currScope = new Scope();

  // Store all declarations so unused ones can be collected at the end
  private final List<DeclInfo> allDecls = new ArrayList<>();

  // Used to suppress direct self-recursive references from counting as "used"
  private DeclInfo currFunctionDecl = null;

  // Leave the constructor signature unchanged
  UnusedIdentifierAnalysis() {
  }

  /**
   * Helper method to analyze a list of statements in a new scope.
   * 
   * @param stmts The list of statements to analyze in a new scope.
   */
  private void analyzeInNewScope(List<Stmt> stmts) {
    // Save the old scope so it can be restored
    Scope savedScope = currScope;
    currScope = new Scope(savedScope);

    try {
      for (Stmt stmt : stmts) {
        stmt.accept(this);
      }
    } finally {
      currScope = savedScope;
    }
  }

  /**
   * Helper method to create and define a new declaration in the current scope.
   * 
   * @param name The identifier name to define.
   * @return The declaration info object created for this identifier.
   */
  private DeclInfo defineDecl(String name) {
    DeclInfo declInfo = new DeclInfo(name);
    currScope.define(name, declInfo);
    allDecls.add(declInfo);
    return declInfo;
  }

  /**
   * Helper method to record a syntactic reference to an identifier, if it resolves.
   * 
   * A direct self-reference to the current function does not count as usage
   * for the purposes of this bonus analysis.
   * 
   * @param name The identifier name being referenced.
   */
  private void recordReference(String name) {
    DeclInfo declInfo = currScope.lookup(name);

    // Ignore unbound references for this analysis
    if (declInfo == null) return;

    // Direct self-recursion / self-reference should not count as usage
    if (declInfo == currFunctionDecl) return;

    declInfo.isUsed = true;
  }

  // Keep the method signature unchanged
  // Given a madlang program, report a set of identifiers (strings) that are unused. This includes functions and variables
  public Set<String> unused(List<Stmt> stmts) {
    // Reset analysis state
    currScope = new Scope();
    allDecls.clear();
    currFunctionDecl = null;

    // Save the global scope so main can be specially handled after traversal
    Scope globalScope = currScope;

    for (Stmt stmt : stmts) {
      stmt.accept(this);
    }

    // The global main function, if it exists as the last top-level function, is always used
    if (!stmts.isEmpty()) {
      Stmt lastStmt = stmts.get(stmts.size() - 1);
      if (lastStmt instanceof Stmt.Function mainFunc && mainFunc.name.equals("main")) {
        DeclInfo mainDecl = globalScope.lookup("main");
        if (mainDecl != null) {
          mainDecl.isUsed = true;
        }
      }
    }

    // After analyzing the program for the unused declarations,
    // collect their names and return as a set
    Set<String> unusedIds = new HashSet<>();
    for (DeclInfo declInfo : allDecls) {
      if (!declInfo.isUsed) {
        unusedIds.add(declInfo.name);
      }
    }

    return unusedIds;
  }

  /// STATEMENT VISITORS ///

  /**
   * Visitor to analyze a function statement.
   * 
   * @param stmt The function statement to analyze.
   */
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    // Add the function declaration to the current scope
    DeclInfo funcDecl = defineDecl(stmt.name);

    // Save current scope and current function declaration
    Scope savedScope = currScope;
    DeclInfo savedFunctionDecl = currFunctionDecl;

    // Create new scope for function body
    currScope = new Scope(savedScope);
    currFunctionDecl = funcDecl;

    try {
      // Define parameters in the function body's scope
      for (Stmt.Parameter param : stmt.params) {
        defineDecl(param.name());
      }

      for (Stmt bodyStmt : stmt.body) {
        bodyStmt.accept(this);
      }
    } finally {
      // Restore scope and current function declaration
      currScope = savedScope;
      currFunctionDecl = savedFunctionDecl;
    }

    return null;
  }

  /**
   * Visitor to analyze an if statement.
   * 
   * @param stmt The if statement to analyze.
   */
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    stmt.condition.accept(this);

    // Analyze the then branch (check for block statement to prevent double scoping)
    if (stmt.thenBranch instanceof Stmt.Block) {
      stmt.thenBranch.accept(this);
    } else {
      analyzeInNewScope(List.of(stmt.thenBranch));
    }

    // Analyze else branch if it exists
    if (stmt.elseBranch != null) {
      // Check if else branch is a block statement to prevent double scoping
      if (stmt.elseBranch instanceof Stmt.Block) {
        stmt.elseBranch.accept(this);
      } else {
        analyzeInNewScope(List.of(stmt.elseBranch));
      }
    }

    return null;
  }

  /**
   * Visitor to analyze a return statement.
   * 
   * @param stmt The return statement to analyze.
   */
  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    stmt.value.accept(this);
    return null;
  }

  /**
   * Visitor to analyze a block statement.
   * 
   * @param stmt The block statement to analyze.
   */
  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    // Creating a new scope, so analyze the block in the new scope
    analyzeInNewScope(stmt.statements);
    return null;
  }

  /**
   * Visitor to analyze a variable declaration statement.
   * 
   * @param stmt The variable declaration statement to analyze.
   */
  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    if (stmt.initializer != null) {
      stmt.initializer.accept(this);
    }

    defineDecl(stmt.name);
    return null;
  }

  /**
   * Visitor to analyze a while statement.
   * 
   * @param stmt The while statement to analyze.
   */
  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    stmt.condition.accept(this);

    // Analyze the body of the while statement in a new scope,
    // which is already done when it is a block statement (prevent double scoping)
    if (stmt.body instanceof Stmt.Block) {
      stmt.body.accept(this);
    } else {
      analyzeInNewScope(List.of(stmt.body));
    }

    return null;
  }

  /**
   * Visitor to analyze an expression statement.
   * 
   * @param stmt The expression statement to analyze.
   */
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    stmt.expression.accept(this);
    return null;
  }

  /**
   * Visitor to analyze an assignment statement.
   * 
   * @param stmt The assignment statement to analyze.
   */
  @Override
  public Void visitAssignStmt(Stmt.Assign stmt) {
    // Assignment target is still a syntactic reference to the identifier
    recordReference(stmt.name);
    stmt.value.accept(this);
    return null;
  }

  /// EXPRESSION VISITORS ///

  /**
   * Visitor to analyze a binary expression.
   * 
   * @param expr The binary expression to analyze.
   */
  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    expr.left.accept(this);
    expr.right.accept(this);
    return null;
  }

  /**
   * Visitor to analyze a literal expression.
   * 
   * @param expr The literal expression to analyze.
   */
  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    // No analysis needed for literals since they do not contain references
    return null;
  }

  /**
   * Visitor to analyze a unary expression.
   * 
   * @param expr The unary expression to analyze.
   */
  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    expr.right.accept(this);
    return null;
  }

  /**
   * Visitor to analyze a variable expression.
   * 
   * @param expr The variable expression to analyze.
   */
  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    recordReference(expr.name);
    return null;
  }

  /**
   * Visitor to analyze a function call expression.
   * 
   * @param expr The function call expression to analyze.
   */
  @Override
  public Void visitCallExpr(Expr.Call expr) {
    // The called function name itself is a syntactic reference
    recordReference(expr.name);

    // Analyze arguments from left to right
    for (Expr arg : expr.arguments) {
      arg.accept(this);
    }

    return null;
  }
}