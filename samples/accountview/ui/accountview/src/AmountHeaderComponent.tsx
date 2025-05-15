// src/AmountHeaderComponent.tsx
import React from 'react';
import { IHeaderParams } from 'ag-grid-community';

interface AmountHeaderProps extends IHeaderParams {
  displayName: string;
}

const AmountHeaderComponent: React.FC<AmountHeaderProps> = (props) => {
  return (
    <div
      style={{
        textAlign: 'right',
        width: '100%',
        paddingRight: '8px',
        display: 'flex',
        justifyContent: 'flex-end',
        alignItems: 'center',
      }}
    >
      {props.displayName}
    </div>
  );
};

export default AmountHeaderComponent;