package test;

import main.BankingSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

public class BankingSystemTest {

    private BankingSystem bank;

    @BeforeEach
    void setUp() {
        bank = new BankingSystem();
    }

    // ─── Level 1: Basic Operations ───────────────────────────────────────────

    @Test
    void createAccount_newAccount_returnsTrue() {
        assertTrue(bank.createAccount("acc1", 1));
    }

    @Test
    void createAccount_duplicateAccount_returnsFalse() {
        bank.createAccount("acc1", 1);
        assertFalse(bank.createAccount("acc1", 2));
    }

    @Test
    void deposit_existingAccount_returnsUpdatedBalance() {
        bank.createAccount("acc1", 1);
        assertEquals(500L, bank.deposit("acc1", 2, 500));
    }

    @Test
    void deposit_nonExistingAccount_returnsNull() {
        assertNull(bank.deposit("unknown", 1, 100));
    }

    @Test
    void transfer_validTransfer_returnsUpdatedSenderBalance() {
        bank.createAccount("sender", 1);
        bank.createAccount("receiver", 1);
        bank.deposit("sender", 2, 1000);

        Long result = bank.transfer("sender", "receiver", 3, 400);
        assertEquals(600L, result);
    }

    @Test
    void transfer_insufficientFunds_returnsNull() {
        bank.createAccount("sender", 1);
        bank.createAccount("receiver", 1);
        bank.deposit("sender", 2, 100);

        assertNull(bank.transfer("sender", "receiver", 3, 500));
    }

    @Test
    void transfer_sameAccount_returnsNull() {
        bank.createAccount("acc1", 1);
        bank.deposit("acc1", 2, 500);
        assertNull(bank.transfer("acc1", "acc1", 3, 100));
    }

    @Test
    void transfer_nonExistingAccount_returnsNull() {
        bank.createAccount("acc1", 1);
        assertNull(bank.transfer("acc1", "ghost", 2, 100));
    }

    // ─── Level 2: Ranking / Analytics ────────────────────────────────────────

    @Test
    void topSpenders_returnsTopNBySpending() {
        bank.createAccount("a1", 1);
        bank.createAccount("a2", 1);
        bank.createAccount("a3", 1);
        bank.deposit("a1", 2, 1000);
        bank.deposit("a2", 2, 1000);
        bank.deposit("a3", 2, 1000);

        bank.transfer("a1", "a3", 3, 300);
        bank.transfer("a2", "a3", 3, 700);

        List<String> top = bank.topSpenders(4, 2);
        assertEquals(List.of("a2", "a1"), top);
    }

    @Test
    void topSpenders_excludesZeroSpenders() {
        bank.createAccount("a1", 1);
        bank.createAccount("a2", 1);
        bank.deposit("a1", 2, 500);
        bank.transfer("a1", "a2", 3, 200);

        List<String> top = bank.topSpenders(4, 5);
        assertFalse(top.contains("a2")); // a2 never transferred out
        assertTrue(top.contains("a1"));
    }

    @Test
    void topSpenders_tieBreaker_alphabeticalOrder() {
        bank.createAccount("bob", 1);
        bank.createAccount("alice", 1);
        bank.deposit("bob", 2, 1000);
        bank.deposit("alice", 2, 1000);
        bank.transfer("bob", "alice", 3, 500);
        bank.transfer("alice", "bob", 4, 500);

        List<String> top = bank.topSpenders(5, 2);
        assertEquals("alice", top.get(0)); // same spending, alphabetical
        assertEquals("bob", top.get(1));
    }

    // ─── Level 3: Scheduled Payments ─────────────────────────────────────────

    @Test
    void schedulePayment_validAccounts_returnsTrue() {
        bank.createAccount("from", 1);
        bank.createAccount("to", 1);
        assertTrue(bank.schedulePayment("from", "to", 10, 200, 10));
    }

    @Test
    void schedulePayment_unknownAccount_returnsFalse() {
        bank.createAccount("from", 1);
        assertFalse(bank.schedulePayment("from", "ghost", 10, 200, 10));
    }

    @Test
    void scheduledPayment_executesOnNextOperation() {
        bank.createAccount("from", 1);
        bank.createAccount("to", 1);
        bank.deposit("from", 1, 1000);
        bank.schedulePayment("from", "to", 10, 300, 20); // 20% cashback

        // Trigger processing by depositing at timestamp >= 10
        Long balance = bank.deposit("from", 10, 0);

        // Sent 300, got back 60 (20%), started with 1000 → 1000 - 300 + 60 = 760
        assertEquals(760L, balance);
    }

    @Test
    void scheduledPayment_insufficientFunds_skipped() {
        bank.createAccount("from", 1);
        bank.createAccount("to", 1);
        bank.deposit("from", 1, 100);
        bank.schedulePayment("from", "to", 10, 500, 10); // more than balance

        Long balance = bank.deposit("from", 10, 0);

        // Payment should be skipped; balance unchanged from 100
        assertEquals(100L, balance);
    }



    @Test
    void testLevel1_BasicOperations() {
        assertTrue(bank.createAccount("A1", 1));
        assertFalse(bank.createAccount("A1", 2)); // Duplicate ID

        assertEquals(100L, bank.deposit("A1", 3, 100));
        assertNull(bank.deposit("A2", 4, 100)); // Non-existent account

        assertTrue(bank.createAccount("A2", 5));
        assertEquals(40L, bank.transfer("A1", "A2", 6, 60));
        assertEquals(60L, bank.deposit("A2", 7, 0)); // Check balance of A2

        assertNull(bank.transfer("A1", "A2", 8, 1000)); // Insufficient funds
    }

    @Test
    void testLevel2_TopSpenders() {
        bank.createAccount("A1", 1);
        bank.createAccount("A2", 2);
        bank.createAccount("A3", 3);

        bank.deposit("A1", 4, 1000);
        bank.deposit("A2", 5, 1000);
        bank.deposit("A3", 6, 1000);

        bank.transfer("A1", "A3", 7, 500);
        bank.transfer("A2", "A3", 8, 500);
        bank.transfer("A3", "A1", 9, 200);

        // Expected: A1 (500), A2 (500), A3 (200).
        // Tie-break: A1 before A2 (ID ascending)
        List<String> top = bank.topSpenders(10, 2);
        assertEquals(List.of("A1", "A2"), top);
    }

    @Test
    void testLevel3_ScheduledPaymentWithCashback() {
        bank.createAccount("User", 1);
        bank.createAccount("Merchant", 2);
        bank.deposit("User", 3, 100);

        // Schedule 50 transfer with 10% cashback (5)
        assertTrue(bank.schedulePayment("User", "Merchant", 10, 50, 10));

        // Balance before schedule time
        assertEquals(100L, bank.deposit("User", 5, 0));

        // Action at/after timestamp 10 triggers the payment
        // New balance: 100 - 50 (transfer) + 5 (cashback) = 55
        assertEquals(55L, bank.deposit("User", 11, 0));
        assertEquals(50L, bank.deposit("Merchant", 12, 0));
    }

    @Test
    void testLevel3_InsufficientFundsForScheduledPayment() {
        bank.createAccount("A", 1);
        bank.createAccount("B", 2);
        bank.deposit("A", 3, 10);

        // Schedule 50 but only have 10
        bank.schedulePayment("A", "B", 10, 50, 0);

        // Payment should fail silently, balance remains 10
        assertEquals(10L, bank.deposit("A", 15, 0));
    }
}

