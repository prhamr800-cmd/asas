package com.example.domain.repository

import com.example.domain.model.SubscriptionPlan

interface ISubscriptionRepository {
    fun getPlans(): List<SubscriptionPlan>
    suspend fun purchasePlan(planId: String): Result<String>
}
