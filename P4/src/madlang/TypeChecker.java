package madlang;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

class TypeChecker implements Expr.Visitor<VarType, TypeError>, Stmt.Visitor<Void, TypeError> {

  // TODO: Feel free to add additional instance variables to the class

  // Do not change the constructor signature
  TypeChecker() {
  }

  public void visitProgram(List<Stmt> stmts) throws TypeError {
    // TODO: Implement
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public Void visitAssignStmt(Stmt.Assign stmt) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public VarType visitBinaryExpr(Expr.Binary expr) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public VarType visitLiteralExpr(Expr.Literal expr) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public VarType visitUnaryExpr(Expr.Unary expr) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public VarType visitVariableExpr(Expr.Variable expr) throws TypeError {
    // TODO: Implement
    return null;
  }

  @Override
  public VarType visitCallExpr(Expr.Call expr) throws TypeError {
    // TODO: Implement
    return null;
  }

}
