package ru.tpenskaya.itmo.sd.stock.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.tpenskaya.itmo.sd.stock.entity.Company

@Repository
interface CompanyRepo: JpaRepository<Company, String> {
    fun findCompanyByName(name: String): Company?
}