package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone")
    fun observeUserByPhone(phone: String): Flow<User?>

    @Query("SELECT * FROM users ORDER BY phoneNumber ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isBanned = :isBanned WHERE phoneNumber = :phone")
    suspend fun updateUserBanStatus(phone: String, isBanned: Boolean)

    @Query("UPDATE users SET balance = :newBalance WHERE phoneNumber = :phone")
    suspend fun updateUserBalance(phone: String, newBalance: Double)

    // --- Support Tickets ---
    @Query("SELECT * FROM support_tickets WHERE userPhone = :phone ORDER BY timestamp DESC")
    fun observeTicketsForUser(phone: String): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets ORDER BY timestamp DESC")
    fun observeAllTickets(): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket)

    @Update
    suspend fun updateTicket(ticket: SupportTicket)

    @Query("DELETE FROM support_tickets WHERE id = :id")
    suspend fun deleteTicketById(id: Int)

    // --- Promo Codes ---
    @Query("SELECT * FROM promo_codes WHERE code = :code")
    suspend fun getPromoCode(code: String): PromoCode?

    @Query("SELECT * FROM promo_codes ORDER BY code ASC")
    fun observeAllPromoCodes(): Flow<List<PromoCode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoCode(promo: PromoCode)

    @Delete
    suspend fun deletePromoCode(promo: PromoCode)

    @Query("DELETE FROM promo_codes WHERE code = :code")
    suspend fun deletePromoCodeByString(code: String)

    // --- Transactions ---
    @Query("SELECT * FROM transactions WHERE userPhone = :phone ORDER BY timestamp DESC")
    fun observeTransactionsForUser(phone: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // --- Game Settings ---
    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun observeGameSettings(): Flow<GameSettings?>

    @Query("SELECT * FROM game_settings WHERE id = 1")
    suspend fun getGameSettings(): GameSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameSettings(settings: GameSettings)
}
