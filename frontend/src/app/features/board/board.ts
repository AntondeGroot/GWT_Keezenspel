import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { GamestatesService, GameState } from '../../api';
import { buildBoard } from './board-geometry';

@Component({
  selector: 'app-board',
  imports: [],
  templateUrl: './board.html',
  styleUrl: './board.scss',
})
export class Board implements OnInit{
  ngOnInit(): void {
    this.gamestateService.getGameStateForGame(this.sessionId)
    .subscribe((s) => this.state.set(s));
  }
  private readonly gamestateService = inject(GamestatesService);
  private readonly sessionId = '1031fc51-1e69-4f0a-8b1f-53648c3f6bae';
  private readonly viewerId = 'p0';

  protected readonly state = signal<GameState | undefined>(undefined);

  protected readonly geometry = computed(() => {
    const s = this.state();
    if (!s?.players) return undefined;
    return buildBoard(
      s.players.map((p) => ({ id: p.id, playerInt: p.playerInt! })),
      this.viewerId,
    );
  });
  protected readonly tiles = computed(() => this.geometry()?.tiles ?? []);
  protected readonly cell  = computed(() => this.geometry()?.cellDistance ?? 0);
}
