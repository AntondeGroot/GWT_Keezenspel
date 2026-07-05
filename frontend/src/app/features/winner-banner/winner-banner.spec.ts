import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WinnerBanner } from './winner-banner';
import { GameStore } from '../../game-store';
import { Player } from '../../api';

describe('WinnerBanner', () => {
  let fixture: ComponentFixture<WinnerBanner>;
  let store: GameStore;
  const banner = () => fixture.nativeElement.querySelector('.winner-banner') as HTMLElement | null;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [WinnerBanner] }).compileComponents();
    fixture = TestBed.createComponent(WinnerBanner);
    store = TestBed.inject(GameStore);
  });

  it('shows nothing when there are no winners', () => {
    store.players.set([{ id: 'p0', name: 'Aria' } as Player]);
    fixture.detectChanges();
    expect(banner()).toBeNull();
  });

  it('announces a new finisher with their medal and name', () => {
    store.players.set([{ id: 'p0', name: 'Aria', color: '#c0392b' } as Player]);
    fixture.detectChanges(); // first push: adopt winners=[] without announcing

    store.winners.set(['p0']);
    fixture.detectChanges();

    expect(banner()).not.toBeNull();
    expect(banner()!.querySelector('.winner-banner__medal')!.textContent).toBe('🥇');
    expect(banner()!.querySelector('.winner-banner__name')!.textContent!.trim()).toBe('Aria');
  });

  it('gives the second finisher the silver medal', () => {
    store.players.set([{ id: 'p0', name: 'Aria' } as Player, { id: 'p1', name: 'Bram' } as Player]);
    fixture.detectChanges();

    store.winners.set(['p0']);
    fixture.detectChanges();
    store.winners.set(['p0', 'p1']);
    fixture.detectChanges();

    expect(banner()!.querySelector('.winner-banner__medal')!.textContent).toBe('🥈');
    expect(banner()!.querySelector('.winner-banner__name')!.textContent!.trim()).toBe('Bram');
  });

  it('does not replay a banner for winners already present on the first push', () => {
    store.players.set([{ id: 'p0', name: 'Aria' } as Player]);
    store.winners.set(['p0']); // already finished before the component's first render
    fixture.detectChanges();
    expect(banner()).toBeNull();
  });

  // Regression: the store starts empty and the first real state (with an existing
  // winner) arrives a tick later over SSE — joining mid-game must stay silent, and
  // only a *subsequent* finish should announce.
  it('adopts a winner that arrives on the first real push, then announces later ones', () => {
    fixture.detectChanges(); // mounts with an empty store, before any SSE

    store.players.set([{ id: 'p0', name: 'Aria' } as Player, { id: 'p1', name: 'Bram' } as Player]);
    store.winners.set(['p0']); // first real push already has a finisher
    fixture.detectChanges();
    expect(banner(), 'joining a game with a prior winner must not replay it').toBeNull();

    store.winners.set(['p0', 'p1']); // a live finish this session
    fixture.detectChanges();
    expect(banner()).not.toBeNull();
    expect(banner()!.querySelector('.winner-banner__medal')!.textContent).toBe('🥈');
  });
});
