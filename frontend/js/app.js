import { Api } from './api.js';
import { formatCurrency, parseRupeesToPaise, formatDate, showToast, openModal, closeModal } from './ui-utils.js';

// Application State
const State = {
  currentGroupId: null,
  groups: [],
  members: [],
  expenses: [],
  balances: [],
  activeExpenseForSim: null,
  simProposal: null,
};

// DOM References
const elements = {
  viewGroups: document.getElementById('view-groups'),
  viewGroupDetail: document.getElementById('view-group-detail'),
  groupsGrid: document.getElementById('groups-grid'),
  btnOpenCreateGroup: document.getElementById('btn-open-create-group'),
  btnQuickNewGroup: document.getElementById('btn-quick-new-group'),
  btnBackToGroups: document.getElementById('btn-back-to-groups'),
  detailGroupName: document.getElementById('detail-group-name'),
  detailGroupDesc: document.getElementById('detail-group-desc'),
  detailTotalSpend: document.getElementById('detail-total-spend'),
  detailMemberCount: document.getElementById('detail-member-count'),
  detailUnsettledSum: document.getElementById('detail-unsettled-sum'),
  expensesTbody: document.getElementById('expenses-tbody'),
  balancesListContainer: document.getElementById('balances-list-container'),
  settlementSuggestionsContainer: document.getElementById('settlement-suggestions-container'),
  optimizerAlgoBadge: document.getElementById('optimizer-algo-badge'),
  activityTimelineContainer: document.getElementById('activity-timeline-container'),
  
  // Modals & Forms
  formCreateGroup: document.getElementById('form-create-group'),
  formAddMember: document.getElementById('form-add-member'),
  formExpense: document.getElementById('form-expense'),
  formSettle: document.getElementById('form-settle'),
  btnOpenAddMember: document.getElementById('btn-open-add-member'),
  btnOpenAddExpense: document.getElementById('btn-open-add-expense'),
  btnTabAddExpense: document.getElementById('btn-tab-add-expense'),
  expenseSharesInputs: document.getElementById('expense-shares-inputs'),
  expenseSplitMethod: document.getElementById('expense-split-method'),
  expensePayer: document.getElementById('expense-payer'),

  // Simulator
  simExpenseSelect: document.getElementById('sim-expense-select'),
  simMethodSelect: document.getElementById('sim-method-select'),
  simParticipantsList: document.getElementById('sim-participants-list'),
  btnRunSim: document.getElementById('btn-run-sim'),
  btnResetSim: document.getElementById('btn-reset-sim'),
  simResultsBox: document.getElementById('sim-results-box'),
  simComparisonTbody: document.getElementById('sim-comparison-tbody'),
  btnDiscardSim: document.getElementById('btn-discard-sim'),
  btnApplySim: document.getElementById('btn-apply-sim'),
};

/**
 * Initialize Event Handlers
 */
function init() {
  // Navigation & Tabs
  elements.btnBackToGroups?.addEventListener('click', showGroupsView);
  elements.btnOpenCreateGroup?.addEventListener('click', () => openModal('modal-create-group'));
  elements.btnQuickNewGroup?.addEventListener('click', () => openModal('modal-create-group'));
  elements.btnOpenAddMember?.addEventListener('click', () => openModal('modal-add-member'));
  elements.btnOpenAddExpense?.addEventListener('click', () => prepareExpenseModal());
  elements.btnTabAddExpense?.addEventListener('click', () => prepareExpenseModal());

  // Close modals on data-close
  document.querySelectorAll('[data-close]').forEach(btn => {
    btn.addEventListener('click', () => closeModal(btn.getAttribute('data-close')));
  });

  // Tab switching
  document.querySelectorAll('[data-tab]').forEach(tabBtn => {
    tabBtn.addEventListener('click', () => {
      document.querySelectorAll('[data-tab]').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
      tabBtn.classList.add('active');
      const target = document.getElementById(tabBtn.getAttribute('data-tab'));
      if (target) target.style.display = 'block';
    });
  });

  // Form Submissions
  elements.formCreateGroup?.addEventListener('submit', handleCreateGroup);
  elements.formAddMember?.addEventListener('submit', handleAddMember);
  elements.formExpense?.addEventListener('submit', handleSaveExpense);
  elements.formSettle?.addEventListener('submit', handleRecordSettlement);

  // Split Method Change in Expense Modal
  elements.expenseSplitMethod?.addEventListener('change', renderExpenseShareInputs);

  // Simulator Events
  elements.simExpenseSelect?.addEventListener('change', handleSimExpenseSelect);
  elements.btnRunSim?.addEventListener('click', handleRunSimulation);
  elements.btnResetSim?.addEventListener('click', resetSimulation);
  elements.btnDiscardSim?.addEventListener('click', resetSimulation);
  elements.btnApplySim?.addEventListener('click', handleApplySimulation);

  // Load Initial Groups
  loadGroups();
}

