import React, { useCallback } from 'react';
import { Link } from 'react-router-dom';

import { AgGridReact } from 'ag-grid-react';
import { ColDef, AllCommunityModule, ModuleRegistry } from 'ag-grid-community';
//import ModuleRegistry from '@ag-grid-community/core';
import { useNavigate } from 'react-router-dom';
import AddAccount from './AddAccount';
import addAccount, { Account } from './functions/addAccount';
import { UserInfo } from './functions/UserInfo';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);

interface AccountsProps {
  accounts: UserInfo['accounts'];
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
  Authorization_in: string;
}

const Accounts: React.FC<AccountsProps> = ({ accounts, setUserInfo, Authorization_in }) => {
  const navigate = useNavigate();

  console.log('Accounts data:', accounts);

  const colDefs: ColDef<UserInfo['accounts'][number]>[] = [
    { field: 'name', filter: true, editable: false, flex: 1, minWidth: 150 },
    { field: 'sortCode', editable: false, width: 120 },
    { field: 'accountNumber', editable: false, width: 150 },
  ];

  const onGridReady = useCallback((params: any) => {
    params.api.sizeColumnsToFit();
  }, []);

  async function onAccountAdded(account: Account) {
    try {
      const result = await addAccount(Authorization_in, account );
      setUserInfo(result.data);
    } catch (error) {
      console.error('Failed to add account:', error);
    }
  }

  const onRowClicked = useCallback(
    (event: any) => {
      console.log('***************** onRowClicked')
      const account = event.data as UserInfo['accounts'][number];
      navigate(`/account/${account.accountNumber}`);
    },
    [navigate]
  );

  return (
    <div>
      <h2>Accounts ({accounts.length})</h2>
        <AddAccount onAccountAdded={onAccountAdded} />
      <div className="ag-theme-alpine" style={{ width: '100%' }}>
        <AgGridReact
          domLayout="autoHeight"
          rowData={accounts}
          columnDefs={colDefs}
          getRowId={(params) => params.data.accountNumber}
          onGridReady={onGridReady}
          onRowClicked={onRowClicked}
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