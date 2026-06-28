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
  private readonly sessionId = 'ae23a3b3-1ab2-4539-8cc1-13503e4c3c8f';
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
  protected readonly tiles = computed(() => {
    const g = this.geometry();
    const s = this.state();
    if (!g || !s?.players) return [];
    const colorOf = (playerId: string) =>
      s.players!.find((p) => p.id === playerId)?.color || '#f2f2f2';
    return g.tiles.map((t) => ({
      ...t,
      color: t.tileNr <= 0 || t.tileNr >= 16 ? colorOf(t.playerId) : '#f2f2f2',
    }));
  });
  protected readonly cell  = computed(() => this.geometry()?.cellDistance ?? 0);
}
