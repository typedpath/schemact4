package org.testedsoftware.accountview

object UserInfoListener {
    fun preSaveFilter(userInfo: UserInfo): UserInfo {

       println("UserInfoListener::preSaveFilter")
       /* val transactionGroups:List<TransactionGroup> =  userInfo.accounts
            .flatMap { it.transactionGroups }

        val cats =   transactionGroups.flatMap { it.transactions }
               .map{it.category}.toSet()

        val userCategories = userInfo.categories.toSet()

        val newCats = cats.minus(userCategories)

        userInfo.categories.addAll(newCats)

        transactionGroups.forEach { it.transactions.clear() }

        */

        return userInfo
    }
}