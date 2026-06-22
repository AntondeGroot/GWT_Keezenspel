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

    // Game rules modal
    String rulesButton();
    String rulesTitle();
    String rulesGettingOnBoard();
    String rulesSpecialCards();
    String rulesClickToClose();

    // Move-rejected popup
    String moveRejectedTitle();
    String moveRejectedGeneric();
    String moveRejectedNotYourTurn();
    String moveRejectedInvalidSelection();
    String moveRejectedDontHaveCard();
    String moveRejectedWrongCardForMove();
    String moveRejectedPawnOnNest();
    String moveRejectedPawnNotOnNest();
    String moveRejectedPawnNotOnBoard();
    String moveRejectedNotYourPawn();
    String moveRejectedDestinationOccupiedByOwnPawn();
    String moveRejectedDestinationBlocked();
    String moveRejectedStartTileOccupied();
    String moveRejectedCannotPassStartTile();
    String moveRejectedCannotSwitchOpponentOnOwnStart();
    String moveRejectedCannotSwitchOwnPawns();
    /** Contains a "%s" placeholder for the number of steps. */
    String moveRejectedMustMoveExactSteps();
    String moveRejectedPawnClosedInFinish();
    String moveRejectedSplitNeedsTwoOwnPawns();
    String moveRejectedSplitStepsNotSeven();
}