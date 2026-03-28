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