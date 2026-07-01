import type { Dictionary } from '../keys';

// Ported from AppConstants_nb.properties (Norsk).
export const nb: Dictionary = {
  playCard: 'Spill kort',
  forfeit: 'Kast bort kortene',
  leaveGame: 'Forlat spillet',
  send: 'Send',
  pawn1: 'Brikke 1',
  pawn2: 'Brikke 2',
  confirmLeaveGame: 'Er du sikker på at du vil forlate spillet?',
  canvasNotSupported: 'Canvas støttes ikke i nettleseren din.',
  hintAce: 'Ess: plasser en brikke på brettet eller flytt 1 steg fremover.',
  hintFour:
    'Fire: flytt 4 steg bakover.\nTips: bruk dette i startruten din for å ta snarveien over hele brettet.',
  hintSeven:
    'Sju: gjør totalt 7 steg med én eller to brikker.\n Velg først kortet, deretter eventuelt en annen brikke. Merk: brikke 1 beveger seg alltid først.',
  hintJack:
    'Knekt: bytt brikken din med en fiendtlig brikke.\nDu kan bare velge en egnet fiende etter at du har valgt dette kortet.',
  hintQueen: 'Dame: flytt 12 steg fremover.',
  hintKing: 'Konge: plasser en brikke på brettet.',
  rulesButton: 'Spilleregler',
  rulesTitle: 'Spilleregler',
  rulesGettingOnBoard: 'Komme på brettet',
  rulesSpecialCards: 'Spesialkort',
  rulesClickToClose: 'Klikk hvor som helst for å lukke',
  moveRejectedTitle: 'Du kan ikke gjøre det trekket',
  moveRejectedGeneric: 'Det trekket er ikke tillatt.',
  moveRejectedNotYourTurn: 'Det er ikke din tur (eller trekket er allerede spilt).',
  moveRejectedInvalidSelection: 'Velg en brikke og et kort først.',
  moveRejectedDontHaveCard: 'Du har ikke det kortet.',
  moveRejectedWrongCardForMove: 'Du kan ikke bruke det kortet til dette trekket.',
  moveRejectedPawnOnNest:
    'Den brikken er fortsatt i redet — få den ut på brettet med et ess eller en konge først.',
  moveRejectedPawnNotOnNest: 'Den brikken er allerede på brettet.',
  moveRejectedPawnNotOnBoard: 'Du kan bare bytte brikker som er på brettet.',
  moveRejectedNotYourPawn: 'Den brikken er ikke din.',
  moveRejectedDestinationOccupiedByOwnPawn: 'En av dine egne brikker står allerede på den ruten.',
  moveRejectedDestinationBlocked: 'Du kan ikke flytte dit — veien er blokkert.',
  moveRejectedStartTileOccupied: 'Startruten din er opptatt, så du kan ikke komme ut på brettet.',
  moveRejectedCannotPassStartTile:
    'Du kan ikke passere en annen spillers brikke på startruten deres.',
  moveRejectedCannotSwitchOpponentOnOwnStart:
    'Du kan ikke bytte med en motstander som står på sin egen startrute.',
  moveRejectedCannotSwitchOwnPawns: 'Du kan ikke bytte to av dine egne brikker.',
  moveRejectedMustMoveExactSteps: 'Du kan bare flytte %s steg med denne brikken.',
  moveRejectedPawnClosedInFinish: 'Den brikken er stengt inne på mål og kan ikke flytte.',
  moveRejectedSplitNeedsTwoOwnPawns: 'En 7-deling krever to av dine egne brikker.',
  moveRejectedSplitStepsNotSeven: 'De to delene av 7-eren må til sammen bli 7 steg.',
};