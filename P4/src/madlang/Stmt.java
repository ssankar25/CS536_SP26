package madlang;
import java.util.List;

abstract class Stmt {
  interface Visitor<R, E extends Throwable> {
    R visitBlockStmt(Block stmt) throws E;
    R visitExpressionStmt(Expression stmt) throws E;
    R visitFunctionStmt(Function stmt) throws E;
    R visitIfStmt(If stmt) throws E;
    R visitReturnStmt(Return stmt) throws E;
    R visitVarStmt(Var stmt) throws E;
    R visitAssignStmt(Assign stmt) throws E;
    R visitWhileStmt(While stmt) throws E;
  }

  static record Parameter(String name, VarType type) {}

  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }

  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }

  static class Function extends Stmt {
    Function(String name, VarType returnType, List<Parameter> params, List<Stmt> body) {
      this.name = name;
      this.returnType = returnType;
      this.params = params;
      this.body = body;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitFunctionStmt(this);
    }

    final String name;
    final VarType returnType;
    final List<Parameter> params;
    final List<Stmt> body;
  }

  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }

  static class Return extends Stmt {
    Return(Expr value) {
      this.value = value;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitReturnStmt(this);
    }

    final Expr value;
  }

  static class Var extends Stmt {
    Var(String name, VarType type, Expr initializer) {
      this.name = name;
      this.type = type;
      this.initializer = initializer;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitVarStmt(this);
    }

    final String name;
    final VarType type;
    final Expr initializer;
  }

  static class Assign extends Stmt {
    Assign(String name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitAssignStmt(this);
    }

    final String name;
    final Expr value;
  }

  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }

  abstract <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E;
}