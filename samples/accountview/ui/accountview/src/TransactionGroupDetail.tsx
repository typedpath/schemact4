import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCurrentUser, fetchAuthSession } from 'aws-amplify/auth';
import getTransactionGroup2 from './functions/getTransactionGroup2';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-quartz.css';
import { ColDef, RowClassParams, CellValueChangedEvent, ICellRendererParams } from 'ag-grid-community';
import './Transactions.css';
import AmountHeaderComponent from './AmountHeaderComponent';
import { UserInfo } from './functions/UserInfo';
import { useCategories } from './CategoryContext';
import categorizeTransactions, { TransactionUpdate } from './functions/categorizeTransactions';
import PivotTableComponent from './PivotTable';
import { TransactionGroup } from './functions/TransactionGroup';

type Transaction = UserInfo['accounts'][number]['transactionGroups'][number]['transactions'][number];
type TransactionGroupType = UserInfo['accounts'][number]['transactionGroups'][number];

const getRowBackgroundColor = (amountInPence: number): string => {
  const amountInPounds = amountInPence / 100;
  if (amountInPounds <= -1000) {
    return '#FF0000';
  }
  if (amountInPounds >= 1000) {
    return '#00FF00';
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
  const navigate = useNavigate();
  const [transactionGroup, setTransactionGroup] = useState<TransactionGroup | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [transactionUpdates, setTransactionUpdates] = useState<Map<number, Transaction>>(new Map());
  const { categoryOptions } = useCategories();

  const updateTransaction = (key: number, updatedTransaction: Transaction) => {
    setTransactionUpdates((prev) => {
      const newMap = new Map(prev);
      newMap.set(key, updatedTransaction);
      return newMap;
    });
    setTransactionGroup((prev) => {
      if (!prev) return prev;
      const updatedTransactions = prev.transactions.map((tx, index) =>
        index === key ? updatedTransaction : tx
      );
      return { ...prev, transactions: updatedTransactions };
    });
  };

  const handleSaveUpdates = async () => {
    if (transactionUpdates.size === 0) {
      alert('No updates to save.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();

      if (!idToken) {
        throw new Error('No ID token available');
      }

      const updates: TransactionUpdate[] = Array.from(transactionUpdates.entries()).map(([index, tx]) => ({
        index,
        transaction: {
          date: tx.date,
          subcategory: tx.subcategory,
          amount: tx.amount,
          memo: tx.memo,
          category: tx.category || '',
          frequency: tx.frequency || '',
          sourceCategory: tx.sourceCategory || '',
          categorized: tx.categorized,
        },
      }));

      const response = await categorizeTransactions(
        fromDate!,
        toDate!,
        accountNumber!,
        updates,
        idToken
      );

      setTransactionGroup(response.data);
      setTransactionUpdates(new Map());
      setLoading(false);
      console.log('categorizeTransactions success:', response.data);
    } catch (err: any) {
      const errorMessage = err.message || 'Failed to save transaction updates';
      setError(errorMessage);
      setLoading(false);
      console.error('categorizeTransactions error:', err);
    }
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
        headerName: 'Auto-Categorize',
        width: 100,
        minWidth: 80,
        headerClass: 'header-center',
        cellClass: 'cell-center',
        editable: false, // Prevent editing
        cellRenderer: (params: ICellRendererParams<Transaction>) => {
          if (params.data?.categorized) {
            return ''; // No button if categorized
          }
          return (
            <button
              onClick={() => navigate(`/autocfilters?pattern=${encodeURIComponent(params.data?.memo || '')}`)}
              style={{
                padding: '4px 8px',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '16px', // Ensure emoji renders clearly
                lineHeight: '1', // Align emoji vertically
              }}
              title="Create auto-categorization rule" // Accessibility tooltip
            >
              💡
            </button>
          );
        },
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
        cellEditor: 'agSelectCellEditor',
        cellEditorParams: {
          values: ['', ...categoryOptions],
        },
        valueSetter: (params) => {
          console.log('category valueSetter called, newValue:', params.newValue, 'oldValue:', params.oldValue);
          if (params.newValue !== params.oldValue) {
            params.data.category = params.newValue || '';
            return true;
          }
          return false;
        },
        cellRenderer: (params: ICellRendererParams<Transaction>) => {
          return params.value || 'Select...';
        },
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
    [categoryOptions]
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

        const response = await getTransactionGroup2(fromDate!, toDate!, accountNumber!, idToken);
        setTransactionGroup(response.data);
        console.log('TransactionGroup Response:', JSON.stringify(response.data, null, 2)); // Debug: Log for TransactionGroup.ts evaluation
        setLoading(false);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch transactions');
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [accountNumber, group, fromDate, toDate]);

  const onCellValueChanged = async (event: CellValueChangedEvent<Transaction>) => {
    if (event.colDef.field === 'category' || event.colDef.field === 'categorized') {
      const updatedTransaction = {
        ...event.data,
        category: event.colDef.field === 'category' ? event.newValue || '' : event.data.category,
        categorized: event.colDef.field === 'categorized' ? event.newValue : event.data.categorized,
      };
      const key = event.rowIndex!;
      updateTransaction(key, updatedTransaction);
      event.api.refreshCells({ force: true });
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>Error: {error}</div>;
  }

  if (!transactionGroup) {
    return <div>No transaction group data available.</div>;
  }

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      <h2>Transactions for Account {accountNumber}</h2>
      <p>Group: {group}</p>
      <p>From: {fromDate} To: {toDate}</p>
      <button
        onClick={() => navigate('/categories')}
        style={{ marginBottom: '10px', padding: '5px 10px' }}
      >
        Manage Categories
      </button>
      <button
        onClick={handleSaveUpdates}
        style={{ padding: '5px 10px', marginLeft: '10px' }}
        disabled={transactionUpdates.size === 0}
      >
        Save Updates
      </button>
      {transactionGroup.transactions.length === 0 ? (
        <p>No transactions found.</p>
      ) : (
        <div className="ag-theme-quartz" style={{ height: '500px', width: '100%', marginBottom: '20px' }}>
          <AgGridReact
            rowData={transactionGroup.transactions}
            columnDefs={columnDefs}
            defaultColDef={{
              resizable: true,
              sortable: true,
              filter: true,
              editable: true,
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
            getRowStyle={(params: RowClassParams<Transaction>) => ({
              backgroundColor: getRowBackgroundColor(params.data?.amount ?? 0),
            })}
            onCellValueChanged={onCellValueChanged}
            singleClickEdit={true}
            onCellClicked={(event) => console.log('Cell clicked:', event.colDef.field, event.rowIndex)}
            onCellEditingStarted={(event) => console.log('Cell editing started:', event.colDef.field, event.rowIndex)}
            onCellEditingStopped={(event) => console.log('Cell editing stopped:', event.colDef.field, event.rowIndex, 'newValue:', event.newValue)}
          />
        </div>
      )}
      <div>transactionUpdates: {transactionUpdates.size}</div>

      {/* Render Transaction Group Pivot Tables */}
      {transactionGroup.pivotTables.length > 0 && (
        <div style={{ marginTop: '20px' }}>
          <h3>Transaction Group Pivot Tables</h3>
          {transactionGroup.pivotTables.map((pivotTable, index) => (
            <PivotTableComponent key={`group-${index}`} pivotTable={pivotTable} />
          ))}
        </div>
      )}

      {/* Placeholder for Account-Level Pivot Tables */}
      {/*
      {account?.pivotTables.length > 0 && (
        <div style={{ marginTop: '20px' }}>
          <h3>Account-Level Pivot Tables</h3>
          {account.pivotTables.map((pivotTable, index) => (
            <PivotTableComponent key={`account-${index}`} pivotTable={pivotTable} />
          ))}
        </div>
      )}
      */}
    </div>
  );
};

export default TransactionGroupDetail;