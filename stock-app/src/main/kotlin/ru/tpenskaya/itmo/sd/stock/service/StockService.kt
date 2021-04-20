package ru.tpenskaya.itmo.sd.stock.service

import org.springframework.stereotype.Service
import ru.tpenskaya.itmo.sd.stock.entity.Company
import ru.tpenskaya.itmo.sd.stock.entity.Shares
import ru.tpenskaya.itmo.sd.stock.exception.NoSuchCompanyException
import ru.tpenskaya.itmo.sd.stock.exception.ServiceException
import ru.tpenskaya.itmo.sd.stock.repository.CompanyRepo
import ru.tpenskaya.itmo.sd.stock.repository.SharesRepo
import java.math.BigDecimal

@Service
class StockService(
    private val companyRepo: CompanyRepo,
    private val sharesRepo: SharesRepo
) {
    fun findCompanyByName(name: String) = companyRepo.findCompanyByName(name)

    fun createNewCompany(name: String, sharesPrice: BigDecimal): Company {
        val company = Company(name = name, sharesPrice = sharesPrice)
        return companyRepo.save(company)
    }

    fun issueShares(companyId: String, n: Int) {
        val shares = Array(n) { _ -> Shares(companyId = companyId) }
        sharesRepo.saveAll(shares.asIterable())
    }

    fun getCompanySharesPrice(companyId: String): BigDecimal =
        companyRepo.findById(companyId)
            .map { it.sharesPrice }
            .orElseThrow { NoSuchCompanyException(companyId) }

    fun countCompanySharesPrice(companyId: String): Long =
        sharesRepo.countByCompanyId(companyId)

    fun buyCompanyShares(buyerId: String, companyId: String, n: Int) {
        val freeShares = sharesRepo.findFreeSharesByCompanyId(companyId)

        if (n > freeShares.size) {
            throw ServiceException("Unable buy shares. Company(id=$companyId) does not have enough shares.")
        }

        for (i in 0 until n) {
            if (freeShares[i].ownerId != null) error("We work only with one user simultaneously")
            freeShares[i].ownerId = buyerId
        }

        sharesRepo.saveAll(freeShares)
    }

    fun sellCompanyShares(buyerId: String, companyId: String, n: Int) {
        val sharesByBuyer = sharesRepo.findByOwnerIdAndCompanyId(buyerId, companyId)

        if (n > sharesByBuyer.size) {
            throw ServiceException("Unable to sell $n shares. There are only ${sharesByBuyer.size}.")
        }

        for (i in 0 until n) {
            sharesByBuyer[i].ownerId = null
        }

        sharesRepo.saveAll(sharesByBuyer)
    }

    fun updateCompanySharesPrice(companyId: String, percent: Double) {
        companyRepo.findById(companyId).ifPresentOrElse(
            { company ->
                val currentPrice = company.sharesPrice!!
                val delta = currentPrice.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100))
                companyRepo.save(company.apply { sharesPrice = currentPrice.plus(delta) })
            }, {
                throw NoSuchCompanyException(companyId)
            }
        )
    }


}