import { Component, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { GameStatePush } from '../../api';
import { buildBoard, pawnBox } from './board-geometry';
import { resolveGameSession } from '../../session';
import { Pawn } from './pawn/pawn';
import { Card } from './card/card';

@Component({
  selector: 'app-board',
  imports: [Pawn, Card],
  templateUrl: './board.html',
  styleUrl: './board.scss',
})
export class Board implements OnInit, OnDestroy{

  ngOnInit(): void {
    if(this.sessionId){
      this.eventSource = new EventSource(this.streamUrl);
      this.eventSource.addEventListener('gamestate', (event: MessageEvent) => {this.state.set(JSON.parse(event.data))});
    }
  }

  ngOnDestroy(): void {
    this.eventSource?.close();
  }

  private readonly session = resolveGameSession();
  private readonly sessionId = this.session.sessionId;
  private readonly viewerId = this.session.playerId;
  private readonly streamUrl = `/gamestates/${this.sessionId}/${this.viewerId}/stream`;
  protected readonly state = signal<GameStatePush | undefined>(undefined);
  protected readonly geometry = computed(() => {
    const s = this.state();
    const viewer = this.viewerId;
    if (!s?.players || !viewer) return undefined;
    return buildBoard(
      s.players.map((p) => ({ id: p.id, playerInt: p.playerInt! })),
      viewer,
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
  protected readonly pawns = computed(() => {
    const g = this.geometry();
    const s = this.state();
    if(!g || !s?.pawns) return [];
    const colorOf = (playerId: string) =>
      s.players!.find((p) => p.id === playerId)?.color || '#f2f2f2';
    return s.pawns.map((pawn) => {
      const tile = pawn.currentTileId;
      const pt = g.position(tile.playerId, tile.tileNr)
      const colorOfPawn = colorOf(pawn.playerId);
      const pawnId = `${pawn.pawnId.playerId}:${pawn.pawnId.pawnNr}`;
      if(!pt) return null;
      return {...pawnBox(pt), color:colorOfPawn, id: pawnId}}).filter(x => x !== null);
  })
  protected readonly cell  = computed(() => this.geometry()?.cellDistance ?? 0);
  private eventSource?: EventSource;

  protected readonly hand = computed(() => this.state()?.playerCards ?? []);
}
