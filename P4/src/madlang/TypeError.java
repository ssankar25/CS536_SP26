package madlang;

//
// Feel free to define more kinds of type errors, as long as they
// do not overlap with the existing ones
//

// Do not modify
abstract class TypeError extends Exception {
  TypeError(String err) { super(err); }
}

// Do not modify
class IllegalApplication extends TypeError {
  String id; // The identifier that is supposed to be a function
  IllegalApplication(String id) {
    super("Illegal application: <" + id + "> is at function position, but does not have a function type");
    this.id = id;
  }
}

// Do not modify
class WrongArgumentCount extends TypeError {
  String id; // The function being applied
  int expected; // The expected number of arguments
  int actual; // The actual number of arguments found in function call
  WrongArgumentCount(String id, int expected, int actual) {
    super("Wrong argument count: <" + id + "> expects " + Integer.toString(expected) + " arguments" +
          ", but got " + Integer.toString(actual) + ">");
    this.id = id;
    this.expected = expected;
    this.actual = actual;
  }
}

// Do not modify
class MismatchedTypes extends TypeError {
  VarType expected; // The expected type
  VarType actual; // The actual type you got from an expression
  MismatchedTypes(VarType expected, VarType actual) {
    super("Mismatched types: expecting <" + expected.toString() + ">" +
          ", but got <" + actual.toString() + ">");
    this.expected = expected;
    this.actual = actual;
  }
}

// Do not modify
class UnboundReference extends TypeError {
  String id; // The name of the undefined identifier
  UnboundReference(String id) {
    super("Undefined identifier: <" + id + ">");
    this.id = id;
  }
}

/// ADDED ERRORS ///

class InvalidLiteral extends TypeError {
  InvalidLiteral() {
    super("Invalid Literal");
  }
}

class InvalidOperator extends TypeError {
  String operator;

  InvalidOperator(String operator) {
    super("Invalid Operator: <" + operator + ">");
    this.operator = operator;
  }
}

class DuplicateSymbol extends TypeError {
  String id;

  DuplicateSymbol(String id) {
    super("Duplicate symbol: <" + id + ">");
    this.id = id;
  }
}

class IllegalVarApplication extends TypeError {
  String id; // The identifier that is supposed to be a variable

  IllegalVarApplication(String id) {
    super("Illegal variable application: <" + id + "> is at a variable position, but is indicated as a function");
    this.id = id;
  }
}

class IllegalReturn extends TypeError {
  IllegalReturn() {
    super("Illegal return: Return statement appears outside of a function");
  }
}

class IllegalGlobalInitializer extends TypeError {
  IllegalGlobalInitializer() {
    super("Illegal global initializer: global initializers may not contain variable references or function calls");
  }
}