# BankingSystem
A simple in-memory banking system implemented in Java, supporting account management, fund transfers, spending analytics, and scheduled payments with cashback.
---
## Features
| Level | Feature |
|-------|---------|
| 1 | Create accounts, deposit funds, transfer between accounts |
| 2 | Rank top spenders with tie-breaking |
| 3 | Schedule future payments with percentage cashback |
---
## Class Overview
\`\`\`
BankingSystem
├── accounts          : Map<String, Long>               — account balances
├── totalSpending     : Map<String, Long>               — cumulative outgoing transfers
└── scheduledPayments : PriorityQueue<ScheduledPayment> — time-ordered scheduled payments
\`\`\`
---
## API Reference
### Level 1 — Basic Operations
#### \`createAccount(String accountId, long timestamp) → boolean\`
Creates a new account with a zero balance.
- Returns \`true\` on success.
- Returns \`false\` if the account ID already exists.
\`\`\`java
bank.createAccount("alice", 1);  // true
bank.createAccount("alice", 2);  // false — duplicate
\`\`\`
---
#### \`deposit(String accountId, long timestamp, int amount) → Long\`
Deposits the given amount into the account.
- Returns the **new balance** after deposit.
- Returns \`null\` if the account does not exist.
- Triggers any pending scheduled payments due at or before- Triggers any pending schebank.deposit("alice", 2, 500);  // 500
\`\`\`
---
#### \`transfer(String fromId, String toId, long timestamp, int amount) → Long\`
Transfers funds from one account to another.
- Returns the **sender's new balance** on success.
- Returns \`null\` if:
  - Either account does not exist
  - \`fromId\` equals \`toId\`
  - Sender has insufficient funds
- Triggers - Triggers - Theduled payments due at or before \`timestamp\`.
\`\`\`java
bank.transfer("alice", "bob", 3, 200);  // 30bank.transfer("alice", "bob", \`\`\`
---
############################## \`topSpenders(long timestamp, int n) → List<String>\`
Returns the top \`n\` accounts ranked by total outgoing transfer amount.
- Excludes accounts with zero spending.
- **Tie-breaker:** alphabetical order by account ID (ascending).
- Triggers any pending scheduled payments due at or before \`timestamp\`.
\`\`\`java
bank.topSpenders(10, 3);  // ["alice", "bob", "charlie"]
\`\`\`
---
### Level 3 — Scheduled Payments
#### \`schedulePayment(String fromId, String toId, long timestamp, int amount, int cashbackPct) → boolean\`
Schedules a future transfer with a cashback reward.
- Returns \`true\` if both accounts exist and the payment is queu- Returns \`true\` if both accounts exunt does not exist.
- The payment executes automatically when any operation is called with a \`timestamp >=\` the scheduled time.
- **C- **C- **C- **C- **C- **C- **C- **C- **C- **C- **C- **C- **C- **C- **Cnder after the transfer.
- If the sender has **insufficient funds** at execution time, the payment is silently skipped.
\`\`\`java
bank.schedulePayment("alice", "bob", 10, 300, 20);
// At timestamp 10: alice sends 300, receives 60 back (20% cashback)
\`\`\`
---
## Data Flow
\`\`\`
createAccount → deposit → transfer ──► totalSpending updated
                                   └──► schedulePayment queued
                                            │
                           next operation ──► processScheduledPayments
                                                    │
                                        executeTransferWithCashback
\`\`\`
---
## Example Usage
\`\`\`java
BankingSystem bank = new BankingSystem();
bank.createAccount("alice", 1);
bank.createAccount("bob", 1);
bank.deposit("alice", 2, 1000);        // alice: 1000
bank.transfer("alice", "bob", 3, 400); // alice: 600, bob: 400
bank.schedulePayment("alice", "bob", 10, 200, 10); // scheduled at t=10, 10% cashback
bank.deposit("alice", 10, 0);
// Scheduled payment fires: alice -200, bob +200, alice +20 (cashback)
// alice: 420, bob: 600
bank.topSpenders(11, 2); // ["alice"] — alice spent 600 total
\`\`\`
---
## Console Output
Each method logs its result to \`stdout\` for easy tracing:
\`\`\`
[createAccount] SUCCESS - Created account: alice
[deposit] Account: alice | Amount: 1000 | New Balance: 1000
[transfer] SUCCESS - From: alice To: bob Amount: 400 | Sender Balance: 600
[schedulePayment] Scheduled - From: alice To: bob Amount: 200 At: 10 Cashback: 10%
[processScheduledPayments] Executing scheduled payment - From: alice To: bob Amount: 200
[executeTransferWithCashback] SUCCESS - From: al[executeTransfoun[ex200 Cashback: 20 | From Balance: 420
[topSpenders] Top 2 spenders: [alice]
\`\`\`
---
## Internal Classes
### \`ScheduledPayment\` (private static)
| Field | Type | D| Field | Typ|-------|------|-------------|
| \`fromId\` | \`String\` | Sender account ID |
| \`toId\` | \`String\` | Receiver account ID |
| \`timestamp\` | \`long\` | Scheduled execution time |
| \`amount\` | \`int\` | Transfer amount |
| \`cashbackPct\` | \`int\` | Cashback percentage (0–100) |
---
## Testing
JUnit 5 tests are located at:
\`\`\`
test/com/dsa/BankingSystemTest.java
\`\`\`
Test coverage includes:
- Account creation (success & duplicate)
- Deposit (success & unknown account)
- Transfer (success, insufficient funds, same account, unknown account)
- Top spenders (ranking, tie-breaking, zero-spend exclusion)
- Scheduled payments (valid/invalid, cashback calculation, insufficient funds skip)
