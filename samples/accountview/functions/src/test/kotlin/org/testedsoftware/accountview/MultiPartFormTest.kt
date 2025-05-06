import org.testedsoftware.accountview.MultiPart

//TODO convert to automated test
fun main() {

    val parts = MultiPart.read(body, contentType)
    println("==================")
    println(parts.map { "${it.key} => ${it.value} ${System.lineSeparator()} ${String(it.value.body)}" }.joinToString("\n"))
}


const val contentType =  "multipart/form-data; boundary=----WebKitFormBoundarysAdNLKuuDqBZJOPV"
const val body =  """LS0tLS0tV2ViS2l0Rm9ybUJvdW5kYXJ5c0FkTkxLdXVEcUJaSk9QVg0KQ29udGVudC1EaXNwb3NpdGlvbjogZm9ybS1kYXRhOyBuYW1lPSJmaWxlIjsgZmlsZW5hbWU9IkRvbWFpbi5rdCINCkNvbnRlbnQtVHlwZTogYXBwbGljYXRpb24vb2N0ZXQtc3RyZWFtDQoNCnBhY2thZ2Ugc2NoZW1hY3QuZG9tYWluCgpjbGFzcyBEb21haW4odmFsIG5hbWU6IFN0cmluZywgdmFsIHdpbGRjYXJkQ2VydGlmaWNhdGVSZWY6IFN0cmluZywKICAgICAgICAgICAgIHZhbCBjZG5ab25lUmVmZXJlbmNlOiBTdHJpbmcsCiAgICAgICAgICAgICB2YWwgZGVwbG95bWVudHM6IExpc3Q8RGVwbG95bWVudD4gPSBtdXRhYmxlTGlzdE9mKCkpDQotLS0tLS1XZWJLaXRGb3JtQm91bmRhcnlzQWROTEt1dURxQlpKT1BWDQpDb250ZW50LURpc3Bvc2l0aW9uOiBmb3JtLWRhdGE7IG5hbWU9ImFub3RoZXJwYXJhbSINCg0KYW5vdGhlcnZhbHVlDQotLS0tLS1XZWJLaXRGb3JtQm91bmRhcnlzQWROTEt1dURxQlpKT1BWLS0NCg=="""
