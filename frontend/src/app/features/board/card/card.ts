import { Component, Input } from '@angular/core';

/**
 * A single playing card, drawn from the card-deck.png sprite. suit (0-3, row)
 * and value (1=ace..13, column) are exposed to CSS as --card-col / --card-row,
 * which pick the cell out of the sheet. One element per card (the parent tracks
 * by uuid), so the same element can later transition from hand to pile.
 */
@Component({
  selector: 'app-card',
  templateUrl: './card.html',
  styleUrl: './card.scss',
  host: {
    '[style.--card-col]': 'value - 1',
    '[style.--card-row]': 'suit',
  },
})
export class Card {
  @Input() suit = 0;
  @Input() value = 1;
}