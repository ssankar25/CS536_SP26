# P4: Madlang Name Analysis and Type Checking

## Overview

In this assignment, you will implement **type checking** for the madlang programming language. These is a critical static analysis phases that catches errors that the parser cannot detect.

## Learning Goals

### 1. **Type Checking**
You will implement a type checker that:
- Assigns types to all expressions
- Verifies type compatibility for operations
- Checks function call argument types and counts
- Validates types of statements

### 2. **Error Reporting**
- You will report type errors in a way that is clear to the user

## Type System Rules
Your type checker should enforce the following rules. For additional information, refer to the language spec.

### **Literals**
- Integer literals (e.g., `42`, `0`, `-5`) have type `int`
- Boolean literals (`true`, `false`) have type `bool`

### **Variables**
- Variables have the type specified in their declaration, until shadowed
- Example: `x : int = 5;` means `x` has type `int` for the current scope

### **Arithmetic Operators** (`+`, `-`, `*`, `/`, `%`)
- Both operands must be `int`
- Result type is `int`

### **Comparison Operators** (`<`, `<=`, `>`, `>=`, `==`, `!=`)
- Both operands must be `int`
- Result type is `bool`

### **Logical Operators** (`&&`, `||`)
- Both operands must be `bool`. Note that there is no short-circuiting when type-checking!
- Result type is `bool`

### **Logical NOT** (`!`)
- Operand must be `bool`
- Result type is `bool`

### **Unary Minus** (`-`)
- Operand must be `int`
- Result type is `int`

### **IO expressions**
- The `input` built-in function takes no arguments and returns an `int`
- The `output` built-in function takes an `int` and returns an `int`

### **Variable Declarations**
- If an initializer is present, its type must match the declared type
- Example: `x : int = true;` is a type error

### **Assignments**
- The right-hand side type must match the variable's declared type
- Example: If `x` is declared as `int`, then `x = true;` is a type error

### **If Statements**
- The condition must have type `bool`

### **While Statements**
- The condition must have type `bool`

### **Return Statements**
- The returned value's type must match the function's return type
- Example: In a function declared as `fn f() : int {...}`, all return statements must return `int`, unless in a nested function

### **Function Calls**
- The number of arguments must match the number of parameters
- Each argument's type must match the corresponding parameter's type

Your type checker should enforce the following rules:

### **Variables**
- A variable cannot be declared twice in the same scope
- A variable must be declared before it's used
- Variables in inner scopes can shadow variables in outer scopes
- Variable lookup follows lexical scoping rules

### **Functions**
- A function cannot be declared twice in the same scope
- A function can shadow functions from outer scopes
- A function must be declared before it's called, except in the case of recursion
- In a function call, the function name must refer to a function, not a variable

### **Scoping**
- Each block (`{ ... }`) creates a new scope. Branches of if-statements and body of while-statements always create a new scope, even without the block syntax
- Function parameters are in the function's scope
- Variables declared in a scope are only visible in that scope and nested scopes


## Type errors
In `TypeError.java`, you will find four error classes: `IllegalApplication`, `WrongArgumentCount`, `MismatchedTypes`, and `UnboundReference`. When you encounter a type-error during typechecking, you need to raise one of the aforementioned exceptions:

- `IllegalApplication(id)`: When `id` is bound in the current context, but refers to a variable instead of a function
- `WrongArgumentCount(id, expected, actual)`: When a function application applies `f` to `actual` number of arguments, which is different from `f`'s `expected` number
- `MismatchedTypes(expected, actual)`: When the `expected` type differs from the `actual` type.
  + For example, `(4 + 2) || true` expects a `bool`, but the actual LHS type is `int`
  + For example, if `fn f(x : int, y : int) : bool { ... }` is in scope, then `f(1, true)` expects an `int` in the second argument but receives an actual `bool`.
  + For example, `x : int = 0; x = false;` is also an error
- `UnboundReference(id)`: When `id` is used, but is not bound in the current scope. This includes local and global scopes.


## Your Task
Implement type checking with error-reporting for madlang
- Fill in `Context.java` and `TypeChecker.java` for your implementation, adding additional files if needed
- `Context` can be thought of as the static analog of `Environment` from P2!
- If a madlang program is well-typed, checking it with a new `TypeChecker` should not raise an exception.


### Bonus — UnusedIdentifierAnalysis (+20%)
- For the bonus section, you will complete the `unused` method in `UnusedIdentifierAnalysis.java`.
- An identifier is unused if there are no non-self syntactic reference to it in the scope it's defined in, or any inner scopes.
  + For example, in `fn f() : int { x : int = 1; return 0; }`, the variable `x` is unused
- Be careful with shadowing. An identifier can be unused AND shadowed by an identifier of the same name, which is used.
- You do not need to implement recursive usage analysis. That is, if `y` is unused in `x : int; y : int = x + 1;`, `x` is still considered used. The global `main` function, which should always be the last one in the global scope if it exists, is always considered used.

### **Test Your Implementation**
Although we won't be grading your test cases this time, you should still write some positive and negative test cases to make sure your type checker is working correctly!

## Deliverables
Submit a `.zip` file of the `P4` folder containing your changes.

**If you worked with a partner, only one of you needs to submit; include both Net ID's at the top of the README.md**

### Grading

| Component | Weight |
|------------|---------|
| Functional correctness | 90% |
| Code quality & style | 10% |
| Bonus — unused identifier analysis | +20% |
