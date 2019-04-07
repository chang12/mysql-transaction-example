package kr.fakenerd

import org.hibernate.annotations.DynamicUpdate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@DynamicUpdate
data class Item (
    @Id
    var id: Int? = null,

    var a: Int? = null,
    var b: Int? = null
)
