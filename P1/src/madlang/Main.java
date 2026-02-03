package madlang;

import madlang.test.TestPrograms;

public final class Main {
  public static void main(String[] args) {
    if (args.length > 0) {
      System.err.println(
        "Warning: parser not implemented, ignoring the input file."
      );
    }
    TestPrograms.runAll();
  }
}
