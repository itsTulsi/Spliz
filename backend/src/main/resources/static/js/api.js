const API_BASE = (window.location.port && window.location.port !== '8080')
  ? 'http://localhost:8080/api'
  : '/api';

export const Api = {
  async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...options.headers,
    };

    try {
      const response = await fetch(url, { ...options, headers });
      if (response.status === 204) return null;

      const contentType = response.headers.get('content-type');
      const isJson = contentType && contentType.includes('application/json');
      const data = isJson ? await response.json() : await response.text();

      if (!response.ok) {
        let errorMsg = data?.message || data?.error || `HTTP ${response.status}: ${response.statusText}`;
        if (data?.validationErrors && typeof data.validationErrors === 'object') {
          const details = Object.entries(data.validationErrors).map(([k, v]) => `${k}: ${v}`).join('; ');
          if (details) {
            errorMsg = details;
          }
        }
        const err = new Error(errorMsg);
        err.status = response.status;
        err.data = data;
        throw err;
      }

      return data;
    } catch (err) {
      console.error(`API Error on ${options.method || 'GET'} ${url}:`, err);
      throw err;
    }
  },

  // Groups
  getGroups() {
    return this.request('/groups');
  },
  getGroup(groupId) {
    return this.request(`/groups/${groupId}`);
  },
  createGroup(payload) {
    return this.request('/groups', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateGroup(groupId, payload) {
    return this.request(`/groups/${groupId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deleteGroup(groupId) {
    return this.request(`/groups/${groupId}`, { method: 'DELETE' });
  },

  // Members
  getMembers(groupId) {
    return this.request(`/groups/${groupId}/members`);
  },
  addMember(groupId, payload) {
    return this.request(`/groups/${groupId}/members`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateMember(groupId, memberId, payload) {
    return this.request(`/groups/${groupId}/members/${memberId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  // Expenses
  getExpenses(groupId) {
    return this.request(`/groups/${groupId}/expenses`);
  },
  getExpense(expenseId) {
    return this.request(`/expenses/${expenseId}`);
  },
  createExpense(groupId, payload) {
    return this.request(`/groups/${groupId}/expenses`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateExpense(expenseId, payload) {
    return this.request(`/expenses/${expenseId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deleteExpense(expenseId) {
    return this.request(`/expenses/${expenseId}`, { method: 'DELETE' });
  },

  // Balances & Settlements
  getBalances(groupId) {
    return this.request(`/groups/${groupId}/balances`);
  },
  getSettlementSuggestions(groupId) {
    return this.request(`/groups/${groupId}/settlements/suggestions`);
  },
  getRecordedSettlements(groupId) {
    return this.request(`/groups/${groupId}/settlements`);
  },
  recordSettlement(groupId, payload) {
    return this.request(`/groups/${groupId}/settlements`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  initiateUpiPayment(groupId, payload) {
    return this.request(`/groups/${groupId}/settlements/initiate`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  confirmSettlementPayment(groupId, settlementId, payload) {
    return this.request(`/groups/${groupId}/settlements/${settlementId}/confirm`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  getSettlement(groupId, settlementId) {
    return this.request(`/groups/${groupId}/settlements/${settlementId}`);
  },
  previewSettleUp(groupId) {
    return this.request(`/groups/${groupId}/settlements/settle-up/preview`);
  },
  executeSettleUp(groupId) {
    return this.request(`/groups/${groupId}/settlements/settle-up`, {
      method: 'POST',
    });
  },

  // Fairness Simulator
  previewSplit(expenseId, payload) {
    if (expenseId) {
      return this.request(`/expenses/${expenseId}/preview-split`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    }
    return this.request('/expenses/preview-split', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  applySplit(expenseId, payload) {
    return this.request(`/expenses/${expenseId}/apply-split`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  // Activity History & Budgets
  getActivity(groupId) {
    return this.request(`/groups/${groupId}/activity`);
  },
  getBudgets(groupId) {
    return this.request(`/groups/${groupId}/budgets`);
  },
  createBudget(groupId, payload) {
    return this.request(`/groups/${groupId}/budgets`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  getGroupSummary(groupId) {
    return this.request(`/groups/${groupId}/summary`);
  },
};
