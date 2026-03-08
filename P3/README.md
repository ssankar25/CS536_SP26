## CS536 Assignment P3 — MadLang Parser

### Overview

In this assignment, you will implement a **parser** for **MadLang** and connect it to your interpreter from P2 so that a `.madl` source file can be parsed and executed end-to-end.

The MadLang specification can be found under **Canvas -> Files**. We recommend checking it regularly, as **we may add further clarifications** over time. Latest update: Feb 24, 2026

We are providing minimal instructions for this assignment. You can use AI tools for guidance on topics such as how to get started and how to use ANTLR, as long as you eventually understand the underlying logic.


### Learning Goals

- Understand how lexical analysis and parsing work together.
- Practice using a parser generator (for example, ANTLR) or a hand-written parser.
- Connect frontend compilation stages (parsing) with runtime execution (interpreting).
- (Optional / Bonus) Improve compiler-style error reporting.

### Your Tasks

#### Task 1 — Implement the Parser

Implement a parser for MadLang source programs based on the language specification.

You may choose either:
- a parser generator (such as ANTLR). We strongly encourage this option.
- a hand-written parser.

Your parser should build the AST representation expected by your interpreter.

#### Task 2 — Connect Parser to Your P2 Interpreter

Copy relevant code from your P2 implementation (interpreter/runtime support) and connect it to the parser pipeline so that:

1. A `.madl` file is read as input.
2. The file is parsed into an AST.
3. The resulting AST is executed by the interpreter.

#### Task 3 — Submit One NetID Test Program

In addition to the starter tests already in this repository, create and submit **one** test program named `YOUR-NETID.madl` (for example, `badger43.madl`) under `test-programs/`.

Test-case submission requirement for this assignment:
- Submit exactly one **student-authored** valid test program (`YOUR-NETID.madl`) under `test-programs/`.
- Keep the provided starter tests in `test-programs/` unchanged.
- We do not require any language-feature coverage percentage.
- Please do not submit tests that rely on UB, run for excessively long time, or produce excessively long output.

#### Grading Test Policy

- The official grading suite includes hidden tests (not all grading tests are public in advance).
- For P3 grading, we will not run tests submitted by students under `test-programs/`.
- Grading tests only include programs with clearly defined semantics in the language spec. If behavior is not clearly specified, that case will not be used for grading.
- We will not intentionally design P3 tests to target specific interpreter corner cases (for example, shadowing-only stress tests). However, if your interpreter still contains bugs from P2, those bugs can still cause failures on normal P3 tests, so you are responsible for fixing necessary P2 issues.

### Requirements

This starter repository is a minimal code base. You may modify existing code and add more files/classes as needed, but the following command must work:

```bash
make run FILE=filename.madl
```

Your implementation should support valid MadLang programs described in the provided specification.
Sample test cases are provided under `test-programs/` for your local testing.

If you use ANTLR or another parser generator that depends on an external JAR, you **must** either (1) include that JAR in your submission package, or (2) ensure your Makefile correctly downloads the JAR so that `make run FILE=...` works after a fresh clone or unpack.

Example (Makefile downloads ANTLR jar automatically):

```make
ANTLR_VERSION := 4.13.2
ANTLR_JAR := tools/antlr-$(ANTLR_VERSION)-complete.jar

$(ANTLR_JAR):
	mkdir -p tools
	curl -L -o $(ANTLR_JAR) https://www.antlr.org/download/antlr-$(ANTLR_VERSION)-complete.jar
```

### Bonus — Better Error Reporting (+20%)

Improve error messages to include:
- line and column information (required), and
- a clear human-readable explanation.

Reference example (from actual `gcc` output):

Erroring C code:

```c
int shared = 1;
int shared = 2;

int main(void) {
    return shared;
}
```

Compiler output:

```text
redef.c:2:5: error: redefinition of 'shared'
    2 | int shared = 2;
      |     ^
redef.c:1:5: note: previous definition is here
    1 | int shared = 1;
      |     ^
1 error generated.
```

This bonus may apply to:
- parse-time errors, and/or
- runtime semantic errors (for example, unbound references or type mismatch).
The assignment score is capped at 100, and bonus points can be used to recover lost points up to that cap.

### Grading

| Component | Weight |
|------------|---------|
| Functional correctness | 80% |
| Code quality & style | 10% |
| Submit required `YOUR-NETID.madl` test | 10% |
| Bonus — better error reporting | +20% |
