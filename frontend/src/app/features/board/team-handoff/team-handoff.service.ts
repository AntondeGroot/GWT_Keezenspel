import { Injectable, signal } from '@angular/core';

/**
 * Holds a one-shot team hand-off announcement ("you can now play your teammate's pawns"),
 * shown the moment the viewer's own pawns are all home. The board sets it; the popup (mounted
 * at the app root) renders it and it auto-dismisses. A tiny service so the two are decoupled,
 * mirroring MoveRejection.
 */
@Injectable({ providedIn: 'root' })
export class TeamHandoff {
  readonly message = signal<string | null>(null);

  show(message: string): void {
    this.message.set(message);
  }

  close(): void {
    this.message.set(null);
  }
}
