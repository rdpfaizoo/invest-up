package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val balance: Double = 0.0,
    val earnings: Double = 0.0
)

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val planName: String, // Basic, Pro, Premium
    val price: Double,
    val dailyReturn: Double,
    val totalDays: Int = 90,
    val daysCollected: Int = 0,
    val lastCollectedTimestamp: Long = 0L
)

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val amount: Double,
    val senderNumber: String,
    val transactionId: String,
    val status: String = "Pending", // Pending, Approved
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE userEmail = :email")
    fun getUserPlans(email: String): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity)

    @Update
    suspend fun updatePlan(plan: PlanEntity)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getUserDeposits(email: String): Flow<List<DepositEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity)

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)

    @Query("SELECT * FROM deposits ORDER BY timestamp DESC")
    fun getAllDeposits(): Flow<List<DepositEntity>>
}

@Database(entities = [UserEntity::class, PlanEntity::class, DepositEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun planDao(): PlanDao
    abstract fun depositDao(): DepositDao
}
