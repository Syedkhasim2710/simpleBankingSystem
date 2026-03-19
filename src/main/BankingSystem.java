package main;

import java.util.*;

public class BankingSystem {
    private final Map<String, Long> accounts = new HashMap<>();
    private final Map<String, Long> totalSpending = new HashMap<>();
    private final PriorityQueue<ScheduledPayment> scheduledPayments = new PriorityQueue<>(
            Comparator.comparingLong(p -> p.timestamp)
    );

    // Level 1: Basic Operations
    public boolean createAccount(String accountId, long timestamp) {
        if (accounts.containsKey(accountId)) {
            System.out.println("[createAccount] FAILED - Account already exists: " + accountId);
            return false;
        }
        accounts.put(accountId, 0L);
        totalSpending.put(accountId, 0L);
        System.out.println("[createAccount] SUCCESS - Created account: " + accountId);
        return true;
    }

    public Long deposit(String accountId, long timestamp, int amount) {
        processScheduledPayments(timestamp);
        if (!accounts.containsKey(accountId)) {
            System.out.println("[deposit] FAILED - Account not found: " + accountId);
            return null;
        }
        accounts.put(accountId, accounts.get(accountId) + amount);
        System.out.println("[deposit] Account: " + accountId + " | Amount: " + amount + " | New Balance: " + accounts.get(accountId));
        return accounts.get(accountId);
    }

    public Long transfer(String fromId, String toId, long timestamp, int amount) {
        processScheduledPayments(timestamp);
        if (fromId.equals(toId) || !accounts.containsKey(fromId) || !accounts.containsKey(toId)) {
            System.out.println("[transfer] FAILED - Invalid accounts. From: " + fromId + " To: " + toId);
            return null;
        }
        if (accounts.get(fromId) < amount) {
            System.out.println("[transfer] FAILED - Insufficient funds. From: " + fromId + " Balance: " + accounts.get(fromId) + " Required: " + amount);
            return null;
        }
        accounts.put(fromId, accounts.get(fromId) - amount);
        accounts.put(toId, accounts.get(toId) + amount);
        totalSpending.put(fromId, totalSpending.get(fromId) + amount);
        System.out.println("[transfer] SUCCESS - From: " + fromId + " To: " + toId + " Amount: " + amount + " | Sender Balance: " + accounts.get(fromId));
        return accounts.get(fromId);
    }

    // Level 2: Ranking/Analytics
    public List<String> topSpenders(long timestamp, int n) {
        processScheduledPayments(timestamp);
        List<String> sortedAccounts = new ArrayList<>(totalSpending.keySet());

        sortedAccounts.sort((a, b) -> {
            long diff = totalSpending.get(b) - totalSpending.get(a);
            if (diff != 0) return (int) diff;
            return a.compareTo(b); // Tie-breaker: ID ascending
        });

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, sortedAccounts.size()); i++) {
            if (totalSpending.get(sortedAccounts.get(i)) > 0) {
                result.add(sortedAccounts.get(i));
            }
        }
        System.out.println("[topSpenders] Top " + n + " spenders: " + result);
        return result;
    }

    // Level 3: Advanced Features
    public boolean schedulePayment(String fromId, String toId, long timestamp, int amount, int cashbackPct) {
        if (!accounts.containsKey(fromId) || !accounts.containsKey(toId)) {
            System.out.println("[schedulePayment] FAILED - Invalid accounts. From: " + fromId + " To: " + toId);
            return false;
        }
        scheduledPayments.offer(new ScheduledPayment(fromId, toId, timestamp, amount, cashbackPct));
        System.out.println("[schedulePayment] Scheduled - From: " + fromId + " To: " + toId + " Amount: " + amount + " At: " + timestamp + " Cashback: " + cashbackPct + "%");
        return true;
    }

    private void processScheduledPayments(long currentTimestamp) {
        while (!scheduledPayments.isEmpty() && scheduledPayments.peek().timestamp <= currentTimestamp) {
            ScheduledPayment p = scheduledPayments.poll();
            System.out.println("[processScheduledPayments] Executing scheduled payment - From: " + p.fromId + " To: " + p.toId + " Amount: " + p.amount);
            executeTransferWithCashback(p.fromId, p.toId, p.amount, p.cashbackPct);
        }
    }

    private void executeTransferWithCashback(String from, String to, int amount, int cashbackPct) {
        if (accounts.get(from) < amount) {
            System.out.println("[executeTransferWithCashback] SKIPPED - Insufficient funds. From: " + from + " Balance: " + accounts.get(from) + " Required: " + amount);
            return;
        }
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(to, accounts.get(to) + amount);
        totalSpending.put(from, totalSpending.get(from) + amount);

        int cashback = (amount * cashbackPct) / 100;
        accounts.put(from, accounts.get(from) + cashback);
        System.out.println("[executeTransferWithCashback] SUCCESS - From: " + from + " To: " + to + " Amount: " + amount + " Cashback: " + cashback + " | From Balance: " + accounts.get(from));
    }

    private static class ScheduledPayment {
        String fromId, toId;
        long timestamp;
        int amount, cashbackPct;

        ScheduledPayment(String f, String t, long ts, int a, int c) {
            this.fromId = f; this.toId = t; this.timestamp = ts; this.amount = a; this.cashbackPct = c;
        }
    }
}
