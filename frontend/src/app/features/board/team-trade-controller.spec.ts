import { signal } from '@angular/core';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Card as CardModel, GameStatePush } from '../../api';
import { TeamTradeController } from './team-trade-controller';

const card = (uuid: number, value: number): CardModel => ({ uuid, suit: 0, value });
const players = () => [
  { id: 'me', name: 'Me', teamId: 1 },
  { id: 'mate', name: 'Mate', teamId: 1 },
];
// A translate stub that echoes the key + any args, so assertions read clearly.
const t = (key: string, ...args: (string | number)[]) =>
  args.length ? `${key}(${args.join(',')})` : key;

describe('TeamTradeController', () => {
  // Real signals so the controller's computeds invalidate exactly as they do in the board.
  const state = signal<GameStatePush | undefined>(undefined);
  const hand = signal<CardModel[]>([]);
  let onSwap: ReturnType<typeof vi.fn>;
  let notify: ReturnType<typeof vi.fn>;
  let ctl: TeamTradeController;

  const build = (viewerId: string) =>
    new TeamTradeController(
      () => state(),
      () => hand(),
      viewerId,
      onSwap as never,
      notify as never,
      t as never,
    );

  beforeEach(() => {
    onSwap = vi.fn();
    notify = vi.fn();
    hand.set([]);
    state.set(undefined);
    ctl = build('me');
  });

  describe('canAsk', () => {
    it('is true in a team trade game with cards and no pending trade', () => {
      state.set({ teamCardTrade: true, players: players() } as GameStatePush);
      hand.set([card(1, 5)]);
      expect(ctl.canAsk()).toBe(true);
    });

    it('is false without the trade option, mid-trade, off-team, or with no cards', () => {
      hand.set([card(1, 5)]);
      state.set({ players: players() } as GameStatePush); // no teamCardTrade
      expect(ctl.canAsk()).toBe(false);

      state.set({
        teamCardTrade: true,
        trade: { requesterId: 'x' },
        players: players(),
      } as GameStatePush);
      expect(ctl.canAsk()).toBe(false);

      state.set({ teamCardTrade: true, players: [{ id: 'me' }] } as GameStatePush); // no teamId
      expect(ctl.canAsk()).toBe(false);

      state.set({ teamCardTrade: true, players: players() } as GameStatePush);
      hand.set([]);
      expect(ctl.canAsk()).toBe(false);
    });
  });

  it('names the other party from the viewer’s perspective', () => {
    state.set({
      players: players(),
      trade: { requesterId: 'me', teammateId: 'mate' },
    } as GameStatePush);
    expect(ctl.otherName()).toBe('Mate');
    expect(build('mate').otherName()).toBe('Me');
  });

  it('toggles the offering flag', () => {
    expect(ctl.offering()).toBe(false);
    ctl.ask();
    expect(ctl.offering()).toBe(true);
    ctl.stopOffering();
    expect(ctl.offering()).toBe(false);
  });

  // Drive a resolving trade: first push has the trade pending (baseline), the second has it gone.
  const resolveTrade = (c: TeamTradeController, endHand: CardModel[], startHand = [card(1, 5)]) => {
    state.set({
      players: players(),
      trade: { requesterId: 'me', teammateId: 'mate' },
    } as GameStatePush);
    hand.set(startHand);
    c.reactToOutcome(); // baseline: remembers the pending trade + hand
    state.set({ players: players() } as GameStatePush); // trade resolved
    hand.set(endHand);
    c.reactToOutcome();
  };

  describe('reactToOutcome', () => {
    it('accepted (requester): animates the swap and shows the received-card banner', () => {
      resolveTrade(ctl, [card(2, 1)]); // gave #1, got an Ace (#2)
      expect(onSwap).toHaveBeenCalledWith('mate', card(2, 1), card(1, 5));
      expect(notify).toHaveBeenCalledWith('tradeGotTitle', 'tradeGotAceMessage(Mate)');
    });

    it('rejected (requester): no swap, shows the rejected banner (hand unchanged)', () => {
      resolveTrade(ctl, [card(1, 5)]); // hand identical → nothing given/received
      expect(onSwap).not.toHaveBeenCalled();
      expect(notify).toHaveBeenCalledWith('tradeRejectedTitle', 'tradeRejectedMessage(Mate)');
    });

    it('cancelled by me: silent (no swap, no banner)', () => {
      ctl.cancelledByMe();
      resolveTrade(ctl, [card(2, 13)]);
      expect(onSwap).not.toHaveBeenCalled();
      expect(notify).not.toHaveBeenCalled();
    });

    it('teammate side: animates the swap but shows no banner (only the requester is told)', () => {
      const mate = build('mate');
      resolveTrade(mate, [card(2, 13)]); // teammate gave #1, got a King (#2)
      expect(onSwap).toHaveBeenCalledWith('me', card(2, 13), card(1, 5));
      expect(notify).not.toHaveBeenCalled();
    });
  });
});
