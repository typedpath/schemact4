package schemact.aws

//this must match UpdateUserData entity NativeDefinition
typealias UpdateUserData<T> =(update: ((data: T) -> T)?, deserialize: (str: String, version: String) -> T, defaultData: ()->T) -> T