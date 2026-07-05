import type { Dictionary } from '../keys';

// Ported from AppConstants.properties (the GWT default/English locale).
// `\n` is a real newline; `%s` is a positional placeholder filled by Translations.t().
export const en: Dictionary = {
  gameName: 'Tock',
  playCard: 'Play Card',
  forfeit: 'Forfeit',
  leaveGame: 'Leave game',
  send: 'Send',
  pawn1: 'Pawn 1',
  pawn2: 'Pawn 2',
  confirmLeaveGame: 'Are you sure you want to leave the game?',
  confirmLeaveYes: 'Leave',
  confirmLeaveNo: 'Stay',
  canvasNotSupported: 'Canvas is not supported in your browser.',
  hintAce: 'Ace: place a pawn on the board or move 1 step forward.',
  hintFour:
    'Four: move 4 steps backward.\nTip: you can use this on your start tile to cut across the entire board.',
  hintSeven:
    'Seven: make a total of 7 steps with one or two pawns.\n First select the card, then optionally choose a second pawn. Note: pawn 1 always moves first.',
  hintJack:
    'Jack: swap your pawn with an enemy pawn.\nYou can only select a suitable enemy after selecting this card.',
  hintQueen: 'Queen: move 12 steps forward.',
  hintKing: 'King: place a pawn on the board.',
  rulesButton: 'Game Rules',
  rulesTitle: 'Game Rules',
  rulesGettingOnBoard: 'Getting on the board',
  rulesSpecialCards: 'Special Cards',
  rulesClickToClose: 'Click anywhere to close',
  moveRejectedTitle: "You can't make that move",
  moveRejectedGeneric: "That move isn't allowed.",
  moveRejectedNotYourTurn: "It's not your turn (or that move was already played).",
  moveRejectedInvalidSelection: 'Select a pawn and a card first.',
  moveRejectedDontHaveCard: "You don't have that card.",
  moveRejectedWrongCardForMove: "You can't use that card for this move.",
  moveRejectedPawnOnNest:
    'That pawn is still in the nest — first bring it onto the board with an Ace or King.',
  moveRejectedPawnNotOnNest: 'That pawn is already on the board.',
  moveRejectedPawnNotOnBoard: 'You can only switch pawns that are on the board.',
  moveRejectedNotYourPawn: "That pawn isn't yours.",
  moveRejectedDestinationOccupiedByOwnPawn: 'One of your own pawns is already on that tile.',
  moveRejectedDestinationBlocked: "You can't move there — the path is blocked.",
  moveRejectedStartTileOccupied: "Your starting tile is occupied, so you can't come onto the board.",
  moveRejectedCannotPassStartTile: "You can't pass another player's pawn on their starting tile.",
  moveRejectedCannotSwitchOpponentOnOwnStart:
    "You can't switch with an opponent who is on their own starting tile.",
  moveRejectedCannotSwitchOwnPawns: "You can't switch two of your own pawns.",
  moveRejectedMustMoveExactSteps: 'You can only move %s steps with this pawn.',
  moveRejectedPawnClosedInFinish: "That pawn is closed in on the finish and can't move.",
  moveRejectedSplitNeedsTwoOwnPawns: 'A 7-split needs two of your own pawns.',
  moveRejectedSplitStepsNotSeven: 'The two parts of the 7 must add up to 7 steps.',
};