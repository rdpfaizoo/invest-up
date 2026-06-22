package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.UserEntity
import com.example.data.database.PlanEntity
import com.example.data.database.DepositEntity
import com.example.data.repository.InvestmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: InvestmentRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Active streams dependent on logged in user
    val userPlans: StateFlow<List<PlanEntity>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getUserPlans(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDeposits: StateFlow<List<DepositEntity>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getUserDeposits(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All deposits for mock administrator control
    val allDeposits: StateFlow<List<DepositEntity>> = repository.getAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // Refresh user balance from database
    private fun reloadUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            _currentUser.value = user
        }
    }

    fun login(email: String, word: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || word.isBlank()) {
            onResult(false, "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            if ((normalizedEmail == "admin" || normalizedEmail == "admin@gmail.com") && word == "admin") {
                var user = repository.getUserByEmail("admin")
                if (user == null) {
                    val defaultAdmin = UserEntity(
                        email = "admin",
                        name = "Admin",
                        passwordHash = "admin",
                        balance = 50000.0,
                        earnings = 0.0
                    )
                    repository.insertUser(defaultAdmin)
                    user = defaultAdmin
                }
                _currentUser.value = user
                _isAdminMode.value = true
                onResult(true, "Welcome back, Admin! (Admin Mode activated)")
                return@launch
            }

            val user = repository.getUserByEmail(normalizedEmail)
            if (user != null && user.passwordHash == word) {
                _currentUser.value = user
                if (user.email == "admin") {
                    _isAdminMode.value = true
                }
                onResult(true, "Welcome back, ${user.name}!")
            } else {
                onResult(false, "Invalid email or password")
            }
        }
    }

    fun signup(email: String, name: String, word: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || name.isBlank() || word.isBlank()) {
            onResult(false, "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            val existing = repository.getUserByEmail(normalizedEmail)
            if (existing != null) {
                onResult(false, "Email already registered")
                return@launch
            }
            val newUser = UserEntity(
                email = normalizedEmail,
                name = name.trim(),
                passwordHash = word,
                balance = 100.0, // give a standard welcome bonus of 100 PKR! To encourage the minimalist investor experience.
                earnings = 0.0
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            onResult(true, "Registration successful! Welcome bonus of 100 PKR added.")
        }
    }

    fun logout() {
        _currentUser.value = null
        _isAdminMode.value = false
    }

    fun makeDeposit(amount: Double, senderNumber: String, transactionId: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "User session not active")
            return
        }
        if (amount <= 0.0) {
            onResult(false, "Please enter a valid amount")
            return
        }
        if (senderNumber.isBlank() || transactionId.isBlank()) {
            onResult(false, "Sender number and TxID are required")
            return
        }
        viewModelScope.launch {
            val deposit = DepositEntity(
                userEmail = user.email,
                amount = amount,
                senderNumber = senderNumber.trim(),
                transactionId = transactionId.trim(),
                status = "Pending"
            )
            repository.insertDeposit(deposit)
            onResult(true, "Deposit submitted successfully! Awaiting Admin approval (you can toggle Admin Mode to approve it easily).")
        }
    }

    fun buyPlan(planName: String, price: Double, dailyReturn: Double, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "User session not active")
            return
        }
        if (user.balance < price) {
            onResult(false, "Insufficient balance! Please deposit money via EasyPaisa.")
            return
        }
        viewModelScope.launch {
            // Deduct balance and purchase plan
            val updatedUser = user.copy(balance = user.balance - price)
            repository.updateUser(updatedUser)
            
            val plan = PlanEntity(
                userEmail = user.email,
                planName = planName,
                price = price,
                dailyReturn = dailyReturn,
                totalDays = 90,
                daysCollected = 0,
                lastCollectedTimestamp = 0L // collect immediately!
            )
            repository.insertPlan(plan)
            _currentUser.value = updatedUser
            onResult(true, "Plan '$planName' purchased successfully!")
        }
    }

    fun collectDailyReturn(plan: PlanEntity) {
        val user = _currentUser.value ?: return
        val now = System.currentTimeMillis()
        val nextCollectTime = plan.lastCollectedTimestamp + 24 * 60 * 60 * 1000
        val isCooldowned = now < nextCollectTime

        if (isCooldowned) {
            showMessage("Already collected today! Use Fast-Forward on profile page to collect again.")
            return
        }

        if (plan.daysCollected >= plan.totalDays) {
            showMessage("This plan has matured (90 days completed). Congratulations!")
            return
        }

        viewModelScope.launch {
            val updatedPlan = plan.copy(
                daysCollected = plan.daysCollected + 1,
                lastCollectedTimestamp = now
            )
            val updatedUser = user.copy(
                balance = user.balance + plan.dailyReturn,
                earnings = user.earnings + plan.dailyReturn
            )
            repository.updatePlan(updatedPlan)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            showMessage("+${plan.dailyReturn.toInt()} PKR returns collected successfully!")
        }
    }

    fun testCollectImmediate(plan: PlanEntity) {
        val user = _currentUser.value ?: return
        if (plan.daysCollected >= plan.totalDays) {
            showMessage("This plan has matured.")
            return
        }
        viewModelScope.launch {
            val updatedPlan = plan.copy(
                daysCollected = plan.daysCollected + 1,
                lastCollectedTimestamp = System.currentTimeMillis()
            )
            val updatedUser = user.copy(
                balance = user.balance + plan.dailyReturn,
                earnings = user.earnings + plan.dailyReturn
            )
            repository.updatePlan(updatedPlan)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            showMessage("Fast-Collect: +${plan.dailyReturn.toInt()} PKR collected!")
        }
    }

    fun adminApproveDeposit(deposit: DepositEntity) {
        if (deposit.status == "Approved") return
        viewModelScope.launch {
            val updatedDeposit = deposit.copy(status = "Approved")
            repository.updateDeposit(updatedDeposit)
            
            // Credit user balance
            val beneficiary = repository.getUserByEmail(deposit.userEmail)
            if (beneficiary != null) {
                val updatedUser = beneficiary.copy(balance = beneficiary.balance + deposit.amount)
                repository.updateUser(updatedUser)
                // If it is the current user logged in, reflect in UI immediately
                if (_currentUser.value?.email == deposit.userEmail) {
                    _currentUser.value = updatedUser
                }
            }
            showMessage("Deposit of ${deposit.amount.toInt()} PKR approved successfully!")
        }
    }

    fun adminRejectDeposit(deposit: DepositEntity) {
        if (deposit.status != "Pending") return
        viewModelScope.launch {
            val updatedDeposit = deposit.copy(status = "Rejected")
            repository.updateDeposit(updatedDeposit)
            showMessage("Deposit transaction rejected.")
        }
    }

    // Speeds up simulation by clearing last collected hours so user can collect returns again immediately
    fun fastForwardAllPlans() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val plansList = userPlans.value
            for (plan in plansList) {
                // Set last collected to 1 day ago so they can collect again
                val resetPlan = plan.copy(lastCollectedTimestamp = 0L)
                repository.updatePlan(resetPlan)
            }
            showMessage("Time Machine: All plans are now ready for Collection!")
        }
    }
}

class MainViewModelFactory(private val repository: InvestmentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
