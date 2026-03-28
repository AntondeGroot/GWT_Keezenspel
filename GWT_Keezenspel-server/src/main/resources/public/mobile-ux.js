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

  /* ── Poll until GWT has rendered the pawnIntegerBoxes element ──────── */
  var poll = setInterval(function () {
    if (init()) clearInterval(poll);
  }, 250);

})();