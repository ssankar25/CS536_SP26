package madlang;

import java.util.ArrayList;
import java.util.List;

class TypeChecker implements Expr.Visitor<VarType, TypeError>, Stmt.Visitor<Void, TypeError> {

  private Context currContext = new Context();

  // Used to save the function's return type to eventually check
  // against the return statement type
  private VarType currReturnType = null;

  // Do not change the constructor signature
  TypeChecker() {
  }

  /**
   * Helper method to check if the types of an actual and
   * expected value match.
   * 
   * @param expected The expected VarType to check actual against.
   * @param actual The VarType of the actual value.
   */
  private void checkTypeMatch(VarType expected, VarType actual) throws MismatchedTypes {
    if (actual != expected) {
      throw new MismatchedTypes(expected, actual);
    }
  }

  /**
   * Helper method to type check a list of statements in a new scope.
   * 
   * @param stmts The list of statements to type check in a new scope.
   */
  private void typeCheckInNewScope(List<Stmt> stmts) throws TypeError {
    // Save the old context so it can be restored
    Context savedContext = currContext;
    currContext = new Context(savedContext);

    try {
      for (Stmt stmt : stmts) {
        // Must type check in the new context to handle variable
        // declarations vs. shadowing
        stmt.accept(this);
      }
    } finally {
      currContext = savedContext;
    }
  }

  /**
   * Helper method to make sure that a global variable initializer
   * only contains literals as per the spec.
   * 
   * @param expr The expression to check as a global variable initializer.
   * @throws TypeError If the expression is not a valid global variable initializer.
   */
  private void checkGlobalInitializer(Expr expr) throws TypeError {

    // Base case: Literals are valid global initializers
    if (expr instanceof Expr.Literal) {
      return;
    }

    // Base cases: Intializer cannot contain a variable reference or function call as per the spec
    if (expr instanceof Expr.Variable || expr instanceof Expr.Call) {
      throw new IllegalGlobalInitializer();
    }

    // Recursive cases: Unary and binary expressions are valid as long as they only contain literals

    if (expr instanceof Expr.Unary unaryExpr) {
      checkGlobalInitializer(unaryExpr.right);
      return;
    }

    if (expr instanceof Expr.Binary binaryExpr) {
      checkGlobalInitializer(binaryExpr.left);
      checkGlobalInitializer(binaryExpr.right);
      return;
    }

    // Throw error by default
    throw new IllegalGlobalInitializer();
  }

