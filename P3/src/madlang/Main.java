package madlang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Error: missing input file.");
      System.err.println("usage: make run FILE=filename.madl");
      System.exit(1);
      return;
    }

    String source;
    try {
      source = Files.readString(Path.of(args[0]));
    } catch (IOException e) {
      System.err.println("Error: failed to read input file: " + args[0]);
      System.err.println("Reason: " + e.getMessage());
      System.exit(1);
      return;
    }

    // Source is read successfully; parser/interpreter pipeline is to be implemented.
    if (source.isEmpty()) {
      System.err.println("Warning: input file is empty: " + args[0]);
    }
    System.out.println("P3 placeholder main: parser/interpreter pipeline not implemented yet.");
  }
}