/**
 * View Management
 */
function showGroupsView() {
  State.currentGroupId = null;
  elements.viewGroupDetail.classList.remove('active');
  elements.viewGroups.classList.add('active');
  loadGroups();
}

function showGroupDetailView(groupId) {
  State.currentGroupId = groupId;
  elements.viewGroups.classList.remove('active');
  elements.viewGroupDetail.classList.add('active');
  loadGroupDetails(groupId);
}

/**
 * Groups Loading & Creation
 */
async function loadGroups() {
  try {
    elements.groupsGrid.innerHTML = '<div class="col-span-12"><div class="loading-spinner"></div></div>';
    const groups = await Api.getGroups();
    State.groups = groups || [];

    if (State.groups.length === 0) {
      elements.groupsGrid.innerHTML = `
        <div class="col-span-12 card empty-state">
          <div class="empty-state-icon">📂</div>
          <h3>No expense groups yet</h3>
          <p style="margin-top: 0.5rem; color: var(--text-secondary);">Create your first group to start recording shared costs.</p>
          <button class="btn btn-primary" style="margin-top: 1.5rem;" onclick="document.getElementById('btn-open-create-group').click()">
            + Create Group
          </button>
        </div>
      `;
      return;
    }

    elements.groupsGrid.innerHTML = State.groups.map(group => `
      <div class="col-span-4 card" style="cursor: pointer;" data-group-id="${group.id}">
        <div class="card-header">
          <h3 class="card-title">${escapeHtml(group.name)}</h3>
          <span class="badge badge-neutral">${group.currencyCode || 'INR'}</span>
        </div>
        <p class="card-subtitle" style="min-height: 2.5rem;">${escapeHtml(group.description || 'No description')}</p>
        <div style="margin-top: 1.25rem; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-subtle); padding-top: 0.875rem;">
          <span style="font-size: 0.8125rem; color: var(--text-muted);">${group.memberCount || 0} members</span>
          <span style="font-weight: 600; color: var(--accent-primary);">Open Group →</span>
        </div>
      </div>
    `).join('');

    elements.groupsGrid.querySelectorAll('[data-group-id]').forEach(card => {
      card.addEventListener('click', () => {
        const id = card.getAttribute('data-group-id');
        showGroupDetailView(id);
      });
    });
  } catch (err) {
    elements.groupsGrid.innerHTML = `
      <div class="col-span-12 alert alert-warning">
        Failed to connect to backend: ${err.message}. Ensure Docker PostgreSQL & Spring Boot are running.
      </div>
    `;
  }
}