  /**
   * Type check a list of statements in the global scope. This is the entry point for type checking a program.
   * 
   * @param stmts The list of statements in the program to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  public void visitProgram(List<Stmt> stmts) throws TypeError {
    // Reset context and return type
    currContext = new Context();
    currReturnType = null;

    // Instantiate builtin functions
    currContext.defineFunction("input", new Context.FunctionTypeInfo(List.of(), VarType.INT));
    currContext.defineFunction("output", new Context.FunctionTypeInfo(List.of(VarType.INT), VarType.INT));

    for (Stmt stmt : stmts) {
      // Check global variable statements have valid initializers
      if (stmt instanceof Stmt.Var varStmt && varStmt.initializer != null) {
        checkGlobalInitializer(varStmt.initializer);
      }

      stmt.accept(this);
    }
  }

  /// STATEMENT VISITORS: Return null if the statement is well-typed. ///

  /**
   * Visitor to type check a function statement.
   * 
   * @param stmt The function statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) throws TypeError {
    // Get the parameter types
    List<VarType> paramTypes = new ArrayList<>();
    for (Stmt.Parameter param : stmt.params) {
      paramTypes.add(param.type());
    }

    // Add the function to the current context
    currContext.defineFunction(stmt.name, new Context.FunctionTypeInfo(paramTypes, stmt.returnType));

    // Save current context and return type
    Context savedContext = currContext;
    VarType savedReturnType = currReturnType;

    // Create new context for function body
    currContext = new Context(savedContext);
    currReturnType = stmt.returnType;

    // Type check function body in new context
    try {
      // Define parameters in the function body's context
      for (Stmt.Parameter param : stmt.params) {
        currContext.defineVar(param.name(), param.type());
      }

      for (Stmt bodyStmt : stmt.body) {
        bodyStmt.accept(this);
      }
    } finally {
      // Restore context and return type
      currContext = savedContext;
      currReturnType = savedReturnType;
    }

    return null;
  }

  /**
   * Visitor to type check an if statement.
   * 
   * @param stmt The if statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitIfStmt(Stmt.If stmt) throws TypeError {
    VarType condType = stmt.condition.accept(this);
    // ERROR CASE: Condition of if statement is not BOOL
    checkTypeMatch(VarType.BOOL, condType);

    // Type check the then branch (check for block statement to prevent double scoping)
    if (stmt.thenBranch instanceof Stmt.Block) {
      stmt.thenBranch.accept(this);
    } else {
      typeCheckInNewScope(List.of(stmt.thenBranch));
    }

    // Type check else branch if it exists
    if (stmt.elseBranch != null) {
      // Check if else branch is a block statement to prevent double scoping
      if (stmt.elseBranch instanceof Stmt.Block) {
        stmt.elseBranch.accept(this);
      } else {
        typeCheckInNewScope(List.of(stmt.elseBranch));
      }
    }

    return null;
  }

  /**
   * Visitor to type check a return statement.
   * 
   * @param stmt The return statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitReturnStmt(Stmt.Return stmt) throws TypeError {
    if (currReturnType == null) {
      // ERROR CASE: Return statement not within a function
      throw new IllegalReturn();
    }

    // Get return statement type
    VarType returnType = stmt.value.accept(this);

    // ERROR CASE: Return statement type does not match the function's declared return type
    checkTypeMatch(currReturnType, returnType);
    return null;
  }

  /**
   * Visitor to type check a block statement.
   * 
   * @param stmt The block statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitBlockStmt(Stmt.Block stmt) throws TypeError {
    // Creating a new scope, so type check the block in the new context
    typeCheckInNewScope(stmt.statements);
    return null;
  }

  /**
   * Visitor to type check a variable declaration statement.
   * 
   * @param stmt The variable declaration statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitVarStmt(Stmt.Var stmt) throws TypeError {
    if (stmt.initializer != null) {
      // ERROR CASE: Type of the initializer does not match the variable's declared type
      checkTypeMatch(stmt.type, stmt.initializer.accept(this));
    }

    currContext.defineVar(stmt.name, stmt.type);
    return null;
  }

  /**
   * Visitor to type check a while statement.
   * 
   * @param stmt The while statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitWhileStmt(Stmt.While stmt) throws TypeError {
    VarType condType = stmt.condition.accept(this);
    // ERROR CASE: Condition of while statement is not BOOL
    checkTypeMatch(VarType.BOOL, condType);

    // Type check the body of the while statement in a new scope,
    // which is already done when it is a block statement (prevent double scoping)
    if (stmt.body instanceof Stmt.Block) {
      stmt.body.accept(this);
    } else {
      typeCheckInNewScope(List.of(stmt.body));
    }

    return null;
  }

  /**
   * Visitor to type check an expression statement.
   * 
   * @param stmt The expression statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) throws TypeError {
    // Do type checks on the expression statement
    stmt.expression.accept(this);
    return null;
  }

  /**
   * Visitor to type check an assignment statement.
   * 
   * @param stmt The assignment statement to type check.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public Void visitAssignStmt(Stmt.Assign stmt) throws TypeError {
    // Get the variable's entry in the context map
    Context.Entry varEntry = currContext.lookup(stmt.name);

    if (varEntry == null) {
      // ERROR CASE: Variable not defined, so it is an unbound reference
      throw new UnboundReference(stmt.name);
    }

    if (varEntry.isFunction()) {
      // ERROR CASE: Variable is indicated as a function in the context map,
      // which means it is being incorrectly assigned to
      throw new IllegalVarApplication(stmt.name);
    }

    VarType declType = varEntry.varType;
    VarType valueType = stmt.value.accept(this); 
    // ERROR CASE: Type of the value being assigned does not match the variable's declared type
    checkTypeMatch(declType, valueType);

    return null;
  }

  /// EXPRESSION VISITORS: Return the type of the expression if it is a valid type. ///
  
  /**
   *  Visitor to type check and return the type of a binary expression.
   * 
   * @param expr The binary expression to type check.
   * @return The type of the binary expression if it is well-typed.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public VarType visitBinaryExpr(Expr.Binary expr) throws TypeError {
    // Get the types of the left and right expressions
    VarType leftType = expr.left.accept(this);
    VarType rightType = expr.right.accept(this);

    // Check the expression types match with the operator
    switch(expr.operator) {
      // Arithmetic operators
      case PLUS, MINUS, MULTIPLY, DIVIDE, MODULO -> {
        // ERROR CASE: Arithmetic operator, but one or more non-INT expressions
        checkTypeMatch(VarType.INT, leftType);
        checkTypeMatch(VarType.INT, rightType);
        return VarType.INT;
      }

      // Comparison operators
      case EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL -> {
        // ERROR CASE: Comparison operator, but one or more non-INT expressions
        checkTypeMatch(VarType.INT, leftType);
        checkTypeMatch(VarType.INT, rightType);
        return VarType.BOOL;
      }

      // Logical operators
      case AND, OR -> {
        // ERROR CASE: Logical operator, but one or more non-BOOL expressions
        checkTypeMatch(VarType.BOOL, leftType);
        checkTypeMatch(VarType.BOOL, rightType);
        return VarType.BOOL;
      }

      default -> 
        // ERROR CASE: Unsupported operator
        throw new InvalidOperator(expr.operator.toString());
    }
  }

  /**
   * Visitor to type check and return the type of a literal expression.
   * 
   * @param expr The literal expression to type check.
   * @return The type of the literal expression if it is well-typed.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public VarType visitLiteralExpr(Expr.Literal expr) throws TypeError {
    // Check if the expression value is an INT or BOOL
    if (expr.value instanceof Integer) return VarType.INT;
    else if (expr.value instanceof Boolean) return VarType.BOOL;
    else {
      // ERROR CASE: Literal must be of type INT or BOOL
      throw new InvalidLiteral();
    }
  }

  /**
   * Visitor to type check and return the type of a unary expression.
   * 
   * @param expr The unary expression to type check.
   * @return The type of the unary expression if it is well-typed.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public VarType visitUnaryExpr(Expr.Unary expr) throws TypeError {
    // Get the type of the right side expression
    VarType rightType = expr.right.accept(this);

    // Check the types logically match with the unary operator
    switch(expr.operator) {
      case MINUS -> {
        // ERROR CASE: (-) operator, but right expression is not INT
        checkTypeMatch(VarType.INT, rightType);
        return VarType.INT;
      }

      case NOT -> {
        // ERROR CASE: (!) operator, but right expression is not BOOL
        checkTypeMatch(VarType.BOOL, rightType);
        return VarType.BOOL;
      }

      default ->
        // ERROR CASE: Unsupported operator
        throw new InvalidOperator(expr.operator.toString());
    }
  }

  /**
   * Visitor to type check and return the type of a variable expression.
   * 
   * @param expr The variable expression to type check.
   * @return The type of the variable expression if it is well-typed.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public VarType visitVariableExpr(Expr.Variable expr) throws TypeError {
    // Get the variable's entry in the context map
    Context.Entry varEntry = currContext.lookup(expr.name);

    // Check if the variable is defined in the scope chain
    if (varEntry == null) {
      // ERROR CASE: Variable not defined, so it is an unbound reference
      throw new UnboundReference(expr.name);
    }

    if (varEntry.isFunction()) {
      // ERROR CASE: Variable is indicated as a function in the context map,
      // which means it is being incorrectly used as a value
      throw new IllegalVarApplication(expr.name);
    }

    return varEntry.varType;
  }

  /**
   * Visitor to type check and return the type of a function call expression.
   * 
   * @param expr The function call expression to type check.
   * @return The type of the function call expression if it is well-typed.
   * @throws TypeError If a type error is encountered during type checking.
   */
  @Override
  public VarType visitCallExpr(Expr.Call expr) throws TypeError {
    // Lookup the function's entry in the context map
    Context.Entry funcEntry = currContext.lookup(expr.name);

    // Check that it is defined as a function in the scope chain
    if (funcEntry == null) {
      // ERROR CASE: Function not defined, so it is an unbound reference
      throw new UnboundReference(expr.name);
    }

    if (!funcEntry.isFunction()) {
      // ERROR CASE: Function not defined as one in the context map
      throw new IllegalApplication(expr.name);
    }

    Context.FunctionTypeInfo funcTypeInfo = funcEntry.funcType;
    if (funcTypeInfo.paramTypes.size() != expr.arguments.size()) {
      // ERROR CASE: Wrong number of arguments provided in the function call
      throw new WrongArgumentCount(expr.name, funcTypeInfo.paramTypes.size(), expr.arguments.size());
    }

    for (int i = 0; i < expr.arguments.size(); i++) {
      VarType argType = expr.arguments.get(i).accept(this);
      VarType paramType = funcTypeInfo.paramTypes.get(i);
      // ERROR CASE: Argument type does not match parameter type
      checkTypeMatch(paramType, argType);
    }

    // Type of the function is the function's return type
    return funcTypeInfo.returnType;
  }

}
