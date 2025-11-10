package com.example.domain.repository

import com.example.domain.model.SmsMessage
import com.example.domain.model.Transaction

interface SmsRepository {
    suspend fun getAllSmsMessages(onlyBankSms: Boolean = true): List<SmsMessage>
    fun parseTransaction(smsMessage: SmsMessage): Transaction?
}
