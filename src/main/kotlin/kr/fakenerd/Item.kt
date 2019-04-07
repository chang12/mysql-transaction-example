package kr.fakenerd

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Item (
    @Id
    var id: Int? = null,

    var a: Int? = null,
    var b: Int? = null
)
