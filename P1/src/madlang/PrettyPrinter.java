package madlang;

import madlang.ast.Ast;

/**
 * Pretty printer entry point for CS536 MadLang.
 *
 * P1 requirement: implement PrettyPrinter.print(program) to return a canonical
 * source representation of the given AST.
 */
public final class PrettyPrinter {
    private PrettyPrinter() {}

    /**
     * Pretty-print a MadLang program AST into a canonical source string.
     *
     * @param program the AST of a whole MadLang program
     * @return the pretty-printed program as a string
     */
    public static String pretty(Ast.Program program) {
        throw new UnsupportedOperationException(
      		"TODO: pretty printer not implemented"
        );
    }
}
