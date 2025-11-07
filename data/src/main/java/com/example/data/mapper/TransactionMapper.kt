package com.example.data.mapper

import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = TransactionType.valueOf(type),
        merchantRaw = merchantRaw,
        merchantDisplayName = merchantDisplayName,
        category = category,
        timestamp = timestamp,
        smsBody = smsBody,
        sender = sender,
        referenceNumber = referenceNumber,
        upiVpa = upiVpa,
        needsReview = needsReview
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        merchantRaw = merchantRaw,
        merchantDisplayName = merchantDisplayName,
        category = category,
        timestamp = timestamp,
        smsBody = smsBody,
        sender = sender,
        referenceNumber = referenceNumber,
        upiVpa = upiVpa,
        needsReview = needsReview
    )
}
