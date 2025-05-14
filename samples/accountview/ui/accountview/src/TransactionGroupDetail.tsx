import React, { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { getCurrentUser, fetchAuthSession } from 'aws-amplify/auth';
import getTransactionGroup from './functions/getTransactionGroup';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-quartz.css'; // Consistent with Accounts.tsx
import { ColDef } from 'ag-grid-community';

interface Transaction {
  date: string;
  subcategory: string;
  amount: number;
  memo: string;
}

const TransactionGroupDetail: React.FC = () => {
  const { accountNumber, group, fromDate, toDate } = useParams<{
    accountNumber: string;
    group: string;
    fromDate: string;
    toDate: string;
  }>();
  const [rowData, setRowData] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Column definitions to match Accounts.tsx style
  const columnDefs: ColDef<Transaction>[] = useMemo(
    () => [
      {
        field: 'date',
        headerName: 'Date',
        sortable: true,
        filter: 'agDateColumnFilter',
        rowGroup: true, // Group by date
        minWidth: 200,
      },
      {
        field: 'subcategory',
        headerName: 'Subcategory',
        sortable: true,
        filter: 'agTextColumnFilter',
        minWidth: 150,
      },
      {
        field: 'amount',
        headerName: 'Amount',
        sortable: true,
        filter: 'agNumberColumnFilter',
        valueFormatter: (params) => `£${params.value.toFixed(2)}`,
        minWidth: 120,
      },
      {
        field: 'memo',
        headerName: 'Memo',
        sortable: true,
        filter: 'agTextColumnFilter',
        flex: 1, // Expand to fill space
        minWidth: 200,
      },
    ],
    []
  );

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        await getCurrentUser();
        const session = await fetchAuthSession();
        const idToken = session.tokens?.idToken?.toString();

        if (!idToken) {
          throw new Error('No ID token available');
        }

        const response = await getTransactionGroup(fromDate!, toDate!, accountNumber!, idToken);
        const transactionGroup = response.data;

        const filteredTransactions = group === 'all'
          ? transactionGroup.transactions
          : transactionGroup.transactions.filter(
            (tx: Transaction) => tx.subcategory.toLowerCase() === group?.toLowerCase()
          );

        setRowData(transactionGroup.transactions);
        setLoading(false);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch transactions');
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [accountNumber, group, fromDate, toDate]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>Error: {error}</div>;
  }

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h2>Transactions for Account {accountNumber}</h2>
      <p>Group: {group}</p>
      <p>From: {fromDate} To: {toDate}</p>
      {rowData.length === 0 ? (
        <p>No transactions found.</p>
      ) : (
        <div
          className="ag-theme-quartz"
          style={{ height: '500px', width: '100%' }}
        >
          <AgGridReact
            rowData={rowData}
            columnDefs={columnDefs}
            defaultColDef={{
              resizable: true,
              sortable: true,
              filter: true,
            }}
            autoGroupColumnDef={{
              headerName: 'Date',
              field: 'date',
              minWidth: 200,
              cellRendererParams: {
                suppressCount: true, // Cleaner group labels
              },
            }}
            groupDisplayType="groupRows"
            animateRows={true}
            pagination={true}
            paginationPageSize={20}
          />
        </div>
      )}
    </div>
  );
};

export default TransactionGroupDetail;