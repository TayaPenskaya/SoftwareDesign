package ru.tpenskaya.itmo.sd.stock.exception


open class ServiceException(reason: String): RuntimeException(reason)

class NoSuchCompanyException(id: String): ServiceException(
    "Company with id=$id was not found"
)

