import { Component, Input } from '@angular/core';

/**
 * A single pawn, rendered as the (ported) pawn.svg. The owner's colour is passed
 * in and exposed to the SVG as the CSS variable `--pawn-color`; the template uses
 * it directly for head/body and a `color-mix`-darkened shade for collar/base
 * (mirrors the GWT client's main colour + 0.6 darken).
 */
@Component({
  selector: 'app-pawn',
  templateUrl: './pawn.html',
  styleUrl: './pawn.scss',
  host: {
    '[style.--pawn-color]': 'color',
  },
})
export class Pawn {
  @Input() color = '#888888';
}