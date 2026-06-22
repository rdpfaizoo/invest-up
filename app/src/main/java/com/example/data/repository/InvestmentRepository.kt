package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.UserEntity
import com.example.data.database.PlanEntity
import com.example.data.database.DepositEntity
import kotlinx.coroutines.flow.Flow

class InvestmentRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val planDao = db.planDao()
    private val depositDao = db.depositDao()

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    fun getUserPlans(email: String): Flow<List<PlanEntity>> = planDao.getUserPlans(email)

    suspend fun insertPlan(plan: PlanEntity) = planDao.insertPlan(plan)

    suspend fun updatePlan(plan: PlanEntity) = planDao.updatePlan(plan)

    fun getUserDeposits(email: String): Flow<List<DepositEntity>> = depositDao.getUserDeposits(email)

    suspend fun insertDeposit(deposit: DepositEntity) = depositDao.insertDeposit(deposit)

    suspend fun updateDeposit(deposit: DepositEntity) = depositDao.updateDeposit(deposit)

    fun getAllDeposits(): Flow<List<DepositEntity>> = depositDao.getAllDeposits()
}
