import type { Dictionary } from '../keys';

// Ported from AppConstants_nl.properties (Nederlands).
export const nl: Dictionary = {
  gameName: 'Keezen',
  playCard: 'Kaart spelen',
  forfeit: 'Kaarten weggooien',
  leaveGame: 'Spel verlaten',
  send: 'Verstuur',
  pawn1: 'Pion 1',
  pawn2: 'Pion 2',
  confirmLeaveGame: 'Weet je zeker dat je het spel wilt verlaten?',
  canvasNotSupported: 'Canvas wordt niet ondersteund in je browser.',
  hintAce: 'Aas: zet een pion op het bord of beweeg 1 stap vooruit.',
  hintFour:
    'Vier: beweeg 4 stappen achteruit.\nTip: je kunt dit gebruiken op je startveld om een heel bord af te snijden.',
  hintSeven:
    'Zeven: maak in totaal 7 stappen met één of twee pionnen.\n Selecteer eerst de kaart en kies daarna eventueel een tweede pion. Let op: pion 1 beweegt altijd als eerste.',
  hintJack:
    'Boer: ruil jouw pion met een vijandige pion.\nJe kunt pas een geschikte vijand selecteren wanneer je deze kaart hebt geselecteerd.',
  hintQueen: 'Vrouw: beweeg 12 stappen vooruit.',
  hintKing: 'Koning: zet een pion op het bord.',
  rulesButton: 'Spelregels',
  rulesTitle: 'Spelregels',
  rulesGettingOnBoard: 'Op het bord komen',
  rulesSpecialCards: 'Speciale kaarten',
  rulesClickToClose: 'Klik ergens om te sluiten',
  moveRejectedTitle: 'Deze zet kan niet',
  moveRejectedGeneric: 'Deze zet is niet toegestaan.',
  moveRejectedNotYourTurn: 'Je bent niet aan de beurt (of deze zet is al gedaan).',
  moveRejectedInvalidSelection: 'Selecteer eerst een pion en een kaart.',
  moveRejectedDontHaveCard: 'Je hebt die kaart niet.',
  moveRejectedWrongCardForMove: 'Je kunt die kaart niet voor deze zet gebruiken.',
  moveRejectedPawnOnNest:
    'Die pion staat nog op het nest — zet hem eerst op het bord met een Aas of Koning.',
  moveRejectedPawnNotOnNest: 'Die pion staat al op het bord.',
  moveRejectedPawnNotOnBoard: 'Je kunt alleen ruilen met pionnen die op het bord staan.',
  moveRejectedNotYourPawn: 'Die pion is niet van jou.',
  moveRejectedDestinationOccupiedByOwnPawn: 'Een van je eigen pionnen staat al op dat veld.',
  moveRejectedDestinationBlocked: 'Je kunt daar niet heen — de weg is geblokkeerd.',
  moveRejectedStartTileOccupied: 'Je startveld is bezet, dus je kunt niet op het bord komen.',
  moveRejectedCannotPassStartTile:
    'Je kunt niet voorbij een pion van een andere speler op zijn startveld.',
  moveRejectedCannotSwitchOpponentOnOwnStart:
    'Je kunt niet ruilen met een tegenstander die op zijn eigen startveld staat.',
  moveRejectedCannotSwitchOwnPawns: 'Je kunt geen twee eigen pionnen met elkaar ruilen.',
  moveRejectedMustMoveExactSteps: 'Je kunt met deze pion maar %s stappen zetten.',
  moveRejectedPawnClosedInFinish: 'Die pion zit klem op de finish en kan niet bewegen.',
  moveRejectedSplitNeedsTwoOwnPawns: 'Voor een 7-splitsing heb je twee eigen pionnen nodig.',
  moveRejectedSplitStepsNotSeven: 'De twee delen van de 7 moeten samen 7 stappen zijn.',
};