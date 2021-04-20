package ru.tpenskaya.itmo.sd.stock.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.tpenskaya.itmo.sd.stock.entity.Shares

@Repository
interface SharesRepo: JpaRepository<Shares, String> {

    fun countByCompanyId(companyId: String): Long

    @Query("SELECT new ru.tpenskaya.itmo.sd.stock.entity.Shares(s.id, s.companyId, s.ownerId) FROM Shares s WHERE s.ownerId IS NULL AND s.companyId = :companyId ")
    fun findFreeSharesByCompanyId(companyId: String): List<Shares>

    fun findByCompanyId(companyId: String): List<Shares>

    fun findByOwnerId(userId: String): List<Shares>

    fun findByOwnerIdAndCompanyId(ownerId: String, companyId: String): List<Shares>
}