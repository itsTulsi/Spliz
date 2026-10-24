/**
 * UI Utilities for FairShare Application
 */

/**
 * Format integer paise into standard Indian Rupee string (e.g. 10050 -> ₹100.50)
 * @param {number} paise 
 * @param {boolean} showSign 
 * @returns {string}
 */
export function formatCurrency(paise, showSign = false) {
  if (paise === null || paise === undefined || isNaN(paise)) return '₹0.00';
  const isNegative = paise < 0;
  const absPaise = Math.abs(paise);
  const rupees = Math.floor(absPaise / 100);
  const remainingPaise = absPaise % 100;
  
  // Format with Indian numbering system (lakhs/crores)
  const rupeesStr = rupees.toLocaleString('en-IN');
  const paiseStr = remainingPaise.toString().padStart(2, '0');
  
  const formatted = `₹${rupeesStr}.${paiseStr}`;
  if (showSign && paise > 0) return `+${formatted}`;
  if (isNegative) return `-${formatted}`;
  return formatted;
}

/**
 * Convert user Rupee input (e.g., "12.50" or 12.5) to integer paise (1250)
 * Prevents floating point errors by using string arithmetic
 * @param {string|number} rupeeInput 
 * @returns {number}
 */
export function parseRupeesToPaise(rupeeInput) {
  if (!rupeeInput) return 0;
  const str = rupeeInput.toString().trim().replace(/,/g, '');
  if (!/^\d+(\.\d{1,2})?$/.test(str)) {
    throw new Error('Please enter a valid amount (up to 2 decimal places)');
  }
  const parts = str.split('.');
  const whole = parseInt(parts[0], 10);
  const fraction = parts[1] ? parts[1].padEnd(2, '0') : '00';
  return whole * 100 + parseInt(fraction, 10);
}

/**
 * Format ISO datetime string to readable local date
 */
export function formatDate(isoStr) {
  if (!isoStr) return '';
  const date = new Date(isoStr);
  return date.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Show a transient toast notification
 */
export function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.style.cssText = `
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      z-index: 9999;
    `;
    document.body.appendChild(container);
  }

    const toast = document.createElement('div');
  const bg = type === 'error' ? 'var(--status-negative)' :
             type === 'success' ? 'var(--status-positive)' : '#2B2540';
  const color = type === 'success' ? 'var(--text-on-accent)' : '#fff';

  toast.style.cssText = `
    background: ${bg};
    color: ${color};
    padding: 0.75rem 1.25rem;
    border-radius: var(--radius-sm);
    box-shadow: var(--shadow-md);
    font-size: 0.875rem;
    font-weight: 500;
    opacity: 0;
    transform: translateY(10px);
    transition: all 0.2s ease;
  `;
  toast.textContent = message;
  container.appendChild(toast);

  requestAnimationFrame(() => {
    toast.style.opacity = '1';
    toast.style.transform = 'translateY(0)';
  });

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 200);
  }, 3500);
}

/**
 * Modal helpers
 */
export function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('open');
    document.body.style.overflow = 'hidden';
  }
}

export function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('open');
    document.body.style.overflow = '';
  }
}
