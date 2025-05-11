import React from 'react';
import { AgGridReact } from 'ag-grid-react';
import { ColDef } from 'ag-grid-community';
import AddAccount from './AddAccount';
import addAccount, { Account } from './functions/addAccount';
import { UserInfo } from './functions/UserInfo';
import 'ag-grid-community/styles/ag-grid.css'; // Core AG Grid CSS
import 'ag-grid-community/styles/ag-theme-alpine.css'; // Theme CSS

interface AccountsProps {
  accounts: UserInfo['accounts'];
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
  Authorization_in: string;
}

const Accounts: React.FC<AccountsProps> = ({ accounts, setUserInfo, Authorization_in }) => {
  // Log accounts to verify data
  console.log('Accounts data:', accounts);

  const colDefs: ColDef<UserInfo['accounts'][number]>[] = [
    { field: 'name', filter: true, editable: true },
    { field: 'sortCode', editable: true },
    { field: 'accountNumber', editable: true },
  ];

  async function onAccountAdded(account: Account) {
    try {
      const result = await addAccount(account, Authorization_in);
      setUserInfo(result.data);
    } catch (error) {
      console.error('Failed to add account:', error);
    }
  }

  return (
    <div>
      <h2>Accounts ({accounts.length})</h2>
      <div className="ag-theme-alpine" style={{ height: '400px', width: '100%' }}>
        <AgGridReact
          rowData={accounts}
          columnDefs={colDefs}
          getRowId={(params) => params.data.accountNumber /*|| String(params.node.id)*/} // Ensure unique row IDs
          onCellValueChanged={(event) => {
            console.log('Cell value changed:', event);
            // Update userInfo when a cell is edited
            setUserInfo(prev => {
              if (!prev || event.rowIndex == null) return prev;
              const updatedAccounts = [...prev.accounts];
              updatedAccounts[event.rowIndex] = event.data;
              return {
                ...prev,
                accounts: updatedAccounts,
              };
            });
          }}
        />
      </div>
      <AddAccount onAccountAdded={onAccountAdded} />
    </div>
  );
};

export default Accounts;