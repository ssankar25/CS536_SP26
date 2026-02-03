## CS536 Spring 2026 — Assignment P1

### Pretty Printer for MadLang
---
### Overview

In this assignment, you will implement a **pretty printer** for **MadLang**. The goal is to convert an abstract syntax tree (AST) into a readable and well-formatted source program.

The MadLang specification is available [on Canvas](https://canvas.wisc.edu/courses/485409/files?preview=50989659). We recommend checking it regularly, as **we may add further clarifications** over time.

### Learning Goals

- Understand the structure of Abstract Syntax Trees (ASTs).

- Understand and practice the Visitor Pattern commonly used in compiler implementations.

### Your Tasks

#### Task 1 — Implement the Pretty Printer

Implement the pretty printer in `src/PrettyPrinter.java`.

You must not modify any AST classes.

You may modify the class PrettyPrinter and add additional helper classes or files if needed, but you must provide the following standard API:

```java
public static String pretty(Ast.Program program)
```

#### Task 2 — Test with the following Example Program

```Rust
fn Outer(x: int): int {
    r: int = 0;
    i: int = 0;

    fn Inner(y: int): int {
        r = r + 10;
        return y * y;
    }

    while (i < x) {
        if (i % 2 == 0) {
            r = r + Inner(i);
        }
        i = i + 1;
    }
    return r;
}

fn main(): int {
    r: int = Outer(5);
    output(r);
    return 0;
}
```
In `madlang/test/TestPrograms.java`, hard-code the corresponding AST for this program inside `test2()` and use it to test your pretty printer.


#### Task 3 — Construct an AST to Test your pretty printer

In `madlang/test/TestPrograms.java`, choose an AST and hard-code it inside `test3()`.

**Requirements:**

1. The AST must represent a **valid** MadLang program.
2. The program must **contain your NetID** as an identifier (variable name, function name, or parameter name).
3. Both the source program, and its pretty-printed output must contain **no more than 20 lines of code**.

Note: Since student submissions may be used to **test each other’s pretty printers**, this task ensures diversity and robustness of test cases.

Your example Requirements:

1. The AST must represent a valid MadLang program.

2. The program must contain your NetID as an identifier
(variable name, function name, or parameter name).

1. Both the source program and its pretty-printed output must contain no more than 20 lines of code.

### Formatting Rules:

1. Indentation: 2 spaces or 4 spaces are both acceptable.
2. Each time a new block is entered using `{`, the indentation level increases by one. For example, 
```Rust
fn f(x: int): int { 
  if (x > 0) {
    while (x > 1) {
      x = x - 1;
    }
  }
  return x;
}
```
3. Formatting style choices (including whether `{` appears on the same line or the next line, whether to insert spaces around operators, whether to insert spaces after `if` and `while`, and whether to insert blank lines between functions) are **your choice**.
   However, the output must remain **consistent**, readable, and clean.
4. Comments do **not** appear in the AST. Therefore, you do **not** need to consider comment printing.


### Bonus — Minimal Parentheses (+20%)

**Minimize unnecessary parentheses** in expression printing, while preserving correct operator precedence and associativity, in order to improve readability.

Example:

```Rust
-a + a * b - b
```

instead of:

```Rust
((-a) + (a * b)) - b
```

### Running

From the project root directory:
```bash
make run
```
or:

```bash
make run FILE=filename.madl
```

### Grading

| Component | Weight |
|------------|---------|
| Functional correctness | 80% |
| Code quality & style | 20% |
| Bonus — minimal parentheses | +20% |

#### Functional Correctness (80%)

You must ensure the following:

- **Task 1 (the pretty printer):**

  - The output must satisfy the **formatting rules**.
  - Pretty-printing **should not change the program structure**. Or formally: `parse(pretty(ast))` is always equal to `ast`.
  - `Span` represents the source location information of an AST node. Your pretty printer **must not depend on span information** for its functionality.
- **Task 2:** The constructed AST must correctly correspond to the given example program.
- **Task 3:** The constructed AST must satisfy all stated requirements:
  - a valid MadLang program,
  - contains your NetID as an identifier,
  - no more than 20 lines of code.

#### Code Quality & Style (20%)

We will evaluate code clarity and readability. In particular, failure to properly use **the `Visitor<R>` pattern** may result in point deductions.

#### Bonus — Minimal Parentheses (+20%)
This part is graded based on **how effectively your pretty printer reduces unnecessary parentheses** in the output, while still preserving correct operator precedence and associativity.