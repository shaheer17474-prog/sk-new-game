package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phoneNumber: String,
    val password: String,
    val balance: Double = 500.0, // starts with some demo coins to play
    val vipLevel: Int = 1,       // 1 to 50
    val isBanned: Boolean = false,
    val referCount: Int = 0,
    val lastDailyRewardClaimed: Long = 0L
)

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userPhone: String,
    val message: String,
    val reply: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "promo_codes")
data class PromoCode(
    @PrimaryKey val code: String,
    val rewardAmount: Double,
    val isClaimed: Boolean = false
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userPhone: String,
    val type: String, // "DEPOSIT" or "WITHDRAW"
    val amount: Double,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val paymentDetails: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey val id: Int = 1,
    val forcedCrashMultiplier: Double? = null, // null means random, 1.0 means force crash immediately
    val adminBroadcast: String = "Welcome to Tower Aviator! Fly high and multiply your earnings."
)
