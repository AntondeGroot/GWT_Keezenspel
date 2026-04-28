package adg.keezen.i18n;

import com.google.gwt.i18n.client.Constants;

public interface AppConstants extends Constants {

    // Buttons
    String playCard();
    String forfeit();
    String leaveGame();
    String send();

    // Pawn labels
    String pawn1();
    String pawn2();

    // Dialogs / errors
    String confirmLeaveGame();
    String canvasNotSupported();

    // Card hints (special cards only; regular cards have no hint)
    String hintAce();
    String hintFour();
    String hintSeven();
    String hintJack();
    String hintQueen();
    String hintKing();
}