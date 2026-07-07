import { Component, effect, inject } from '@angular/core';
import { Translations } from '../../../i18n/translations.service';
import { TeamHandoff } from './team-handoff.service';

/**
 * A one-shot, self-dismissing announcement that the viewer may now play their teammate's
 * pawns (shown once their own are all home). Wood/gold to match the game; click to dismiss
 * early. The message is supplied by the board via the TeamHandoff service.
 */
@Component({
  selector: 'app-team-handoff',
  templateUrl: './team-handoff.html',
  styleUrl: './team-handoff.scss',
})
export class TeamHandoffPopup {
  protected readonly i18n = inject(Translations);
  protected readonly handoff = inject(TeamHandoff);
  private timer?: ReturnType<typeof setTimeout>;

  constructor() {
    // Auto-dismiss a few seconds after it appears (re-armed whenever a new message shows).
    effect(() => {
      const message = this.handoff.message();
      clearTimeout(this.timer);
      if (message) {
        this.timer = setTimeout(() => this.handoff.close(), 5000);
      }
    });
  }
}
