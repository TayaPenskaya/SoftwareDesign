package ru.tpenskaya.itmo.sd.stock.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.tpenskaya.itmo.sd.stock.entity.Shares
import ru.tpenskaya.itmo.sd.stock.entity.User
import ru.tpenskaya.itmo.sd.stock.exception.NoSuchCompanyException
import ru.tpenskaya.itmo.sd.stock.exception.ServiceException
import ru.tpenskaya.itmo.sd.stock.model.SharesWithPrice
import ru.tpenskaya.itmo.sd.stock.repository.CompanyRepo
import ru.tpenskaya.itmo.sd.stock.repository.SharesRepo
import ru.tpenskaya.itmo.sd.stock.repository.UserRepo
import java.math.BigDecimal

@Service
class UserService(
    private val userRepo: UserRepo,
    private val sharesRepo: SharesRepo,
    private val companyRepo: CompanyRepo
) {

    fun getUserBalance(id: String) =
        userRepo.findByIdOrNull(id)
            ?.balance
            ?: throw ServiceException("User with id=$id was not found")

    fun registerNewUser(login: String, balance: BigDecimal = BigDecimal(0)): User {
        userRepo.findUserByLogin(login)?.let {
            throw ServiceException("User with login=$login already exists")
        }

        val user = User(login = login, balance = balance)
        return userRepo.save(user)
    }

    fun increaseUserBalance(userId: String, valueToIncrease: BigDecimal) {
        userRepo.increaseBalance(userId, valueToIncrease)
    }

    fun getUserShares(userId: String): List<SharesWithPrice> {
        val sharesByCompanyId: Map<String, List<Shares>> =
            sharesRepo.findByOwnerId(userId).groupBy { it.companyId!! }

        val result = ArrayList<SharesWithPrice>()

        sharesByCompanyId.forEach { (companyId, sharesList) ->
            val company = companyRepo.findById(companyId)
                .orElseThrow { throw NoSuchCompanyException(companyId) }

            sharesList.forEach { shares ->
                result.add(SharesWithPrice(shares, company.sharesPrice))
            }
        }

        return result
    }

    fun decreaseUserBalance(userId: String, valueToDecrease: BigDecimal) {
        val delta = valueToDecrease.multiply(BigDecimal("-1"))
        val curUserBalance = userRepo.findByIdOrNull(userId)?.balance
            ?: throw ServiceException("User with id=$userId was not found")
        if (curUserBalance < delta) {
            throw ServiceException("User with id=$userId has not enough funds")
        }
        userRepo.increaseBalance(userId, delta)
    }
}