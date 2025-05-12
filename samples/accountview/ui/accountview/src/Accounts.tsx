import React, { useCallback } from 'react';

import { ColDef, AllCommunityModule, ModuleRegistry } from 'ag-grid-community';


import { AgGridReact } from 'ag-grid-react'; // React Data Grid Component


import AddAccount from './AddAccount';
import addAccount, { Account } from './functions/addAccount';
import { UserInfo } from './functions/UserInfo';




import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

// Register all Community features
// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);



interface AccountsProps {
  accounts: UserInfo['accounts'];
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
  Authorization_in: string;
}

const Accounts: React.FC<AccountsProps> = ({ accounts, setUserInfo, Authorization_in }) => {
  // Log accounts to verify data
  console.log('Accounts data:', accounts);

  const colDefs: ColDef<UserInfo['accounts'][number]>[] = [
    { field: 'name', filter: true, editable: true, flex: 1, minWidth: 150 },
    { field: 'sortCode', editable: true, width: 120 },
    { field: 'accountNumber', editable: true, width: 150 },
  ];

  // Auto-size columns when the grid is ready
  const onGridReady = useCallback((params: any) => {
    params.api.sizeColumnsToFit();
  }, []);

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
      <AddAccount onAccountAdded={onAccountAdded} />
      <div className="ag-theme-alpine" style={{ width: '100%' }}>
        <AgGridReact
          domLayout="autoHeight"
          rowData={accounts}
          columnDefs={colDefs}
          getRowId={(params) => params.data.accountNumber /*|| String(params.node.id)*/}
          onGridReady={onGridReady}
          onCellValueChanged={(event) => {
            console.log('Cell value changed:', event);
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
    </div>
  );
};

export default Accounts;