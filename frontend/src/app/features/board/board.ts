import { Component, signal, inject, computed, OnInit, OnDestroy } from '@angular/core';
import { GameStatePush, MovesService } from '../../api';
import { buildBoard } from './board-geometry';
import { resolveGameSession } from '../../session';
import { Pawn } from './pawn/pawn';
import { Card } from './card/card';
import {highlightForPawn1, highlightForPawn2} from './pawn-highlight';

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
  private readonly movesService = inject(MovesService);
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
      return {x: pt.x, y: pt.y, zIndex: Math.round(pt.y), color: colorOfPawn, id: pawnId}}).filter(x => x !== null);
  })
  protected readonly cell  = computed(() => this.geometry()?.cellDistance ?? 0);
  private eventSource?: EventSource;

  protected readonly hand = computed(() => this.state()?.playerCards ?? []);

  protected readonly selectedCardUuid = signal<number | undefined>(undefined);
  protected readonly selectedPawnId = signal<string | undefined>(undefined);

  protected selectCard(uuid: number): void {
    this.selectedCardUuid.set(uuid);
    this.tryMove();}
  protected selectPawn(id: string): void {
    this.selectedPawnId.set(id);
    this.tryMove()};

  protected readonly highlightForPawn1 = highlightForPawn1;
  protected readonly highlightForPawn2 = highlightForPawn2;
  private tryMove(): void {
    const cardUuid = this.selectedCardUuid();
    const pawnId = this.selectedPawnId();
    if (cardUuid === undefined || pawnId === undefined) return;
    
    // check if the card and pawn still exist
    const card = this.hand().find((c) => c.uuid === cardUuid);
    const pawn = this.state()?.pawns?.find(
      (p) => `${p.pawnId.playerId}:${p.pawnId.pawnNr}` === pawnId,
    );
    if (!card || !pawn || !this.sessionId || !this.viewerId) return;

    const move = {
      playerId: this.viewerId,
      cardId: cardUuid,
      pawn1Id: pawn.pawnId,
      stepsPawn1: card.value,
      tempMessageType: 'MAKE_MOVE' as const,
    };

    this.movesService.makeMove(this.sessionId, this.viewerId, move).subscribe();
    this.selectedCardUuid.set(undefined);
    this.selectedPawnId.set(undefined);
  }
}
