package madlang;

import java.util.List;

abstract class Expr {
  interface Visitor<R, E extends Throwable> {
    R visitBinaryExpr(Binary expr) throws E;
    R visitLiteralExpr(Literal expr) throws E;
    R visitUnaryExpr(Unary expr) throws E;
    R visitVariableExpr(Variable expr) throws E;
    R visitCallExpr(Call expr) throws E;
  }

  static class Binary extends Expr {
    Binary(Expr left, Operator operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Operator operator;
    final Expr right;
  }

  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }

  static class Unary extends Expr {
    Unary(Operator operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitUnaryExpr(this);
    }

    final Operator operator;
    final Expr right;
  }

  static class Variable extends Expr {
    Variable(String name) {
      this.name = name;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitVariableExpr(this);
    }

    final String name;
  }

  static class Call extends Expr {
    Call(String name, List<Expr> arguments) {
      this.name = name;
      this.arguments = arguments;
    }

    @Override
    <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
      return visitor.visitCallExpr(this);
    }

    final String name;
    final List<Expr> arguments;
  }

  abstract <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E;
}
