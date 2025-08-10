import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { UserInfo } from './functions/UserInfo';
import uploadTransactionGroup from './functions/uploadTransactionGroup';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-quartz.css'; // Match Accounts.tsx
import { ColDef, RowClickedEvent } from 'ag-grid-community';
import { TransactionGroup } from './functions/TransactionGroup';

interface AccountDetailsProps {
  userInfo: UserInfo | null;
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
  Authorization_in: string;
}


const AccountDetails: React.FC<AccountDetailsProps> = ({ userInfo, setUserInfo, Authorization_in }) => {
  const { accountNumber } = useParams<{ accountNumber: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState<{
    file: File | null;
    fromInclusiveDate: string;
    toInclusiveDate: string;
  }>({
    file: null,
    fromInclusiveDate: '',
    toInclusiveDate: '',
  });
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const account = userInfo?.accounts.find(acc => acc.accountNumber === accountNumber);

  // Column definitions for AgGridReact
  const columnDefs: ColDef<TransactionGroup>[] = useMemo(
    () => [
      {
        field: 'fromInclusiveDate',
        headerName: 'From Date',
        sortable: true,
        filter: 'agDateColumnFilter',
        minWidth: 150,
      },
      {
        field: 'toInclusiveDate',
        headerName: 'To Date',
        sortable: true,
        filter: 'agDateColumnFilter',
        minWidth: 150,
      },
      {
        field: 'rawTransactionFile.filename',
        headerName: 'File Name',
        sortable: true,
        filter: 'agTextColumnFilter',
        minWidth: 200,
      },
      {
        field: 'rawTransactionFile.contentType',
        headerName: 'Content Type',
        sortable: true,
        filter: 'agTextColumnFilter',
        minWidth: 150,
      },
      {
        field: 'rawTransactionFile.uploadTime',
        headerName: 'Upload Time',
        sortable: true,
        filter: 'agDateColumnFilter',
        minWidth: 200,
      },
    ],
    []
  );

  if (!account) {
    return (
      <div style={{ padding: '20px' }}>
        <h2>Account Not Found</h2>
        <button onClick={() => navigate('/')} style={{ marginBottom: '10px' }}>
          Back to Accounts
        </button>
      </div>
    );
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    setFormData(prev => ({ ...prev, file: selectedFile }));
    setError(null);
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const { file, fromInclusiveDate, toInclusiveDate } = formData;

    if (!file) {
      setError('Please select a file');
      return;
    }
    if (!fromInclusiveDate) {
      setError('Please select a from date');
      return;
    }
    if (!toInclusiveDate) {
      setError('Please select a to date');
      return;
    }
    if (new Date(toInclusiveDate) < new Date(fromInclusiveDate)) {
      setError('To date must be after from date');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const response = await uploadTransactionGroup(
        file,
        fromInclusiveDate,
        toInclusiveDate,
        accountNumber!,
        Authorization_in
      );

      if (response.status !== 200) {
        const errorData = response.statusText;
        console.error('Upload error:', errorData);
        throw new Error(errorData || 'Failed to upload file');
      }

      const updatedUserInfo: UserInfo = response.data;
      setUserInfo(updatedUserInfo);

      setFormData({ file: null, fromInclusiveDate: '', toInclusiveDate: '' });
      (e.target as HTMLFormElement).reset();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle row click to navigate to Transactions.tsx
  const handleRowClickx = (params: { data: UserInfo['accounts'][number]['transactionGroups'][number] }) => {
    const { fromInclusiveDate, toInclusiveDate } = params.data;
    // Use "all" as group if subcategory isn't directly available
    const group = 'all'; // Adjust if transactions have a specific subcategory
    navigate(`/accounts/${accountNumber}/transactions/${group}/${fromInclusiveDate}/${toInclusiveDate}`);
  };

  const handleRowClick = (event: RowClickedEvent<TransactionGroup>) => {
    console.log('handleRowClick')
    if (!event.data) {
      console.warn('No data available for clicked row');
      return;
    }
    const { fromInclusiveDate, toInclusiveDate, transactions } = event.data;
    console.log('handleRowClick here ', event.data)
    navigate(`/accounts/${accountNumber}/transactions/${event.data.rawTransactionFile.filename}/${fromInclusiveDate}/${toInclusiveDate}`);
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Account Details</h2>
      <button onClick={() => navigate('/')} style={{ marginBottom: '10px' }}>
        Back to Accounts
      </button>
      <div style={{ marginBottom: '20px' }}>
        <p><strong>Name:</strong> {account.name}</p>
        <p><strong>Sort Code:</strong> {account.sortCode}</p>
        <p><strong>Account Number:</strong> {account.accountNumber}</p>
        <h3>Transaction Groups ({account.transactionGroups.length})</h3>
        {account.transactionGroups.length > 0 ? (
          <div
            className="ag-theme-quartz"
            style={{ height: '300px', width: '100%' }}
          >
            <AgGridReact
              rowData={account.transactionGroups}
              columnDefs={columnDefs}
              defaultColDef={{
                resizable: true,
                sortable: true,
                filter: true,
              }}
              onRowClicked={handleRowClick}
              rowSelection="single"
              animateRows={true}
              pagination={true}
              paginationPageSize={10}
            />
          </div>
        ) : (
          <p>No transaction groups</p>
        )}
      </div>
      <h3>Upload Transaction File</h3>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '10px' }}>
          <div>
            <label htmlFor="fromInclusiveDate">From Date</label>
            <input
              id="fromInclusiveDate"
              type="date"
              name="fromInclusiveDate"
              value={formData.fromInclusiveDate}
              onChange={handleDateChange}
              disabled={isSubmitting}
            />
          </div>
          <div>
            <label htmlFor="toInclusiveDate">To Date</label>
            <input
              id="toInclusiveDate"
              type="date"
              name="toInclusiveDate"
              value={formData.toInclusiveDate}
              onChange={handleDateChange}
              disabled={isSubmitting}
            />
          </div>
          <div>
            <label htmlFor="file">Transaction File</label>
            <input
              id="file"
              type="file"
              onChange={handleFileChange}
              disabled={isSubmitting}
              accept=".csv,.pdf,.txt"
            />
          </div>
          <button
            type="submit"
            disabled={isSubmitting || !formData.file || !formData.fromInclusiveDate || !formData.toInclusiveDate}
          >
            {isSubmitting ? 'Uploading...' : 'Upload File'}
          </button>
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
      </form>
    </div>
  );
};

export default AccountDetails;