package ru.tpenskaya.itmo.sd.stock.entity

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Shares(
    @Id
    var id: String = UUID.randomUUID().toString(),
    var companyId: String? = null,
    var ownerId: String? = null
)
