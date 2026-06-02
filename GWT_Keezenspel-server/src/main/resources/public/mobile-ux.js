/**
 * mobile-ux.js — loaded by mobile.html only.
 *
 * Adds +/− stepper buttons around the pawn-step text inputs so that
 * the split-card controls are usable with a finger without typing.
 *
 * The pawnIntegerBoxes table is built by GWT after the module loads,
 * so this script polls until the element appears, then enhances it.
 * Visibility is inherited automatically: when GWT hides/shows the
 * table the +/− buttons follow, because they live inside the same table.
 */
(function () {
  'use strict';

  /* ── Insert +/− buttons into the pawnIntegerBoxes table ───────────── */

  function init() {
    var table = document.getElementById('pawnIntegerBoxes');
    if (!table || table.dataset.mobileEnhanced) return false;
    table.dataset.mobileEnhanced = 'true';

    var inputs = table.querySelectorAll('input');
    if (inputs.length < 2) return false;

    // Enhance pawn-1 input (always enabled) and pawn-2 input (may be disabled).
    addSteppers(table, inputs[0]);
    addSteppers(table, inputs[1]);

    // Keep the [data-disabled] CSS hint in sync so the dim style applies.
    syncDisabledHint(table);
    // Re-sync whenever GWT shows the container (it may enable pawn-2 then).
    new MutationObserver(function () {
      syncDisabledHint(table);
    }).observe(table, { attributes: true, attributeFilter: ['style'] });

    return true;
  }

  /**
   * Inserts a '−' <td> before the input's <td> and a '+' <td> after it.
   *
   * Original row:  [Pawn N label] [input td] …
   * After:         [Pawn N label] [− td] [input td] [+ td] …
   */
  function addSteppers(table, input) {
    var inputTd = input.parentNode;   // <td> wrapping the GWT TextBox
    var tr      = inputTd.parentNode; // <tr>

    var minusTd = document.createElement('td');
    minusTd.appendChild(makeBtn('−', input));
    tr.insertBefore(minusTd, inputTd);

    var plusTd = document.createElement('td');
    plusTd.appendChild(makeBtn('+', input));
    // insertBefore(node, null) appends — safe when inputTd is the last child.
    tr.insertBefore(plusTd, inputTd.nextElementSibling);
  }

  function makeBtn(sign, input) {
    var btn = document.createElement('button');
    btn.type      = 'button';
    btn.className = 'pawn-step-btn';
    btn.textContent = sign;
    btn.dataset.forInput = input.className; // for debugging
    btn.addEventListener('click', function () {
      if (input.disabled) return;
      var val  = parseInt(input.value, 10) || 0;
      var next = (sign === '+') ? (val >= 7 ? 0 : val + 1) : (val <= 0 ? 7 : val - 1);
      input.value = String(next);
      // Fire a native change event so GWT's addChangeHandler callback runs.
      input.dispatchEvent(new Event('change', { bubbles: true }));
    });
    return btn;
  }

  /** Adds data-disabled="true" to any button whose paired input is disabled. */
  function syncDisabledHint(table) {
    var inputs = table.querySelectorAll('input');
    var btns   = table.querySelectorAll('.pawn-step-btn');
    // btns order: [−1, +1, −2, +2]  (two per input)
    var btnsPerInput = btns.length / inputs.length; // typically 2
    inputs.forEach(function (input, i) {
      for (var b = i * btnsPerInput; b < (i + 1) * btnsPerInput; b++) {
        if (btns[b]) btns[b].dataset.disabled = input.disabled ? 'true' : 'false';
      }
    });
  }

  /* ── Hoist leaveGameButton to <body> ──────────────────────────────────
     The button lives inside .buttonContainer, which has transform:translateX.
     A CSS transform on an ancestor turns it into the containing block for
     position:fixed descendants, so the button would be fixed relative to the
     container instead of the viewport.  Moving it to <body> restores normal
     fixed-to-viewport behaviour so the regular CSS (top:18px; left:16px)
     places it in the title bar exactly as on desktop. */
  function hoistLeaveGameButton() {
    var btn = document.querySelector('.leaveGameButton');
    if (!btn || btn.dataset.hoisted) return false;
    btn.dataset.hoisted = 'true';
    document.body.appendChild(btn);
    return true;
  }

  /* ── Chat icon: inject into the buttonContainer flex row ──────────── */

  var CHAT_SVG = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 115.98 122.88">'
    + '<path d="M17.2,0h59.47c4.73,0,9.03,1.93,12.15,5.05c3.12,3.12,5.05,7.42,5.05,12.15v38.36'
    + 'c0,4.73-1.93,9.03-5.05,12.15c-3.12,3.12-7.42,5.05-12.15,5.05H46.93L20.81,95.21'
    + 'c-1.21,1.04-3.04,0.9-4.08-0.32c-0.51-0.6-0.74-1.34-0.69-2.07l1.39-20.07H17.2'
    + 'c-4.73,0-9.03-1.93-12.15-5.05C1.93,64.59,0,60.29,0,55.56V17.2c0-4.73,1.93-9.03,5.05-12.15'
    + 'C8.16,1.93,12.46,0,17.2,0L17.2,0z M102.31,27.98c3.37,0.65,6.39,2.31,8.73,4.65'
    + 'c3.05,3.05,4.95,7.26,4.95,11.9v38.36c0,4.64-1.89,8.85-4.95,11.9c-3.05,3.05-7.26,4.95-11.9,4.95'
    + 'h-0.61l1.42,20.44l0,0c0.04,0.64-0.15,1.3-0.6,1.82c-0.91,1.07-2.52,1.19-3.58,0.28'
    + 'l-26.22-23.2H35.01l17.01-17.3h36.04c7.86,0,14.3-6.43,14.3-14.3V29.11'
    + 'C102.35,28.73,102.34,28.35,102.31,27.98L102.31,27.98z M25.68,43.68c-1.6,0-2.9-1.3-2.9-2.9'
    + 'c0-1.6,1.3-2.9,2.9-2.9h30.35c1.6,0,2.9,1.3,2.9,2.9c0,1.6-1.3,2.9-2.9,2.9H25.68L25.68,43.68z'
    + 'M25.68,29.32c-1.6,0-2.9-1.3-2.9-2.9c0-1.6,1.3-2.9,2.9-2.9H68.7c1.6,0,2.9,1.3,2.9,2.9'
    + 'c0,1.6-1.3,2.9-2.9,2.9H25.68L25.68,29.32z M76.66,5.8H17.2c-3.13,0-5.98,1.28-8.05,3.35'
    + 'C7.08,11.22,5.8,14.06,5.8,17.2v38.36c0,3.13,1.28,5.98,3.35,8.05c2.07,2.07,4.92,3.35,8.05,3.35'
    + 'h3.34v0.01l0.19,0.01c1.59,0.11,2.8,1.49,2.69,3.08l-1.13,16.26L43.83,67.8'
    + 'c0.52-0.52,1.24-0.84,2.04-0.84h30.79c3.13,0,5.98-1.28,8.05-3.35c2.07-2.07,3.35-4.92,3.35-8.05'
    + 'V17.2c0-3.13-1.28-5.98-3.35-8.05C82.65,7.08,79.8,5.8,76.66,5.8L76.66,5.8z"/>'
    + '</svg>';

  function initChatIcon() {
    // Chat icon is mobile-only; mobile.html loads mobile.css, index.html does not.
    if (!document.querySelector('link[href*="mobile.css"]')) return true;
    var tbody = document.querySelector('.buttonContainer > tbody');
    if (!tbody || tbody.dataset.chatEnhanced) return false;
    tbody.dataset.chatEnhanced = 'true';

    /* ── Inject icon row as the leftmost item in the button flex row ── */
    var tr = document.createElement('tr');
    tr.id = 'mobile-chat-icon-tr';
    var td = document.createElement('td');
    var btn = document.createElement('button');
    btn.id   = 'mobile-chat-icon-btn';
    btn.type = 'button';
    btn.innerHTML = CHAT_SVG + '<span id="mobile-chat-badge"></span>';
    td.appendChild(btn);
    tr.appendChild(td);
    tbody.insertBefore(tr, tbody.firstChild);

    /* ── Standalone popup — always a direct child of <body> ─────────────
       We do NOT reuse the GWT chatContainer because it lives inside
       .column2 (position:fixed; z-index:50), which creates a stacking
       context that traps any descendant regardless of its own z-index.
       Instead we create a new popup div here, mirror content from the
       hidden GWT textarea, and route send via the hidden GWT widgets. */
    var popup = document.createElement('div');
    popup.id = 'mobile-chat-popup';
    popup.innerHTML =
        '<div id="mobile-chat-messages"></div>'
      + '<div id="mobile-chat-input-row">'
      +   '<input id="mobile-chat-input" type="text" autocomplete="off">'
      +   '<button id="mobile-chat-send" type="button">&#10148;</button>'
      + '</div>';
    document.body.appendChild(popup);

    var backdrop = document.createElement('div');
    backdrop.id = 'mobile-chat-backdrop';
    document.body.appendChild(backdrop);

    var unread = 0;
    var popupOpen = false;

    function updateBadge() {
      var badge = document.getElementById('mobile-chat-badge');
      if (!badge) return;
      if (unread > 0) {
        badge.textContent = unread > 9 ? '9+' : String(unread);
        badge.style.display = 'flex';
      } else {
        badge.style.display = 'none';
      }
    }

    function syncMessages() {
      var ta = document.querySelector('.chatDisplayField');
      var display = document.getElementById('mobile-chat-messages');
      if (!ta || !display) return;
      display.textContent = ta.value;
      display.scrollTop = display.scrollHeight;
    }

    function openPopup() {
      syncMessages();
      popupOpen = true;
      unread = 0;
      updateBadge();
      popup.style.display = 'flex';
      backdrop.style.display = 'block';
      document.getElementById('mobile-chat-input').focus();
    }

    function closePopup() {
      popupOpen = false;
      popup.style.display = 'none';
      backdrop.style.display = 'none';
    }

    btn.addEventListener('click', openPopup);
    backdrop.addEventListener('click', closePopup);

    /* Send: write into the hidden GWT input and click the hidden send button */
    document.getElementById('mobile-chat-send').addEventListener('click', function () {
      var mobileInput = document.getElementById('mobile-chat-input');
      var gwtInput    = document.querySelector('.chatInputField');
      var gwtSend     = document.querySelector('.chatSendButton');
      if (!mobileInput || !gwtInput || !gwtSend) return;
      var text = mobileInput.value.trim();
      if (!text) return;
      gwtInput.value = text;
      gwtSend.click();
      mobileInput.value = '';
    });

    /* Also send on Enter key */
    document.getElementById('mobile-chat-input').addEventListener('keydown', function (e) {
      if (e.key === 'Enter') document.getElementById('mobile-chat-send').click();
    });

    /* Monitor GWT textarea for new messages (polls every 600 ms) */
    var lastLineCount = 0;
    setInterval(function () {
      var ta = document.querySelector('.chatDisplayField');
      if (!ta) return;
      var lineCount = (ta.value.match(/\n/g) || []).length;
      if (lineCount > lastLineCount) {
        if (popupOpen) {
          syncMessages();
        } else {
          unread += lineCount - lastLineCount;
          updateBadge();
        }
        lastLineCount = lineCount;
      }
    }, 600);

    return true;
  }

  /* ── Content grid: physically move cards/hint/playerList into a real div ─
     display:contents on <tr>/<td> is unreliable in iOS Safari — table rows
     still generate boxes and create phantom gaps.  We move the three target
     elements out of GWT's nested tables into a plain div grid appended inside
     the same td as the canvas, so CSS Grid works without browser quirks.   */

  function initMobileGrid() {
    // Mobile-only: mobile.css is loaded by mobile.html, not by index.html.
    if (!document.querySelector('link[href*="mobile.css"]')) return true;
    var anonVPTd = document.querySelector(
        '.columnWrapper > tbody > tr > td:first-child');
    if (!anonVPTd || anonVPTd.dataset.gridDone) return false;

    var cards      = document.querySelector('.cardsContainer');
    var hint       = document.querySelector('.cardHintLabel');
    var playerList = document.querySelector('.playerListContainer');
    if (!cards || !hint || !playerList) return false;

    anonVPTd.dataset.gridDone = 'true';

    /* Left column: cardsContainer stacked above cardHintLabel */
    var leftCol = document.createElement('div');
    leftCol.id = 'mobile-left-col';
    leftCol.appendChild(cards);
    leftCol.appendChild(hint);

    /* Two-column grid: left col on the left, playerList on the right */
    var grid = document.createElement('div');
    grid.id = 'mobile-content-grid';
    grid.appendChild(leftCol);
    grid.appendChild(playerList);

    /* Append the grid to the same td that holds the canvas — it stacks
       below the canvas because table-cell content flows vertically.      */
    anonVPTd.appendChild(grid);

    return true;
  }

  /* ── Poll until GWT has rendered the pawnIntegerBoxes element ──────── */
  var poll = setInterval(function () {
    var pawnsDone = init();
    var chatDone  = initChatIcon();
    var gridDone  = initMobileGrid();
    hoistLeaveGameButton();
    if (pawnsDone && chatDone && gridDone) clearInterval(poll);
  }, 250);

})();