async function handleCreateGroup(e) {
  e.preventDefault();
  const name = document.getElementById('input-group-name').value.trim();
  const description = document.getElementById('input-group-desc').value.trim();

  try {
    const created = await Api.createGroup({ name, description, currencyCode: 'INR' });
    closeModal('modal-create-group');
    elements.formCreateGroup.reset();
    showToast(`Group "${created.name}" created successfully!`, 'success');
    showGroupDetailView(created.id);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

/**
 * Group Details & Associated Data
 */
async function loadGroupDetails(groupId) {
  try {
    const [group, members, expenses, balances, suggestions, activity] = await Promise.all([
      Api.getGroup(groupId),
      Api.getMembers(groupId),
      Api.getExpenses(groupId),
      Api.getBalances(groupId),
      Api.getSettlementSuggestions(groupId),
      Api.getActivity(groupId).catch(() => []),
    ]);

    State.members = members || [];
    State.expenses = expenses || [];
    State.balances = balances?.balances || [];

    // Header info
    elements.detailGroupName.textContent = group.name;
    elements.detailGroupDesc.textContent = group.description || 'No description provided';
    elements.detailMemberCount.textContent = State.members.length;

    const totalSpendPaise = State.expenses.reduce((sum, exp) => sum + exp.amountPaise, 0);
    elements.detailTotalSpend.textContent = formatCurrency(totalSpendPaise);

    // Render Subsections
    renderExpensesTable(State.expenses);
    renderBalances(State.balances);
    renderSettlementSuggestions(suggestions);
    renderActivityTimeline(activity);
    populateSimulatorExpenseOptions(State.expenses);
  } catch (err) {
    showToast(`Error loading group details: ${err.message}`, 'error');
  }
}

/**
 * Members Management
 */
async function handleAddMember(e) {
  e.preventDefault();
  const displayName = document.getElementById('input-member-name').value.trim();

  try {
    await Api.addMember(State.currentGroupId, { displayName });
    closeModal('modal-add-member');
    elements.formAddMember.reset();
    showToast(`Member "${displayName}" added!`, 'success');
    loadGroupDetails(State.currentGroupId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

/**
 * Expenses Management
 */
function prepareExpenseModal(expense = null) {
  if (State.members.length < 1) {
    showToast('Please add members to the group before recording an expense.', 'error');
    openModal('modal-add-member');
    return;
  }

  // Populate Payer Select
  elements.expensePayer.innerHTML = State.members.map(m => `
    <option value="${m.id}" ${expense && expense.paidByMemberId === m.id ? 'selected' : ''}>
      ${escapeHtml(m.displayName)}
    </option>
  `).join('');

  document.getElementById('expense-id-hidden').value = expense ? expense.id : '';
  document.getElementById('modal-expense-title').textContent = expense ? 'Edit Expense' : 'Add Expense';
  document.getElementById('expense-desc').value = expense ? expense.description : '';
  document.getElementById('expense-amount').value = expense ? (expense.amountPaise / 100).toFixed(2) : '';
  elements.expenseSplitMethod.value = expense ? expense.splitMethod : 'EQUAL';

  renderExpenseShareInputs();
  openModal('modal-expense');
}

function renderExpenseShareInputs() {
  const method = elements.expenseSplitMethod.value;
  const container = elements.expenseSharesInputs;

  if (method === 'EQUAL') {
    container.innerHTML = `
      <div style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: 0.5rem;">
        Select participants (leftover paise will be distributed deterministically):
      </div>
      <div style="display: flex; flex-direction: column; gap: 0.5rem;">
        ${State.members.map(m => `
          <label style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.875rem;">
            <input type="checkbox" name="expense-participant" value="${m.id}" checked>
            <span>${escapeHtml(m.displayName)}</span>
          </label>
        `).join('')}
      </div>
    `;
  } else if (method === 'EXACT') {
    container.innerHTML = `
      <div style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: 0.5rem;">
        Specify exact amount for each participant in Rupees (sum must equal total):
      </div>
      <div style="display: flex; flex-direction: column; gap: 0.625rem;">
        ${State.members.map(m => `
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 1rem;">
            <span style="font-size: 0.875rem;">${escapeHtml(m.displayName)}</span>
            <input type="number" step="0.01" min="0" placeholder="0.00" class="form-control form-control-sm share-exact-input" data-member-id="${m.id}" style="max-width: 120px; text-align: right;">
          </div>
        `).join('')}
      </div>
    `;
  } else if (method === 'PERCENTAGE') {
    container.innerHTML = `
      <div style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: 0.5rem;">
        Enter percentage share for each participant (must sum to 100%):
      </div>
      <div style="display: flex; flex-direction: column; gap: 0.625rem;">
        ${State.members.map(m => `
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 1rem;">
            <span style="font-size: 0.875rem;">${escapeHtml(m.displayName)}</span>
            <div style="display: flex; align-items: center; gap: 0.375rem;">
              <input type="number" step="0.01" min="0" max="100" placeholder="0" class="form-control form-control-sm share-percent-input" data-member-id="${m.id}" style="max-width: 100px; text-align: right;">
              <span style="color: var(--text-muted); font-size: 0.875rem;">%</span>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  }
}

async function handleSaveExpense(e) {
  e.preventDefault();
  const desc = document.getElementById('expense-desc').value.trim();
  const amountStr = document.getElementById('expense-amount').value;
  const paidByMemberId = parseInt(elements.expensePayer.value, 10);
  const splitMethod = elements.expenseSplitMethod.value;

  let amountPaise;
  try {
    amountPaise = parseRupeesToPaise(amountStr);
  } catch (err) {
    showToast(err.message, 'error');
    return;
  }

  const payload = {
    description: desc,
    amountPaise,
    paidByMemberId,
    splitMethod,
  };

  if (splitMethod === 'EQUAL') {
    const selected = Array.from(document.querySelectorAll('input[name="expense-participant"]:checked'))
      .map(cb => parseInt(cb.value, 10));
    if (selected.length === 0) {
      showToast('At least one participant must be selected.', 'error');
      return;
    }
    payload.participantMemberIds = selected;
  } else if (splitMethod === 'EXACT') {
    const shares = [];
    document.querySelectorAll('.share-exact-input').forEach(inp => {
      const val = parseFloat(inp.value) || 0;
      if (val > 0) {
        shares.push({
          memberId: parseInt(inp.getAttribute('data-member-id'), 10),
          sharePaise: Math.round(val * 100),
        });
      }
    });
    payload.shares = shares;
  } else if (splitMethod === 'PERCENTAGE') {
    const shares = [];
    document.querySelectorAll('.share-percent-input').forEach(inp => {
      const val = parseFloat(inp.value) || 0;
      if (val > 0) {
        shares.push({
          memberId: parseInt(inp.getAttribute('data-member-id'), 10),
          percentageBasisPoints: Math.round(val * 100), // 100% = 10000 bp
        });
      }
    });
    payload.shares = shares;
  }

  try {
    const expenseId = document.getElementById('expense-id-hidden').value;
    if (expenseId) {
      await Api.updateExpense(expenseId, payload);
      showToast('Expense updated successfully!', 'success');
    } else {
      await Api.createExpense(State.currentGroupId, payload);
      showToast('Expense recorded successfully!', 'success');
    }
    closeModal('modal-expense');
    elements.formExpense.reset();
    loadGroupDetails(State.currentGroupId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function renderExpensesTable(expenses) {
  if (!expenses || expenses.length === 0) {
    elements.expensesTbody.innerHTML = `
      <tr>
        <td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">
          No expenses recorded in this group yet.
        </td>
      </tr>
    `;
    return;
  }

  elements.expensesTbody.innerHTML = expenses.map(exp => {
    const payer = State.members.find(m => m.id === exp.paidByMemberId)?.displayName || 'Unknown';
    return `
      <tr>
        <td>${formatDate(exp.occurredAt || exp.createdAt)}</td>
        <td style="font-weight: 500; color: var(--text-primary);">${escapeHtml(exp.description)}</td>
        <td>${escapeHtml(payer)}</td>
        <td><span class="badge badge-neutral">${exp.splitMethod}</span></td>
        <td style="font-family: var(--font-mono); font-weight: 600; color: var(--text-primary);">
          ${formatCurrency(exp.amountPaise)}
        </td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="window.fairshareEditExpense(${exp.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="window.fairshareDeleteExpense(${exp.id})" style="margin-left: 0.25rem;">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

window.fairshareEditExpense = (id) => {
  const expense = State.expenses.find(e => e.id === id);
  if (expense) prepareExpenseModal(expense);
};

window.fairshareDeleteExpense = async (id) => {
  if (!confirm('Are you sure you want to delete this expense? Recalculation will occur automatically.')) return;
  try {
    await Api.deleteExpense(id);
    showToast('Expense deleted.', 'info');
    loadGroupDetails(State.currentGroupId);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

/**
 * Balances & Settlement Suggestions
 */
function renderBalances(balances) {
  if (!balances || balances.length === 0) {
    elements.balancesListContainer.innerHTML = '<p class="text-muted">No balances available.</p>';
    return;
  }

  elements.balancesListContainer.innerHTML = balances.map(b => {
    const isCreditor = b.netBalancePaise > 0;
    const isDebtor = b.netBalancePaise < 0;
    const badgeClass = isCreditor ? 'badge-positive' : isDebtor ? 'badge-negative' : 'badge-neutral';
    const statusText = isCreditor ? 'Owed' : isDebtor ? 'Owes' : 'Settled';

    return `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.875rem 0; border-bottom: 1px solid var(--border-subtle);">
        <div>
          <div style="font-weight: 600; color: var(--text-primary);">${escapeHtml(b.displayName)}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted);">${statusText}</div>
        </div>
        <div style="text-align: right;">
          <span class="badge ${badgeClass}" style="font-family: var(--font-mono); font-size: 0.875rem;">
            ${formatCurrency(b.netBalancePaise, true)}
          </span>
        </div>
      </div>
    `;
  }).join('');
}

function renderSettlementSuggestions(suggestionsData) {
  const container = elements.settlementSuggestionsContainer;
  const transfers = suggestionsData?.transfers || [];
  const algorithm = suggestionsData?.algorithm || 'EXACT_OPTIMAL';

  elements.optimizerAlgoBadge.textContent = algorithm === 'EXACT_OPTIMAL' ? 'Exact Optimal' : 'Greedy Plan';
  elements.optimizerAlgoBadge.className = algorithm === 'EXACT_OPTIMAL' ? 'badge badge-positive' : 'badge badge-warning';

  if (transfers.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem; color: var(--text-muted);">
        <p>🎉 All balances are settled! No payments required.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = transfers.map((t, idx) => `
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.875rem; background: var(--bg-surface-elevated); border-radius: var(--radius-sm); margin-bottom: 0.75rem;">
      <div>
        <span style="font-weight: 600; color: var(--status-negative);">${escapeHtml(t.fromMemberName)}</span>
        <span style="color: var(--text-muted); margin: 0 0.5rem;">pays</span>
        <span style="font-weight: 600; color: var(--status-positive);">${escapeHtml(t.toMemberName)}</span>
      </div>
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <span style="font-family: var(--font-mono); font-weight: 700; color: var(--text-primary);">
          ${formatCurrency(t.amountPaise)}
        </span>
        <button class="btn btn-outline btn-sm" onclick="window.fairshareOpenSettleModal(${t.fromMemberId}, ${t.toMemberId}, ${t.amountPaise})">
          Record Payment
        </button>
      </div>
    </div>
  `).join('');
}

window.fairshareOpenSettleModal = (fromId, toId, amountPaise) => {
  elements.settleFrom = document.getElementById('settle-from');
  elements.settleTo = document.getElementById('settle-to');

  elements.settleFrom.innerHTML = State.members.map(m => `
    <option value="${m.id}" ${m.id === fromId ? 'selected' : ''}>${escapeHtml(m.displayName)}</option>
  `).join('');

  elements.settleTo.innerHTML = State.members.map(m => `
    <option value="${m.id}" ${m.id === toId ? 'selected' : ''}>${escapeHtml(m.displayName)}</option>
  `).join('');

  document.getElementById('settle-amount').value = (amountPaise / 100).toFixed(2);
  openModal('modal-settle');
};

async function handleRecordSettlement(e) {
  e.preventDefault();
  const fromMemberId = parseInt(document.getElementById('settle-from').value, 10);
  const toMemberId = parseInt(document.getElementById('settle-to').value, 10);
  const amountStr = document.getElementById('settle-amount').value;
  const note = document.getElementById('settle-note').value.trim();

  let amountPaise;
  try {
    amountPaise = parseRupeesToPaise(amountStr);
  } catch (err) {
    showToast(err.message, 'error');
    return;
  }

  try {
    await Api.recordSettlement(State.currentGroupId, {
      fromMemberId,
      toMemberId,
      amountPaise,
      note,
    });
    closeModal('modal-settle');
    elements.formSettle.reset();
    showToast('Payment recorded successfully!', 'success');
    loadGroupDetails(State.currentGroupId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

/**
 * Fairness Simulator
 */
function populateSimulatorExpenseOptions(expenses) {
  elements.simExpenseSelect.innerHTML = `
    <option value="">-- Choose an expense to simulate --</option>
    ${expenses.map(e => `
      <option value="${e.id}">${escapeHtml(e.description)} (${formatCurrency(e.amountPaise)})</option>
    `).join('')}
  `;
}

function handleSimExpenseSelect() {
  const expenseId = parseInt(elements.simExpenseSelect.value, 10);
  State.activeExpenseForSim = State.expenses.find(e => e.id === expenseId);

  if (!State.activeExpenseForSim) {
    elements.simParticipantsList.innerHTML = '';
    return;
  }

  // Populate participant selection
  elements.simParticipantsList.innerHTML = State.members.map(m => `
    <label style="display: inline-flex; align-items: center; gap: 0.375rem; background: var(--bg-surface); padding: 0.375rem 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-subtle); font-size: 0.875rem;">
      <input type="checkbox" name="sim-participant" value="${m.id}" checked>
      <span>${escapeHtml(m.displayName)}</span>
    </label>
  `).join('');
}

async function handleRunSimulation() {
  if (!State.activeExpenseForSim) {
    showToast('Please select an expense first.', 'error');
    return;
  }

  const method = elements.simMethodSelect.value;
  const selectedParticipantIds = Array.from(document.querySelectorAll('input[name="sim-participant"]:checked'))
    .map(cb => parseInt(cb.value, 10));

  if (selectedParticipantIds.length === 0) {
    showToast('At least one participant must be selected.', 'error');
    return;
  }

  const payload = {
    amountPaise: State.activeExpenseForSim.amountPaise,
    splitMethod: method,
    participantMemberIds: selectedParticipantIds,
  };

  try {
    const preview = await Api.previewSplit(State.activeExpenseForSim.id, payload);
    State.simProposal = { expenseId: State.activeExpenseForSim.id, payload, preview };
    renderSimulationComparison(preview);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function renderSimulationComparison(preview) {
  elements.simResultsBox.style.display = 'block';
  const currentShares = preview.currentShares || [];
  const proposedShares = preview.proposedShares || [];

  elements.simComparisonTbody.innerHTML = State.members.map(m => {
    const curr = currentShares.find(s => s.memberId === m.id)?.sharePaise || 0;
    const prop = proposedShares.find(s => s.memberId === m.id)?.sharePaise || 0;
    const delta = prop - curr;

    const deltaSign = delta > 0 ? `+${formatCurrency(delta)} (Pays more)` :
                      delta < 0 ? `${formatCurrency(delta)} (Pays less)` : 'No change';
    const deltaClass = delta > 0 ? 'delta-negative' : delta < 0 ? 'delta-positive' : '';

    return `
      <tr>
        <td style="font-weight: 500; color: var(--text-primary);">${escapeHtml(m.displayName)}</td>
        <td style="font-family: var(--font-mono);">${formatCurrency(curr)}</td>
        <td style="font-family: var(--font-mono); font-weight: 600; color: var(--text-primary);">${formatCurrency(prop)}</td>
        <td class="delta-pill ${deltaClass}">${deltaSign}</td>
        <td><span class="badge badge-neutral">Simulated</span></td>
      </tr>
    `;
  }).join('');
}

function resetSimulation() {
  State.activeExpenseForSim = null;
  State.simProposal = null;
  elements.simResultsBox.style.display = 'none';
  elements.simExpenseSelect.value = '';
  elements.simParticipantsList.innerHTML = '';
}

async function handleApplySimulation() {
  if (!State.simProposal) return;
  try {
    await Api.applySplit(State.simProposal.expenseId, State.simProposal.payload);
    showToast('Scenario applied successfully!', 'success');
    resetSimulation();
    loadGroupDetails(State.currentGroupId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

/**
 * Activity Log Timeline
 */
function renderActivityTimeline(activities) {
  const container = elements.activityTimelineContainer;
  if (!activities || activities.length === 0) {
    container.innerHTML = '<p class="text-muted" style="text-align: center; padding: 2rem;">No activity recorded yet.</p>';
    return;
  }

  container.innerHTML = activities.map(act => `
    <div style="padding: 1rem 0; border-bottom: 1px solid var(--border-subtle);">
      <div style="display: flex; justify-content: space-between; font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.25rem;">
        <span class="badge badge-neutral">${act.eventType}</span>
        <span>${formatDate(act.createdAt)}</span>
      </div>
      <div style="color: var(--text-primary); font-size: 0.875rem;">${escapeHtml(act.summary)}</div>
    </div>
  `).join('');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, match => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[match]));
}

// Kickoff
document.addEventListener('DOMContentLoaded', init);
