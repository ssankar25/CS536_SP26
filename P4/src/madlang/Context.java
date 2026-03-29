package madlang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Context {

  /**
   * Class for storing function type information, including parameter types and return type.
   */
  static final class FunctionTypeInfo {
    final List<VarType> paramTypes;
    final VarType returnType;

    FunctionTypeInfo(List<VarType> paramTypes, VarType returnType) {
      // Store copy of parameter type list for security
      this.paramTypes = List.copyOf(paramTypes);
      this.returnType = returnType;
    }
  }
  
  /**
   * Class for entries stored in the context map. An entry can either be a variable or a function, but not both.
   * 
   * If the entry is a variable, then varType is non-null and funcType is
   * null. If the entry is a function, then funcType is non-null and varType is null.
   */
  static final class Entry {
    VarType varType; // NULL if the entry is a function
    FunctionTypeInfo funcType; // NULL if entry is a variable

    /**
     * Constructor for an entry in the context.
     * 
     * @param varType The variable type if the entry is a variable, or null if the entry is a function.
     * @param funcType The function type information if the entry is a function, or null if the entry is a variable.
     */
    Entry(VarType varType, FunctionTypeInfo funcType) {
      this.varType = varType;
      this.funcType = funcType;
    }

    /**
     * Helper method to check if the entry is a function.
     */
    boolean isFunction() {
      return funcType != null;
    }
  }

  // Map for names -> entries
  private final Map<String, Entry> values = new HashMap<>();

  // Parent scope's context
  private final Context parent;

  /**
   * Default context constructor that initializes the parent to null.
   */
  Context() {
    this(null);
  }

  /**
   * Context constructor that sets the parent context.
   * 
   * @param parent The parent context to set.
   */
  Context(Context parent) {
    this.parent = parent;
  }

  /**
   * Lookup the identifier type in the current scope chain.
   * 
   * @param name The identifier to lookup.
   * @return The corresponding entry, or null if not found.
   */
  Entry lookup(String name) {

    // First check the current scope for the name (can be shadowed)
    if (values.containsKey(name)) return values.get(name);

    // Check the parent scope for the name, if it exists
    if (parent != null) return parent.lookup(name);

    return null;
  }

  /**
   * Adds a new variable type to the context if there is no duplicate in the same scope.
   * 
   * @param name The name of the variable.
   * @param type The variable type (int or bool).
   */
  void defineVar(String name, VarType type) throws DuplicateSymbol {

    if (values.containsKey(name)) {
      // ERROR CASE: Duplicate variable declaration in the same scope
      throw new DuplicateSymbol(name);
    }

    values.put(name, new Entry(type, null));
  }

  /**
   * Adds a new function type to the context if there is no duplicate in the same scope.
   * 
   * @param name The name of the function.
   * @param typeInfo The function type information.
   */
  void defineFunction(String name, FunctionTypeInfo typeInfo) throws DuplicateSymbol {

    if (values.containsKey(name)) {
      // ERROR CASE: Duplicate function declaration in the same scope
      throw new DuplicateSymbol(name);
    }

    values.put(name, new Entry(null, typeInfo));
  }
}
