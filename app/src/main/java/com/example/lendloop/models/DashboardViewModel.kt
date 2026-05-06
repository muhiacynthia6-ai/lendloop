package com.example.lendloop.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.*
import com.example.lendloop.data.repository.BorrowRepository
import com.example.lendloop.data.repository.PaymentRepository
import com.example.lendloop.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class DashboardRole { NONE, LENDER, BORROWER }

data class MonthlyBar(val label: String, val lent: Int, val borrowed: Int)

// ── Lender stats ──────────────────────────────────────────────────────────────
data class LenderStats(
    val totalLentOut: Double        = 0.0,
    val activeLoansCount: Int       = 0,
    val overdueCount: Int           = 0,
    val overdueAmount: Double       = 0.0,
    val totalRecovered: Double      = 0.0,
    val mpesaReceived: Double       = 0.0,
    val paypalReceived: Double      = 0.0,
    val cashReceived: Double        = 0.0,
    val monthlyBars: List<MonthlyBar> = emptyList(),
    val recentLent: List<BorrowRecord> = emptyList(),
    val topBorrowers: List<Pair<String, Double>> = emptyList()
)

// ── Borrower stats ────────────────────────────────────────────────────────────
data class BorrowerStats(
    val totalOwed: Double           = 0.0,
    val activeBorrowsCount: Int     = 0,
    val overdueCount: Int           = 0,
    val nextDueRecord: BorrowRecord? = null,
    val totalPaid: Double           = 0.0,
    val mpesaPaid: Double           = 0.0,
    val paypalPaid: Double          = 0.0,
    val cashPaid: Double            = 0.0,
    val monthlyBars: List<MonthlyBar> = emptyList(),
    val recentBorrowed: List<BorrowRecord> = emptyList()
)

data class DashboardUiState(
    val role: DashboardRole     = DashboardRole.NONE,
    val userName: String        = "",
    val lender: LenderStats     = LenderStats(),
    val borrower: BorrowerStats = BorrowerStats()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val borrowRepository: BorrowRepository,
    private val paymentRepository: PaymentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _role = MutableStateFlow(DashboardRole.NONE)

    val uiState: StateFlow<DashboardUiState> = combine(
        borrowRepository.getAllRecords(),
        paymentRepository.getAllConfirmedPayments(),
        _role
    ) { records, payments, role ->
        val now  = System.currentTimeMillis()
        DashboardUiState(
            role     = role,
            userName = sessionManager.getUserName() ?: "there",
            lender   = buildLenderStats(records, payments, now),
            borrower = buildBorrowerStats(records, payments, now)
        )
    }.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.WhileSubscribed(5000),
        initialValue   = DashboardUiState(
            userName = sessionManager.getUserName() ?: "there"
        )
    )

    fun selectRole(role: DashboardRole) { _role.value = role }
    fun clearRole()                      { _role.value = DashboardRole.NONE }

    // ── Lender ───────────────────────────────────────────────────────────────
    private fun buildLenderStats(
        records: List<BorrowRecord>,
        payments: List<Payment>,
        now: Long
    ): LenderStats {
        val lentRecords  = records.filter { it.direction == Direction.LENT }
        val activeLent   = lentRecords.filter { it.status == Status.ACTIVE }
        val overdue      = activeLent.filter { it.dueDate != null && it.dueDate < now }

        val lentIds      = lentRecords.map { it.id }.toSet()
        val lentPayments = payments.filter { it.recordId in lentIds }

        // Top borrowers by amount owed
        val topBorrowers = activeLent
            .groupBy { it.personName }
            .mapValues { (_, recs) -> recs.sumOf { it.amount ?: 0.0 } }
            .entries.sortedByDescending { it.value }
            .take(3)
            .map { it.key to it.value }

        return LenderStats(
            totalLentOut    = activeLent.sumOf { it.amount ?: 0.0 },
            activeLoansCount = activeLent.size,
            overdueCount    = overdue.size,
            overdueAmount   = overdue.sumOf { it.amount ?: 0.0 },
            totalRecovered  = lentPayments.sumOf { it.amount },
            mpesaReceived   = lentPayments.filter { it.method == PaymentMethod.MPESA }.sumOf { it.amount },
            paypalReceived  = lentPayments.filter { it.method == PaymentMethod.PAYPAL }.sumOf { it.amount },
            cashReceived    = lentPayments.filter { it.method == PaymentMethod.CASH }.sumOf { it.amount },
            monthlyBars     = buildMonthlyBars(records),
            recentLent      = lentRecords.sortedByDescending { it.lentAt }.take(5),
            topBorrowers    = topBorrowers
        )
    }

    // ── Borrower ─────────────────────────────────────────────────────────────
    private fun buildBorrowerStats(
        records: List<BorrowRecord>,
        payments: List<Payment>,
        now: Long
    ): BorrowerStats {
        val borrowedRecs   = records.filter { it.direction == Direction.BORROWED }
        val activeBorrowed = borrowedRecs.filter { it.status == Status.ACTIVE }
        val overdue        = activeBorrowed.filter { it.dueDate != null && it.dueDate < now }

        val nextDue = activeBorrowed
            .filter { it.dueDate != null && it.dueDate > now }
            .minByOrNull { it.dueDate!! }

        val borIds      = borrowedRecs.map { it.id }.toSet()
        val borPayments = payments.filter { it.recordId in borIds }

        return BorrowerStats(
            totalOwed        = activeBorrowed.sumOf { it.amount ?: 0.0 },
            activeBorrowsCount = activeBorrowed.size,
            overdueCount     = overdue.size,
            nextDueRecord    = nextDue,
            totalPaid        = borPayments.sumOf { it.amount },
            mpesaPaid        = borPayments.filter { it.method == PaymentMethod.MPESA }.sumOf { it.amount },
            paypalPaid       = borPayments.filter { it.method == PaymentMethod.PAYPAL }.sumOf { it.amount },
            cashPaid         = borPayments.filter { it.method == PaymentMethod.CASH }.sumOf { it.amount },
            monthlyBars      = buildMonthlyBars(records),
            recentBorrowed   = borrowedRecs.sortedByDescending { it.lentAt }.take(5)
        )
    }

    private fun buildMonthlyBars(records: List<BorrowRecord>): List<MonthlyBar> {
        val fmt = SimpleDateFormat("MMM", Locale.getDefault())
        val cal = Calendar.getInstance()
        return (5 downTo 0).map { monthsBack ->
            cal.time = Date()
            cal.add(Calendar.MONTH, -monthsBack)
            val year  = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val label = fmt.format(cal.time)
            val inMonth = records.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.lentAt }
                c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
            }
            MonthlyBar(
                label    = label,
                lent     = inMonth.count { it.direction == Direction.LENT },
                borrowed = inMonth.count { it.direction == Direction.BORROWED }
            )
        }
    }
}