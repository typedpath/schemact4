
export interface UserInfo   {
     loginEvents: string[]
    uploads:  {
         filename: string
        location: string
        contentType: string
        uploadTime: string
         } []
    accounts:  {
         name: string
        sortCode: string
        accountNumber: string
        transactionGroups:  {
             fromInclusiveDate: string
            toInclusiveDate: string
            rawTransactionFile:  {
                 filename: string
                location: string
                contentType: string
                uploadTime: string
                 } 
            transactionFile:  {
                 filename: string
                location: string
                contentType: string
                uploadTime: string
                 } 
            transactions:  {
                 date: string
                subcategory: string
                amount: number
                memo: string
                category: string
                frequency: string
                sourceCategory: string
                categorized: boolean
                 } []
             } []
         } []
    categories: string[]
    autoCatFilters:  {
         name: string
        pattern: string
        type: string
        category: string
        frequency: string
        sourceCategory: string
         } []
     } 
