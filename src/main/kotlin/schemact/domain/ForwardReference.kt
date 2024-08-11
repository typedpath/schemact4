package schemact.domain

interface  ForwardReference<T> {
   fun resolve() : T
}