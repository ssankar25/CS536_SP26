package madlang;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interpreter class that evaluates the MadLang AST by implementing the
 * expression and statement visitors.
 */
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

  // Top-level global environment 
  private final Environment globals = new Environment();

  // Environment of current scope, which begins as the globals environment
  private Environment currEnv = globals;

  // Scanner object for builtin input/output functions
  private final Scanner scanner = new Scanner(System.in);

  /**
   * Interpreter constructor that initializes the global 
   * environment with the builtin input() and output() functions.
   */
  Interpreter() {
    // Built-in functions instantiated in global scope
    // Should not have any other duplicate symbols on instantiation
    globals.defineFunction("input", new BuiltinInputFunction());
    globals.defineFunction("output", new BuiltinOutputFunction());
  }

  /**
   * Lightweight exception is thrown upon returning from a function
   * to easily unwind the call stack
   */
  private static final class ReturnSignal extends RuntimeException {
    final Object value;

    /**
     * ReturnSignal constructor to be a lightweight exception that
     * returns the passed in value.
     * 
     * @param value The return value.
     */
    ReturnSignal(Object value) {
      // Initialize ReturnSignal exception to not capture
      // the call stack, but to just unwind the stack to the point
      // where the exception is caught
      super(null, null, false, false);
      this.value = value;
    }
  }

  /**
   * Interface to define what a callable function object looks like,
   * which is stored in the environment for each function reference.
   * 
   * Each function has an accessible return type and parameters, along
   * with a call function for its implementation.
   */
  private interface CallableFunction {
    VarType getReturnType();
    List<Stmt.Parameter> getParams(); // Empty list when there are no parameters
    Object call(List<Object> args);
  }

  /**
   * Callable function class that implements the builtin input function
   * to take in user input from stdin.
   */
  private final class BuiltinInputFunction implements CallableFunction {
    final VarType returnType;
    final List<Stmt.Parameter> params;

    /**
     * Default input function constructor.
     * 
     * Sets return type to INT and parameter list to be empty.
     */
    BuiltinInputFunction() {
      this.returnType = VarType.INT;
      this.params = List.of();
    }

    /**
     * Getter method for the function return type.
     * 
     * @return The function's return type.
     */
    @Override
    public VarType getReturnType() {
      return this.returnType;
    }

    /**
     * Getter method for the function parameters.
     * 
     * @return The function's parameter list.
     */
    @Override
    public List<Stmt.Parameter> getParams() {
      return this.params;
    }

    /**
     * Implements the taking in of stdin.
     * 
     * @param args List of arguments that is expected to be empty for the input() function.
     * @return The integer input from stdin.
     */
    @Override
    public Object call(List<Object> args) {
      // Check for no input arguments
      if (!args.isEmpty()) typeMismatch();

      // Get next int from stdin (throws exception if invalid)
      return scanner.nextInt();
    }
  }

  /**
   * Callable function class that implements the builtin output function
   * to output the single argument to stdout.
   */
  private final class BuiltinOutputFunction implements CallableFunction {
    final VarType returnType;
    final List<Stmt.Parameter> params;

    /**
     * Default output function constructor.
     * 
     * Sets return type to INT and the parameter list as "x: int".
     */
    BuiltinOutputFunction() {
      this.returnType = VarType.INT;
      this.params = List.of(new Stmt.Parameter("x", VarType.INT));
    }

    /**
     * Getter method for the function return type.
     * 
     * @return The function's return type.
     */
    @Override
    public VarType getReturnType() {
      return this.returnType;
    }

    /**
     * Getter method for the function parameters.
     * 
     * @return The function's parameter list.
     */
    @Override
    public List<Stmt.Parameter> getParams() {
      return this.params;
    }

    /**
     * Implements the outputting of the argument to stdout.
     * 
     * @param args List of arguments that should just contain the output argument.
     * @return 0 on success. Ignored by programs.
     */
    @Override
    public Object call(List<Object> args) {
      // Check for exactly 1 input argument
      if (args.size() != 1) typeMismatch();

      // Print out argument
      int x = asInt(args.get(0));
      System.out.println(x);

      // return value is ignored by programs
      return 0;
    }
  }

  /**
   * Generic callable user function that is stored
   * in the environment hashmap.
   */
  private final class UserFunction implements CallableFunction {
    final VarType returnType;
    final List<Stmt.Parameter> params;
    final List<Stmt> body;
    final Environment definedEnv; // Stores environment context at definition time

    /**
     * UserFunction constructor that sets the fields.
     * 
     * @param returnType The return type of the function.
     * @param params The parameter list of the function, which is empty if there are none.
     * @param body The body of the function.
     * @param definedEnv The current environment of the program at definition time used for lexical scoping.
     */
    UserFunction(VarType returnType, List<Stmt.Parameter> params, List<Stmt> body, Environment definedEnv) {
      this.returnType = returnType;
      this.params = (params == null) ? List.of() : params; // Empty list if no parameters passed in
      this.body = body;
      this.definedEnv = definedEnv;
    }

    /**
     * Getter method for the function parameters.
     * 
     * @return The function's parameter list.
     */
    @Override
    public List<Stmt.Parameter> getParams() {
      return this.params;
    }

    /**
     * Getter method for the function return type.
     * 
     * @return The function's return type.
     */
    @Override
    public VarType getReturnType() {
      return this.returnType;
    }

    /**
     * Calls the function by executing each of the statements
     * in the function body.
     * 
     * @param args The list of arguments that is loaded into the function's environment before executing.
     * @return The function return value, or the default value for the function return type when there is no return statement.
     */
    @Override
    public Object call(List<Object> args) {

      // Check if number of arguments match
      if (args.size() != params.size()) typeMismatch();

      // Check if each of the arguments match the parameter type
      for (int i = 0; i < args.size(); i++) {
        checkTypeMatch(params.get(i).type(), args.get(i));
      }

      // Save the previous environment
      Environment prev = currEnv;

      // Update the current environment to be one that
      // extends the function's environment as set at
      // definition time (lexical scoping)
      currEnv = new Environment(definedEnv);

      try {
        // Add parameters to the current environment
        for (int i = 0; i < params.size(); i++) {
          Stmt.Parameter p = params.get(i);
          currEnv.defineVar(p.name(), p.type(), args.get(i));
        }

        // Execute the body statements
        for (Stmt s : body) execute(s);

      } catch(ReturnSignal r) {
        // When returning from a function, the call stack is 
        // unwound and the value is returned
        checkTypeMatch(returnType, r.value);
        return r.value;

      } finally {
        // Reset the environment
        currEnv = prev;
      }

      // Return default return type value when
      // no return statement is executed
      return defaultValue(returnType);
    }
  }

  /**
   * Helper method to execute top-level declarations and calling main if it exists.
   * 
   * @param program The program, or list of statements, to be executed.
   */
  void interpret(List<Stmt> program) {
    // Execute the program statements
    for (Stmt s : program) {
      execute(s);
    }

    // Call the main function if it exists
    Environment.Entry mainEntry = globals.lookup("main");
    if (mainEntry == null) return;

    if (!mainEntry.isFunction()) typeMismatch();

    CallableFunction mainFunc = (CallableFunction) mainEntry.value;

    // Check that main's return type is int
    if (mainFunc.getReturnType() != VarType.INT) typeMismatch();

    // Check number of main params is 0
    List<Stmt.Parameter> mainParams = mainFunc.getParams();
    if (!mainParams.isEmpty()) typeMismatch();

    mainFunc.call(List.of());
  }

  /**
   * Evaluate wrapper for expression types to call
   * the corresponding expression acceptor.
   * 
   * @param expr The expression to evaluate.
   * @return The evaluated expression.
   */
  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  /**
   * Execute wrapper for statement types to call
   * the corresponding statement acceptor.
   * 
   * @param stmt The statement to execute.
   */
  private void execute(Stmt stmt) {
    // Null check is done since statements can be null
    // like the elseBranch of an ifStmt
    if (stmt != null) stmt.accept(this);
  }

  /**
   * Helper method to throw type mismatch exception.
   */
  private void typeMismatch() {
    throw new IllegalStateException("Error: type mismatch");
  }

  /**
   * Helper method to throw unbound reference exception.
   */
  private void unboundReference(String name) {
    throw new IllegalStateException("Error: unbound reference - " + name);
  }

  /**
   * Helper method to throw arithmetic error exception.
   */
  private void arithmeticError() {
    throw new IllegalStateException("Error: arithmetic error");
  }

  /**
   * Helper method to convert an object to an integer. Throws a type mismatch
   * exception if the object is not an integer.
   * 
   * @param obj The object to convert.
   * @return The integer if the conversion is successful as an unboxed primitive. 
   */
  private int asInt(Object obj) {
    if (!(obj instanceof Integer)) typeMismatch();
    return (Integer) obj;
  }

  /**
   * Helper method to convert an object to a boolean. Throws a type mismatch
   * exception if the object is not an boolean.
   * 
   * @param obj The object to convert.
   * @return The boolean if the conversion is successful as an unboxed primitive. 
   */
  private boolean asBool(Object obj) {
    if (!(obj instanceof Boolean)) typeMismatch();
    return (Boolean) obj;
  }

  /**
   * Helper method to return the default value of the specified VarType.
   * 
   * @param type The VarType to return the default value of.
   * @return 0 if type is an INT and false if it is a BOOL.
   */
  private Object defaultValue(VarType type) {
    return (type == VarType.INT) ? 0 : false;
  }

  /**
   * Helper method to check if the type of the object
   * passed in matches the specified VarType. Throws a type
   * mismatch exception if not.
   * 
   * @param type The VarType to check val against.
   * @param val The object to check the VarType of.
   */
  private void checkTypeMatch(VarType type, Object val) {
    if (type == VarType.INT && !(val instanceof Integer)) typeMismatch();
    if (type == VarType.BOOL && !(val instanceof Boolean)) typeMismatch();
  }

  /// STATEMENT VISITORS ///
  
  /**
   * Executes a function statement.
   * 
   * @param stmt The function statement to execute.
   */
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    // Create callable function object and save the environment at definition time
    CallableFunction func = new UserFunction(stmt.returnType, stmt.params, stmt.body, currEnv);

    // Add the function to the environment
    currEnv.defineFunction(stmt.name, func);

    return null;
  }

  /**
   * Executes an if statement.
   * 
   * @param stmt The if statement to execute.
   */
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    // Implement if statement execution
    boolean cond = asBool(evaluate(stmt.condition));
    if (cond) {
      execute(stmt.thenBranch);
    }
    else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }

    return null;
  }

  /**
   * Executes a return statement.
   * 
   * @param stmt The return statement to execute.
   */
  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {

    // Grab the return value
    Object val = evaluate(stmt.value);

    // Throw the custom lightweight exception
    // to unwind the call stack
    throw new ReturnSignal(val);
  }

  /**
   * Executes a block statement.
   * 
   * @param stmt The block statement to execute.
   */
  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {

    // Save the current environment
    Environment prev = currEnv;

    // Update current environment pointer
    currEnv = new Environment(prev);

    // Execute the statements within the block
    // and restore the previous environment when done
    try {
      for (Stmt s : stmt.statements) execute(s);
    } finally {
      currEnv = prev;
    }

    return null;
  }

  /**
   * Executes a var statement.
   * 
   * @param stmt The var statement to execute.
   */
  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    // Check for initializer, and if there isn't one, then
    // assign the variable its default value
    Object val = null;
    if (stmt.initializer != null) {
      val = evaluate(stmt.initializer);
      // Make sure the variable's and initializer's types match
      checkTypeMatch(stmt.type, val);
    }
    else {
      val = defaultValue(stmt.type);
    }

    // Add the variable definition to the hashmap
    currEnv.defineVar(stmt.name, stmt.type, val);

    return null;
  }

  /**
   * Executes a while statement.
   * 
   * @param stmt The while statement to execute.
   */
  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (asBool(evaluate(stmt.condition))) {
      execute(stmt.body);
    }

    return null;
  }

  /**
   * Executes an expression statement.
   * 
   * @param stmt The expression statement to execute.
   */
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  /**
   * Executes an assign statement.
   * 
   * @param stmt The assign statement to execute.
   */
  @Override
  public Void visitAssignStmt(Stmt.Assign stmt) {

    Object val = evaluate(stmt.value);

    // Extract entry's value after checking the entry
    // exists and is a valid type to assign to (not a function)
    Environment.Entry entry = currEnv.lookup(stmt.name);
    if (entry == null) unboundReference(stmt.name);
    if (entry.isFunction()) typeMismatch();

    // Check if the variable and value's types match
    checkTypeMatch(entry.type, val);

    // Try the assignment (throws unboundReference error if variable doesn't exist in environment)
    currEnv.assign(stmt.name, val);

    return null;
  }

  /// EXPRESSION VISITORS ///
  
  /**
   * Evaluates a binary expression.
   * 
   * @param expr The binary expression to evaluate.
   * @return The result of the expression if there is no exception. 
   */
  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    // Evaluate left and right operands, then apply the operator
    // Handle arithmetic (+, -, *, /), comparison (==, !=, <, <=, >, >=), and logical (&&, ||) operators

    Operator operator = expr.operator;

    // Short-circuit AND evaluation
    if (operator == Operator.AND) {
      // Check if the left side is false and short-circuit return if so
      boolean left = asBool(evaluate(expr.left));
      if (!left) return false;
      
      // Check for expr.right: true && expr.right = expr.right
      return asBool(evaluate(expr.right));
    }

    // Short-circuit OR evaluation
    if (operator == Operator.OR) {
      // Check if left side is true and short-circuit return if so
      boolean left = asBool(evaluate(expr.left));
      if (left) return true;

      // Check for expr.right: false || expr.right = expr.right
      return asBool(evaluate(expr.right));
    }


    // Non-short-circuit evaluation (evaluate both sides)
    Object leftObject = evaluate(expr.left);
    Object rightObject = evaluate(expr.right);

    switch (operator) {
      // Arithmetic evaluation
      case PLUS: return asInt(leftObject) + asInt(rightObject);
      case MINUS: return asInt(leftObject) - asInt(rightObject);
      case MULTIPLY: return asInt(leftObject) * asInt(rightObject);
      case DIVIDE: {
        // Check for divide by 0
        int rightVal = asInt(rightObject);
        if (rightVal == 0) arithmeticError();
        return asInt(leftObject) / rightVal;
      }
      case MODULO: {
        // Check for divide by 0
        int rightVal = asInt(rightObject);
        if (rightVal == 0) arithmeticError();
        return asInt(leftObject) % rightVal;
      }

      // Comparison evaluation - Only integers as per MadLang spec
      case EQUAL: return asInt(leftObject) == asInt(rightObject);
      case NOT_EQUAL: return asInt(leftObject) != asInt(rightObject);
      case LESS: return asInt(leftObject) < asInt(rightObject);
      case LESS_EQUAL: return asInt(leftObject) <= asInt(rightObject);
      case GREATER: return asInt(leftObject) > asInt(rightObject);
      case GREATER_EQUAL: return asInt(leftObject) >= asInt(rightObject);

      // None of above cases: error
      default: {
        typeMismatch();
        return null;
      }
    } 
  }

  /**
   * Evaluates a literal expression.
   * 
   * @param expr The literal expression to evaluate.
   * @return The result of the expression. 
   */
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  /**
   * Evaluates a unary expression.
   * 
   * @param expr The unary expression to evaluate.
   * @return The result of the expression if there is no exception. 
   */
  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    // Evaluate the right operand and apply the unary operator (!, -)
    switch (expr.operator) {
      case MINUS: {
        int val = asInt(evaluate(expr.right));
        return -val;
      }
      case NOT: {
        boolean val = asBool(evaluate(expr.right));
        return !val;
      }
      default: {
        typeMismatch();
        return null;
      }
    }
  }

  /**
   * Evaluates a variable expression.
   * 
   * @param expr The variable expression to evaluate.
   * @return The result of the expression if there is no exception. 
   */
  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    // Get the variable value from the environment
    // and check if it's valid
    Environment.Entry varEntry = currEnv.lookup(expr.name);

    if (varEntry == null) unboundReference(expr.name);
    // Should not be using a function as a variable
    if (varEntry.isFunction()) typeMismatch(); 

    return varEntry.value;
  }

  /**
   * Evaluates a call expression.
   * 
   * @param expr The call expression to evaluate.
   * @return The result of the expression if there is no exception. 
   */
  @Override
  public Object visitCallExpr(Expr.Call expr) {

    // Check if the function is in the environment
    Environment.Entry funcEntry = currEnv.lookup(expr.name);
    if (funcEntry == null) unboundReference(expr.name);
    // Check if the entry is a valid function type
    if (!funcEntry.isFunction()) typeMismatch();

    CallableFunction func = (CallableFunction) funcEntry.value;

    // Store the argument values
    List<Object> args = new ArrayList<>();
    for (Expr arg : expr.arguments) args.add(evaluate(arg));

    // Argument number and type checking done in call()
    return func.call(args);
  }

}
