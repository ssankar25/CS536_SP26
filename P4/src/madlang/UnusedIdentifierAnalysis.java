package madlang;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ************
// BONUS ONLY!! Not required for 100% completion
// In this section, you will build a static analyzer that reports warnings for variable and/or function declarations that are not used
// Refer to the README for more details
// ************

// Do not throw any exceptions in the visitor methods
// Feel free to add `implements ...` to this class
class UnusedIdentifierAnalysis {

  // TODO: Feel free to add additional instance variables to the class

  // Leave the constructor signature unchanged
  UnusedIdentifierAnalysis() {
  }

  // Keep the method signature unchanged
  // Given a madlang program, report a set of identifiers (strings) that are unused. This includes functions and variables
  public Set<String> unused(List<Stmt> stmts) {
    return new HashSet<>();
    // TODO: Implement
  }
}
