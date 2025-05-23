import React, { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { getCurrentUser, fetchAuthSession } from 'aws-amplify/auth';
import getTransactionGroup from './functions/getTransactionGroup';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-quartz.css';
import { ColDef, RowClassParams, CellValueChangedEvent } from 'ag-grid-community';
import './Transactions.css';
import AmountHeaderComponent from './AmountHeaderComponent';
import { UserInfo } from './functions/UserInfo';

type Transaction = UserInfo['accounts'][number]['transactionGroups'][number]['transactions'][number];

const getRowBackgroundColor = (amountInPence: number): string => {
  const amountInPounds = amountInPence / 100;

  if (amountInPounds <= -1000) {
    return '#FF0000'; // Pure red
  }
  if (amountInPounds >= 1000) {
    return '#00FF00'; // Pure green
  }

  if (amountInPounds < 0) {
    const t = Math.abs(amountInPounds) / 1000;
    const r = 255;
    const g = Math.round(255 * (1 - t));
    const b = Math.round(255 * (1 - t));
    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
  } else {
    const t = amountInPounds / 1000;
    const r = Math.round(255 * (1 - t));
    const g = 255;
    const b = Math.round(255 * (1 - t));
    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
  }
};

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
  const [transactionUpdates, setTransactionUpdates] = useState<Map<number, Transaction>>(new Map());

  // Function to update transactionUpdates and rowData
  const updateTransaction = (key: number, updatedTransaction: Transaction) => {

    setTransactionUpdates((prev) => {
      const newMap = new Map(prev);
      newMap.set(key, updatedTransaction);
      return newMap;
    });
    console.log("transactionUpdates", transactionUpdates)
    setRowData((prev) =>
      prev.map((tx, index) => (index === key ? updatedTransaction : tx))
    );
  };


  const columnDefs: ColDef<Transaction>[] = useMemo(
    () => [
      {
        field: 'date',
        headerName: 'Date',
        sortable: true,
        filter: 'agDateColumnFilter',
        width: 120,
        minWidth: 100,
        headerClass: 'header-left',
        cellClass: 'cell-left',
      },
      {
        field: 'subcategory',
        headerName: 'Subcategory',
        sortable: true,
        filter: 'agTextColumnFilter',
        minWidth: 150,
        headerClass: 'header-left',
        cellClass: 'cell-left',
      },
      {
        field: 'amount',
        headerName: 'Amount / £',
        sortable: true,
        filter: 'agNumberColumnFilter',
        valueFormatter: (params) => {
          const amountInPence = params.value as number;
          const amountInPounds = amountInPence / 100;
          return amountInPounds.toFixed(2);
        },
        headerComponent: AmountHeaderComponent,
        cellClass: 'cell-right',
        minWidth: 120,
      },
      {
        field: 'memo',
        headerName: 'Memo',
        sortable: true,
        filter: 'agTextColumnFilter',
        flex: 1,
        minWidth: 200,
        headerClass: 'header-center',
        cellClass: 'cell-center',
      },
      {
        field: 'categorized',
        headerName: 'Categorized',
        sortable: true,
        filter: 'agSetColumnFilter',
        editable: true,
        cellRenderer: 'agCheckboxCellRenderer',
        cellEditor: 'agCheckboxCellEditor',
        width: 120,
        minWidth: 100,
        headerClass: 'header-center',
        cellClass: 'cell-center',
      },
      {
        field: 'category',
        headerName: 'Category',
        sortable: true,
        filter: 'agTextColumnFilter',
        editable: true,
        minWidth: 150,
        headerClass: 'header-center',
        cellClass: (params) => [
          'cell-center',
          !(params && params.data && params.data.categorized) ? 'uncategorized-cell' : '',
        ],
      },
      {
        field: 'frequency',
        headerName: 'Frequency',
        sortable: true,
        filter: 'agTextColumnFilter',
        minWidth: 150,
        headerClass: 'header-center',
        cellClass: (params) => [
          'cell-center',
          !(params && params.data && params.data.categorized) ? 'uncategorized-cell' : '',
        ],
      },
      {
        field: 'sourceCategory',
        headerName: 'Source Category',
        sortable: true,
        filter: 'agTextColumnFilter',
        minWidth: 150,
        headerClass: 'header-center',
        cellClass: (params) => [
          'cell-center',
          !(params && params.data && params.data.categorized) ? 'uncategorized-cell' : '',
        ],
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

        setRowData(transactionGroup.transactions);
        setLoading(false);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch transactions');
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [accountNumber, group, fromDate, toDate]);

  const onCellValueChanged = async (event: CellValueChangedEvent<Transaction>) => {
    if (/*event.colDef.field === 'category' ||*/ event.colDef.field === 'categorized') {
      console.log('onCellValueChanged: event:', event);
      const updatedTransaction = {
        ...event.data,
        //category: event.colDef.field === 'category' ? event.newValue : event.data.category,
        //categorized:  event.colDef.field === 'categorized' 
      };

      const key = event.rowIndex!!
      updateTransaction(key, updatedTransaction); // Collect in transactionUpdates


    }



    // Optionally save to backend
    /*try {
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();
      if (!idToken) throw new Error('No ID token available');

      await fetch('/functions/updateTransaction', {
        method: 'POST',
        headers: { Authorization: `Bearer ${idToken}` },
        body: JSON.stringify({
          accountNumber,
          fromDate,
          toDate,
          transaction: updatedTransaction,
        }),
      });
    } catch (err: any) {
      setError(err.message || 'Failed to save category');
    }
  }*/
  };

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
              minWidth: 120,
              cellRendererParams: {
                suppressCount: true,
              },
            }}
            groupDisplayType="groupRows"
            animateRows={true}
            pagination={true}
            paginationPageSize={20}
            getRowStyle={(params: RowClassParams<Transaction, any>) => ({
              backgroundColor: getRowBackgroundColor(params.data?.amount ?? 0),
            })}
            onCellValueChanged={onCellValueChanged}
          />
        </div>
      )}
      <div>transactionUpdates: {transactionUpdates.size}</div>
    </div>
  );
};

export default TransactionGroupDetail;