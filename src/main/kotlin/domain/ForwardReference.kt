package domain

interface  ForwardReference<T> {
   fun resolve() : T
}