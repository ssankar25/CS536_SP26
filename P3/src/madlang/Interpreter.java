package madlang;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

  // TODO: Feel free to add additional instance variables to the class

  Interpreter() {
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    // TODO: Implement function declaration
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    // TODO: Implement if statement execution
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    // TODO: Implement return statement
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    // TODO: Implement block execution
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    // TODO: Implement variable declaration
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    // TODO: Implement while loop
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    // TODO: Implement expression statement
    return null;
  }

  @Override
  public Void visitAssignStmt(Stmt.Assign stmt) {
    // TODO: Implement assignment statement
    return null;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    // TODO: Implement binary expression evaluation
    // Evaluate left and right operands, then apply the operator
    // Handle arithmetic (+, -, *, /), comparison (==, !=, <, <=, >, >=), and logical (&&, ||) operators
    return null; // Replace this with your implementation
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    // TODO: Implement literal expression evaluation
    return null; // Replace this with your implementation
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    // TODO: Implement unary expression evaluation
    // Evaluate the right operand and apply the unary operator (!, -)
    return null; // Replace this with your implementation
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    // TODO: Implement variable expression evaluation
    return null; // Replace this with your implementation
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    // TODO: Implement function call evaluation
    return null; // Replace this with your implementation
  }

}
