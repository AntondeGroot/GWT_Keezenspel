package adg.keezen;

import java.util.Map;

/**
 * Localized labels/descriptions for the game options, keyed by the {@link
 * com.adg.openapi.model.GameOption}'s {@code labelKey}/{@code descriptionKey} then locale.
 * The Keezen backend resolves these itself (mirroring {@code GameNameController}) so the
 * GameRoom just displays the returned strings, like it does for the game name.
 *
 * <p>English is NOT stored here — callers pass the English label/description from {@link
 * KeezenGameOptions} as the fallback, which is also used for any missing locale or key.
 * Locales match the game-name endpoint: nl, de, fr, nb, it.
 *
 * <p><b>Draft translations — please review.</b> The short labels are the priority; the
 * longer descriptions (game-rule wording) especially warrant a native-speaker check.
 */
public final class GameOptionTranslations {

  private GameOptionTranslations() {}

  private static final Map<String, Map<String, String>> STRINGS = Map.of(
      "gameOption.exactMoveRequired.label", Map.of(
          "nl", "Exacte zet vereist",
          "de", "Exakter Zug erforderlich",
          "fr", "Déplacement exact requis",
          "nb", "Nøyaktig trekk kreves",
          "it", "Mossa esatta richiesta"),
      "gameOption.exactMoveRequired.description", Map.of(
          "nl", "Indien ingeschakeld moet een pion exact op zijn bestemming landen. "
              + "Van richting veranderen mag niet: terugkaatsen op een geblokkeerd startveld "
              + "of voorbij de finish schieten wordt als een ongeldige zet geweigerd.",
          "de", "Wenn aktiviert, muss eine Figur genau auf ihrem Zielfeld landen. "
              + "Richtungswechsel sind nicht erlaubt: das Abprallen an einem blockierten "
              + "Startfeld oder das Überschreiten der Ziellinie wird als ungültiger Zug abgelehnt.",
          "fr", "Si activé, un pion doit s'arrêter exactement sur sa destination. "
              + "Les changements de direction sont interdits : rebondir sur une case de départ "
              + "bloquée ou dépasser la ligne d'arrivée sera refusé comme un coup illégal.",
          "nb", "Når aktivert må en brikke lande nøyaktig på målfeltet. "
              + "Retningsendringer er ikke tillatt: å sprette av et blokkert startfelt eller "
              + "å gå forbi målfeltet avvises som et ulovlig trekk.",
          "it", "Se attivato, una pedina deve fermarsi esattamente sulla destinazione. "
              + "I cambi di direzione non sono consentiti: rimbalzare su una casella di partenza "
              + "bloccata o superare il traguardo verrà rifiutato come mossa non valida."),
      "gameOption.mustPlayIfPossible.label", Map.of(
          "nl", "Verplicht zetten indien mogelijk",
          "de", "Zugpflicht wenn möglich",
          "fr", "Jouer obligatoirement si possible",
          "nb", "Må spille hvis mulig",
          "it", "Obbligo di giocare se possibile"),
      "gameOption.mustPlayIfPossible.description", Map.of(
          "nl", "Indien ingeschakeld kun je je beurt niet overslaan als er een geldige zet "
              + "mogelijk is. Pionnen die al in de finish staan zijn uitgezonderd — die hoef "
              + "je nooit te verplaatsen.",
          "de", "Wenn aktiviert, kannst du deinen Zug nicht aussetzen, solange ein gültiger "
              + "Zug möglich ist. Figuren, die bereits im Ziel sind, sind ausgenommen — du "
              + "musst sie nie bewegen.",
          "fr", "Si activé, vous ne pouvez pas passer votre tour tant qu'un coup valide est "
              + "possible. Les pions déjà dans l'arrivée sont exemptés — vous n'êtes jamais "
              + "obligé de les déplacer.",
          "nb", "Når aktivert kan du ikke stå over turen hvis et gyldig trekk er mulig. "
              + "Brikker som allerede er i mål er unntatt — du må aldri flytte dem.",
          "it", "Se attivato, non puoi passare il turno se è disponibile una mossa valida. "
              + "Le pedine già arrivate sono esentate — non sei mai obbligato a spostarle."));

  /**
   * The localized string for a {@code labelKey}/{@code descriptionKey}, or {@code
   * englishFallback} for any missing locale or key.
   */
  public static String resolve(String key, String englishFallback, String locale) {
    if (key == null || locale == null) return englishFallback;
    Map<String, String> byLocale = STRINGS.get(key);
    if (byLocale == null) return englishFallback;
    return byLocale.getOrDefault(locale.toLowerCase(), englishFallback);
  }
}
