package schemact.domain

class Module(val name: String, val version:String, val functions: MutableList<Function> = mutableListOf()) {
}