package dev.haarolean.gnutardbot.kek

import dev.haarolean.gnutardbot.abilities.LoveAbility

class TestClass {

    private val crudRepositoryImpl: CrudRepositoryImpl = CrudRepositoryImpl()

    open fun test() {
        val a = LoveAbility(null!!)
        crudRepositoryImpl.save(a)
        crudRepositoryImpl.save(a)
    }

}