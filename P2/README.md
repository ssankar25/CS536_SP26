# P2: Madlang Interpreter

## Overview

In this assignment, you will implement an **interpreter** for the madlang programming language.

## Learning Goals

### 1. **Expression Evaluation**
You will implement the evaluation logic for all types of expressions:
- **Literals**: Numbers and boolean values
- **Variables**: Looking up values in the environment
- **Binary Operations**: Arithmetic, comparison, and logical operations
- **Unary Operations**: Negation and logical NOT
- **Function Calls**: Invoking functions with arguments

### 2. **Statement Evaluation**
You'll implement evaluation logic for all types of statements:
- **Variable Declarations**: Creating and initializing variables
- **Assignments**: Updating variable values in the environment
- **Control Flow**: `if` statements and `while` loops
- **Function Definitions**: Creating callable functions
- **Expression statement**: Evaluating an expression, e.g., `f(x, y, z);`, which may update global variables or perform other side effects
- **Return Statements**: Exiting functions with values

### 3. **Learn Runtime Concepts**
You'll implement fundamental interpreter concepts:
- **Environment Management**: Variable lookup, update, and shadowing
- **Error Handling**: Graceful runtime error reporting

## What is Madlang?

Madlang is a simple C-like programming language with following features. You can find the complete language specification on Canvas.

### **Types**
- `int`: 32-bit signed integers
- `bool`: Boolean values (`true` and `false`)

### **Variables**
```madlang
x : int = 42;
flag : bool = true;
y : int;  // Declaration without initialization
```

### **Functions**
```madlang
fun int factorial(int n) {
  if (n <= 1) {
    return 1;
  } else {
    return n * factorial(n - 1);
  }
}
```

### **Control Flow**
```madlang
// If statements
if (x > 0) {
  print x;
} else {
  print 0;
}

// While loops
while (x > 0) {
  print x;
  x = x - 1;
}
```

### **Expressions**
```madlang
// Arithmetic
result : int = (a + b) * c - d / e;

// Logical
valid : bool = (x > 0) && (y < 100) || (z == 0);

// Function calls
answer : int = factorial(5);
```

## Your Task

1. You need to implement the **interpreter** by filling in all the TODO items in `Interpreter.java`. You will also need to write the `Environment` class in `Environment.java`. The interpreter will use the environment to track the values of variables.
2. You should implement support for built-in I/O functions: `input() : int` and `output(n : int) : int` in your interpreter. Remember that `input` and `output` can be shadowed in non-global scopes.
3. You will add a number of tests. You can do this in `Main.java` or separately in a new file. Try to cover different language features and corner cases.
4. Your interpreter will handle runtime errors. For example, instead of crashing when evaluating `1 / 0` or `output(true);`, your interpreter should detect the error and print a helpful error message before exiting gracefully. The following is a list of semantic errors that your interpreter should handle:
   - Type mismatch: `output(true)`, `f(x) + f(x) && f(x)`, etc. When running into this error, your interpreter should print "Error: type mismatch" and exit.
   - Unbound variables or functions: Evaluating `f(v)` when `f` or `v` are not in the environment. The interpreter should print "Error: unbound reference" and exit.
   - Arithematic error: division by zero, taking remainder by zero, etc. "Error: arithmetic error" and exit.
5. Complete the survey about P1 for one point.

**submit a zip of this folder**

**if you worked with a partner, only one of you needs to submit; include both of your names at the top of the README.md (this file)**

## Example Programs

### **Expression evaluation**
```madlang
a : int = 1 + 4 * 3;
b : int = a * a; // <- Here a * a should be 169
```

### **Basic Arithmetic**
```madlang
a : int = 10;
b : int = 5;
output(a + b);  // Should print: 15
```

### **Control Flow**
```madlang
x : int = 5;
while (x > 0) {
  output(x);
  x = x - 1;
}
// Should print: 54321
```

### **Function with Recursion**
```madlang
fun factorial(n : int) : int {
  if (n <= 1) {
    return 1;
  } else {
    return n * factorial(n - 1);
  }
}

result : int = factorial(5);
output(result);  // Should print: 120
```

### **Nested Scopes**
```madlang
x : int = 10;
output(x);  // Prints: 10
{
  x : int = 20;
  output(x);  // Prints: 20
}
output(x);  // Prints: 10
```
