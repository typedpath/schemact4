package domain

class StaticWebsite(val name: String, val description: String) {
    fun bucketNameReference() : ForwardReference<String> =
       object: ForwardReference<String> {
           override fun resolve(): String {
               TODO("Not yet implemented")
           }
       }
}

