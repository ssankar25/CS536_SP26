package madlang;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment class that maps variable and function references
 * to their corresponding values for a running program.
 */
class Environment {

  /**
   * Class for defining the entries stored in the environment.
   * 
   * Both variable values and function objects can be stored in the HashMap,
   * so the same HashMap can be used to check for function names in the same scope,
   * and function shadowing can be done.
   */
  static final class Entry {

    // If type == null, this entry is a function binding.
    final VarType type;

    // For a function, its value is an object containing 
    // the information needed to call the function
    Object value;
    
    /**
     * Entry constructor that sets the HashMap entry's type and value.
     * 
     * @param type The type of the entry (type == null for functions).
     * @param value The value of the entry.
     */
    Entry(VarType type, Object value) {
      this.type = type;
      this.value = value;
    }

    /**
     * Checks to see if the entry is a function (type == null for functions).
     * 
     * @return True if the entry is a function, false otherwise.
     */
    boolean isFunction() {
      return type == null;
    }
  }

  // Map for names -> Entries
  private final Map<String, Entry> values = new HashMap<>();

  // Environment of the parent scope
  private final Environment parent;

  /**
   * Default environment constructor the initializes the parent to null.
   */
  Environment() {
    this(null);
  }

  /**
   * Environment constructor that sets the parent environment.
   * 
   * @param parent The parent environment to set.
   */
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

  /**
   * Adds a new variable to the environment if there is no duplicate in the same scope.
   * 
   * @param name The name of the variable.
   * @param type The variable type (int or bool).
   * @param value The value of the variable.
   */
  void defineVar(String name, VarType type, Object value) {

    // First check for duplicate declaration in the same scope
    if (values.containsKey(name)) {
      throw new IllegalStateException("Error: duplicate symbol - " + name);
    }

    values.put(name, new Entry(type, value));
  }

  /**
   * Adds a new function to the environment if there is no duplicate in the same scope.
   * 
   * @param name The name of the function.
   * @param funcCallable The callable function object that is the value of the function.
   */
  void defineFunction(String name, Object funcCallable) {

    // First check for duplicate declaration in same scope
    if (values.containsKey(name)) {
      throw new IllegalStateException("Error: duplicate symbol - " + name);
    }

    values.put(name, new Entry(null, funcCallable));
  }

  /**
   * Assigns an existing variable in the environment to the specified value.
   * 
   * @param name The name of the variable to assign.
   * @param value The value to assign the variable to.
   */
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
