package com.example.ticketbooking.data.pricing
import com.example.ticketbooking.model.PriceBreakdown

object PricingService {
    /**
     * Tính tổng giá với tuỳ chọn phí cố định và phần trăm phí.
     */
    fun computeTotal(unitPrice: Double, count: Int, feePercent: Double = 0.0, fixedFee: Double = 0.0): Double {
        val base = unitPrice * count
        val fees = base * feePercent + fixedFee
        return base + fees
    }

    /**
     * Tính chi tiết giá: base, serviceFee, VAT và tổng.
     */
    fun computeBreakdown(
        unitPrice: Double,
        count: Int,
        feePercent: Double = 0.0,
        fixedFee: Double = 0.0,
        vatPercent: Double = 0.0
    ): PriceBreakdown {
        val base = unitPrice * count
        val serviceFee = base * feePercent + fixedFee
        val vat = (base + serviceFee) * vatPercent
        val total = base + serviceFee + vat
        return PriceBreakdown(base, serviceFee, vat, total)
    }
}