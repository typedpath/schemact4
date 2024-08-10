package domain

class StaticWebsite(val name: String, val description: String) {
    class BucketName : StringType(maxLength = 100)

}

