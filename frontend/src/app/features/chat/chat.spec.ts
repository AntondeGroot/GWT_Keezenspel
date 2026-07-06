import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ChatPanel } from './chat';
import { ChatService } from './chat.service';
import { GameStore } from '../../game-store';

class FakeEventSource {
  onmessage: ((e: MessageEvent) => void) | null = null;
  onerror: (() => void) | null = null;
  closed = false;
  constructor(public url: string) {}
  close() {
    this.closed = true;
  }
}

const q = (f: ComponentFixture<ChatPanel>, id: string): HTMLElement | null =>
  f.nativeElement.querySelector(`[data-testid="${id}"]`);

describe('ChatPanel', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    vi.stubGlobal('EventSource', FakeEventSource);
    document.cookie = 'sessionid=room-key';
    document.cookie = 'playerid=0';
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    // Viewer is player 0 → "Ann"; the panel should send under that name.
    TestBed.inject(GameStore).players.set([
      { id: '0', name: 'Ann' },
      { id: '1', name: 'Bob' },
    ]);
  });

  afterEach(() => {
    http.verify();
    vi.unstubAllGlobals();
    document.cookie = 'sessionid=;max-age=0';
    document.cookie = 'playerid=;max-age=0';
  });

  function render(): ComponentFixture<ChatPanel> {
    const f = TestBed.createComponent(ChatPanel);
    f.detectChanges(); // runs ngOnInit → chat.connect (stubbed stream)
    return f;
  }

  it('renders nothing while the chat server is offline', () => {
    const f = render();
    expect(q(f, 'chat-send')).toBeNull();
    expect(q(f, 'chat-input')).toBeNull();
    expect(q(f, 'chat-display')).toBeNull();
  });

  it('shows input, send button and display once the stream is up', () => {
    const f = render();
    TestBed.inject(ChatService).ingest('[]'); // an empty array = server is up
    f.detectChanges();
    expect(q(f, 'chat-send')).not.toBeNull();
    expect(q(f, 'chat-input')).not.toBeNull();
    expect(q(f, 'chat-display')).not.toBeNull();
  });

  it('sends the typed message under the viewer’s name and clears the input', () => {
    const f = render();
    TestBed.inject(ChatService).ingest('[]');
    f.detectChanges();

    const input = q(f, 'chat-input') as HTMLInputElement;
    input.value = 'hello';
    (q(f, 'chat-send') as HTMLButtonElement).click();

    const req = http.expectOne('/chat/room-key');
    expect(req.request.method).toBe('POST');
    expect((req.request.body as { sender: string }).sender).toBe('Ann');
    req.flush({});
    expect(input.value).toBe('');
  });
});