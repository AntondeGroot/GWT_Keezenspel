package ADG.Games.Keezen;

/**
 * Standalone entry point for generating colored pawn images at build time.
 * Called by exec-maven-plugin during the process-classes phase.
 * Writes pawn0.png through pawn7.png into the directory passed as args[0].
 */
public class PawnImageGenerator {
  public static void main(String[] args) {
    String outputDir = args.length > 0 ? args[0] : System.getProperty("user.dir");
    for (int i = 0; i < 8; i++) {
      ImageProcessing.create(i, outputDir);
    }
  }
}