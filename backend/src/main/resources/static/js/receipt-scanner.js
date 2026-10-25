import { showToast, openModal, closeModal } from './ui-utils.js';

const money = (paise) => `₹${(paise / 100).toFixed(2)}`;
const escapeText = (value) => String(value ?? '').replace(/[&<>"']/g, ch => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[ch]));

export function initReceiptScanner({ getMembers, getGroupId, createExpense, onSaved }) {
  const $ = (id) => document.getElementById(id);
  const openButton = $('btn-open-receipt-scanner');
  const fileInput = $('receipt-image-input');
  const status = $('receipt-scan-status');
  const itemsHost = $('receipt-items-list');
  const qrResult = $('receipt-qr-result');
  let items = [];
  let nextId = 1;

  const setStatus = (message) => { if (status) status.textContent = message; };
  const memberOptions = (selectedId = '') => (getMembers() || []).map(m =>
    `<option value="${m.id}" ${String(m.id) === String(selectedId) ? 'selected' : ''}>${escapeText(m.displayName || m.name || `Member ${m.id}`)}</option>`
  ).join('');

  function renderItems() {
    itemsHost.innerHTML = '';
    const members = getMembers() || [];
    if (!items.length) {
      itemsHost.innerHTML = '<p class="form-hint">No items detected yet. Upload a clear receipt image or add an item manually.</p>';
    }
    items.forEach((item) => {
      const row = document.createElement('div');
      row.className = 'card receipt-item-row';
      row.style.cssText = 'padding:0.85rem;display:grid;grid-template-columns:minmax(120px,2fr) minmax(90px,1fr) auto;gap:0.65rem;align-items:start;';
      row.innerHTML = `
        <div class="form-group" style="margin:0;">
          <label class="form-label">Item</label>
          <input class="form-control" data-field="name" value="${escapeText(item.name)}" aria-label="Item name">
        </div>
        <div class="form-group" style="margin:0;">
          <label class="form-label">Price (₹)</label>
          <input class="form-control" data-field="price" type="number" min="0.01" step="0.01" value="${Number(item.price || 0).toFixed(2)}" aria-label="Item price">
        </div>
        <button type="button" class="btn btn-danger btn-sm" data-remove="${item.id}" style="align-self:end;">Remove</button>
        <div style="grid-column:1/-1;">
          <span class="form-label">Who had this item?</span>
          <div style="display:flex;gap:0.5rem 1rem;flex-wrap:wrap;margin-top:0.35rem;">
            ${members.map(m => `<label style="display:inline-flex;gap:0.35rem;align-items:center;font-size:0.875rem;"><input type="checkbox" data-member="${m.id}" ${item.memberIds.includes(Number(m.id)) ? 'checked' : ''}>${escapeText(m.displayName || m.name || `Member ${m.id}`)}</label>`).join('')}
          </div>
        </div>`;
      row.querySelector('[data-field="name"]').addEventListener('input', e => { item.name = e.target.value; });
      row.querySelector('[data-field="price"]').addEventListener('input', e => { item.price = Number(e.target.value); updateTotal(); });
      row.querySelector('[data-remove]').addEventListener('click', () => { items = items.filter(i => i.id !== item.id); renderItems(); });
      row.querySelectorAll('[data-member]').forEach(cb => cb.addEventListener('change', () => {
        const id = Number(cb.dataset.member);
        item.memberIds = cb.checked ? [...new Set([...item.memberIds, id])] : item.memberIds.filter(x => x !== id);
      }));
      itemsHost.appendChild(row);
    });
    updateTotal();
  }

  function updateTotal() {
    const total = items.reduce((sum, item) => sum + Math.round((Number(item.price) || 0) * 100), 0);
    $('receipt-items-total').textContent = money(total);
  }

  function addItem(name = '', price = 0) {
    const members = getMembers() || [];
    items.push({ id: nextId++, name, price, memberIds: [] });
    renderItems();
  }

  function parseReceiptText(text) {
    const lines = text.split(/\r?\n/).map(s => s.trim()).filter(Boolean);
    const found = [];
    for (const line of lines) {
      // Accept common receipt formats: "Coffee 120.00", "Coffee ₹120.00", "Coffee  x1  120.00"
      const match = line.match(/^(.*?)(?:\s{1,}|[₹$]\s*)(?:x\s*\d+\s*)?(\d{1,7}(?:[.,]\d{2})?)\s*$/i);
      if (!match) continue;
      const name = match[1].replace(/[|*•]+/g, '').replace(/\s*x\s*\d+\s*$/i, '').trim();
      const price = Number(match[2].replace(',', '.'));
      if (name.length < 2 || !Number.isFinite(price) || price <= 0) continue;
      if (/^(subtotal|total|grand total|tax|gst|cgst|sgst|discount|change|cash|card|upi|amount paid|balance due)$/i.test(name)) continue;
      found.push({ name: name.slice(0, 90), price });
    }
    return found.slice(0, 60);
  }

  async function decodeQr(image) {
    if (!window.jsQR) return null;
    try {
      const bitmap = await createImageBitmap(image);
      const canvas = document.createElement('canvas');
      canvas.width = bitmap.width;
      canvas.height = bitmap.height;
      const ctx = canvas.getContext('2d', { willReadFrequently: true });
      ctx.drawImage(bitmap, 0, 0);
      const result = window.jsQR(ctx.getImageData(0, 0, canvas.width, canvas.height).data, canvas.width, canvas.height);
      return result?.data || null;
    } catch (err) {
      return null;
    }
  }

  async function scanFile(file) {
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      showToast('Please choose a receipt image.', 'error');
      return;
    }
    setStatus('Checking the image for a QR code…');
    qrResult.style.display = 'none';
    const qrText = await decodeQr(file);
    if (qrText) {
      qrResult.style.display = 'block';
      // Treat decoded data as untrusted text; never automatically navigate to it or pay.
      qrResult.textContent = `QR data detected: ${qrText.slice(0, 500)}`;
    }
    if (!window.Tesseract) {
      setStatus('OCR library could not load. You can still add items manually.');
      showToast('OCR library did not load. Add items manually or try again with internet access.', 'error');
      return;
    }
    try {
      setStatus('Reading receipt text with OCR. This can take a little while…');
      const result = await window.Tesseract.recognize(file, 'eng', {
        logger: message => {
          if (message.status === 'recognizing text' && Number.isFinite(message.progress)) {
            setStatus(`Reading receipt… ${Math.round(message.progress * 100)}%`);
          }
        }
      });
      const text = result?.data?.text || '';
      const parsed = parseReceiptText(text);
      const lines = text.split(/\r?\n/).map(s => s.trim()).filter(Boolean);
      if (!$('receipt-merchant').value.trim()) {
        const likelyMerchant = lines.find(line => line.length >= 3 && line.length < 70 && !/\d{3,}/.test(line));
        $('receipt-merchant').value = likelyMerchant || 'Scanned receipt';
      }
      if (parsed.length) {
        const members = getMembers() || [];
        items = parsed.map(item => ({
          id: nextId++, name: item.name, price: item.price,
          memberIds: []
        }));
        renderItems();
        setStatus(`Detected ${parsed.length} possible item(s). Please verify names, prices, and assignments before saving.`);
      } else {
        setStatus('Could not confidently identify item lines. Add or edit the items manually; OCR text is not guaranteed to be accurate.');
        showToast('No clear item-price pairs found. Please enter the items manually.', 'info');
      }
    } catch (err) {
      console.error('Receipt OCR failed:', err);
      setStatus('Could not read this image. Try a clearer, well-lit photo or add items manually.');
      showToast('Receipt OCR failed. Try a clearer photo.', 'error');
    }
  }

  async function saveItems() {
    const groupId = getGroupId();
    const members = getMembers() || [];
    const payerId = Number($('receipt-paid-by').value);
    const description = $('receipt-merchant').value.trim() || 'Receipt item';
    if (!groupId || !members.length) return showToast('Open a group with members before scanning a receipt.', 'error');
    if (!payerId) return showToast('Choose who paid for the receipt.', 'error');
    if (!items.length) return showToast('Add at least one receipt item.', 'error');

    const prepared = [];
    for (const item of items) {
      const name = item.name.trim();
      const amountPaise = Math.round(Number(item.price) * 100);
      const assigned = [...new Set(item.memberIds)].filter(id => members.some(m => Number(m.id) === id));
      if (!name || !Number.isFinite(amountPaise) || amountPaise <= 0 || !assigned.length) {
        return showToast('Every item needs a name, a positive price, and at least one assigned member.', 'error');
      }
      const base = Math.floor(amountPaise / assigned.length);
      let remainder = amountPaise - base * assigned.length;
      const shares = assigned.map(memberId => ({
        memberId,
        sharePaise: base + (remainder-- > 0 ? 1 : 0)
      }));
      prepared.push({
        description: `${description} — ${name}`.slice(0, 250),
        amountPaise,
        paidByMemberId: payerId,
        splitMethod: 'EXACT',
        shares
      });
    }
    const button = $('receipt-save-items');
    button.disabled = true;
    button.textContent = 'Saving…';
    let saved = 0;
    try {
      for (const payload of prepared) {
        await createExpense(groupId, payload);
        saved++;
      }
      closeModal('modal-receipt-scanner');
      showToast(`Saved ${saved} itemized expense(s). Review the group totals.`, 'success');
      items = [];
      $('receipt-merchant').value = '';
      fileInput.value = '';
      qrResult.style.display = 'none';
      await onSaved?.();
    } catch (err) {
      showToast(`Saved ${saved} of ${prepared.length} items before an error: ${err.message || err}`, 'error');
      if (saved > 0) await onSaved?.();
    } finally {
      button.disabled = false;
      button.textContent = 'Save itemized expenses';
    }
  }

  openButton?.addEventListener('click', () => {
    const members = getMembers() || [];
    if (!getGroupId() || !members.length) {
      showToast('Open a group and add members before scanning a receipt.', 'error');
      return;
    }
    $('receipt-paid-by').innerHTML = memberOptions(members[0]?.id);
    items = [];
    $('receipt-merchant').value = '';
    fileInput.value = '';
    qrResult.style.display = 'none';
    setStatus('Upload a clear receipt image. You can also add items manually.');
    renderItems();
    openModal('modal-receipt-scanner');
  });
  fileInput?.addEventListener('change', () => scanFile(fileInput.files?.[0]));
  $('receipt-add-item')?.addEventListener('click', () => addItem());
  $('receipt-save-items')?.addEventListener('click', saveItems);
}
