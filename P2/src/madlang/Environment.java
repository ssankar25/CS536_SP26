package madlang;

import java.util.HashMap;
import java.util.Map;

class Environment {

  // Class for defining the entries stored in the environment
  // Both variable values and function objects can be stored in the HashMap,
  // so the same HashMap can be used to check for function names in the same scope,
  // and function shadowing can be done
  static final class Entry {

    // If type == null, this entry is a function binding.
    final VarType type;

    // For a function, its value is an object containing 
    // the information needed to call the function
    Object value;

    Entry(VarType type, Object value) {
      this.type = type;
      this.value = value;
    }

    boolean isFunction() {
      return type == null;
    }
  }

  // Map for names -> Entries
  private final Map<String, Entry> values = new HashMap<>();

  // Environment of the parent scope
  private final Environment parent;

  // Default constructor initializes the parent to null
  Environment() {
    this(null);
  }

  Environment(Environment parent) {
    this.parent = parent;
  }

  // Get the variable or function value from the hashmap
  Entry lookup(String name) {

    // First check the current scope for the name (can be shadowed)
    if (values.containsKey(name)) return values.get(name);

    // Check the parent scope for the name, if it exists
    if (parent != null) return parent.lookup(name);

    return null;
  }

  // Used to add new variable to hashmap
  void defineVar(String name, VarType type, Object value) {

    // First check for duplicate declaration in the same scope
    if (values.containsKey(name)) {
      throw new IllegalStateException("Error: duplicate symbol - " + name);
    }

    values.put(name, new Entry(type, value));
  }

  // Used to add new function to hashmap
  void defineFunction(String name, Object funcCallable) {

    // First check for duplicate declaration in same scope
    if (values.containsKey(name)) {
      throw new IllegalStateException("Error: duplicate symbol - " + name);
    }

    values.put(name, new Entry(null, funcCallable));
  }

  // Used to evaluate assignment statements by adding the 
  // new assignment to the Hashmap
  void assign(String name, Object value) {

    if (values.containsKey(name)) {
      // First check if the variable is in the current scope and reassign if so
      values.get(name).value = value;
    }
    else if (parent != null) {
      // If the variable is defined in a parent scope, then reassign that
      parent.assign(name, value);
    }
    else {
      // Variable not declared anywhere
      throw new IllegalStateException("Error: unbound reference - " + name);
    }
  }

}
