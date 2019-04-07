package kr.fakenerd

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.CountDownLatch
import javax.persistence.EntityManagerFactory
import kotlin.concurrent.thread

@RunWith(SpringRunner::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class TransactionTest {
    @Autowired
    lateinit var factory: EntityManagerFactory

    @Before
    fun prepare() {
        val em = factory.createEntityManager()
        em.transaction.begin()
        em.createNativeQuery("truncate table item").executeUpdate()
        em.persist(Item(id = 1))
        em.transaction.commit()
    }

    @Test
    @Rollback(false)
    fun testConcurrentTransactions() {
        // Thread 코드 실행 순서 제어를 위한 CountDownLatch 들.
        val t1SelectLatch = CountDownLatch(1)
        val t2SelectLatch = CountDownLatch(1)
        val t1CommitLatch = CountDownLatch(1)

        val t1 = thread {
            val em = factory.createEntityManager()
            em.transaction.begin()
            val item = em.find(Item::class.java, 1)
            t1SelectLatch.countDown()
            t2SelectLatch.await()
            item.a = 100
            em.persist(item)
            em.transaction.commit()
            t1CommitLatch.countDown()
        }
        val t2 = thread {
            t1SelectLatch.await()
            val em = factory.createEntityManager()
            em.transaction.begin()
            val item = em.find(Item::class.java, 1)
            t2SelectLatch.countDown()
            t1CommitLatch.await()
            item.b = 200
            em.persist(item)
            em.transaction.commit()
        }

        // Thread 코드 실행 완료 때 까지 Test 메서드 종료되지 않도록.
        t1.join()
        t2.join()
    }
}
