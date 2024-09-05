package paramicons.schemact

import schemact.domain.Entity
import schemact.domain.int
import schemact.domain.string


object OpenGraphSchema {
// see here https://ogp.me/

val openGraphImage = Entity(name="OpenGraphImageData", description="An image  which should represent your object within the graph") {
    string(name ="url",  maxLength = 500)
    string(name="type", "A MIME type for this image", 500, optional = true)
    int(name="width", "The number of pixels wide")
    int(name="height", "The number of pixels high")
    string(name="alt", maxLength = 1000,
        description = "A description of what is in the image (not a caption). If the page specifies an og:image it should specify og:image:alt")
}

val OpenGraphTagging = Entity(name = "OpenGraphTagging",
    description = "") {
    string(name = "title", maxLength = 200, description="The title of your object as it should appear within the graph, e.g., \"The Rock\"")
    string(name = "description", maxLength = 500, description="A one to two sentence description of your object")
    string(name = "type", maxLength = 50, description="The type of your object, e.g., \"video.movie\". Depending on the type you specify, other properties may also be required")
    string(name = "url", maxLength = 50, description="The canonical URL of your object that will be used as its permanent ID in the graph, e.g., \"https://www.imdb.com/title/tt0117500/\"")
    containsOne("image", description="", optional=false, type=openGraphImage)
}
}
