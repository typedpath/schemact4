
export interface TransactionGroup   {
     fromInclusiveDate: string
    toInclusiveDate: string
    rawTransactionFile:  {
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
     } 
