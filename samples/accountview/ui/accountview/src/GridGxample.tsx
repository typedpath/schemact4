import React, { useState } from 'react';

import { ColDef, AllCommunityModule, ModuleRegistry } from 'ag-grid-community';


import { AgGridReact } from 'ag-grid-react'; // React Data Grid Component

// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);

export type ICar = {
  make: string;
  model: string;
  price: number;
  electric: boolean;
};

const GridExample = () => {



  // Row Data: The data to be displayed.
  const [rowData, setRowData] = useState<ICar[]>([
    { make: "Tesla", model: "Model Y", price: 64950, electric: true },
    { make: "Ford", model: "F-Series", price: 33850, electric: false },
    { make: "Toyota", model: "Corolla", price: 29600, electric: false },
  ]);


  // Column Definitions: Defines the columns to be displayed.
  const [colDefs, setColDefs] = useState<ColDef<ICar>[]>([
    { field: "make", filter:true, editable:true  },
    { field: "model" },
    { field: "price" },
    { field: "electric" }
  ]);
  return (
    // Data Grid will fill the size of the parent container
    <div style={{ height: 500 }}>
      <AgGridReact
        rowData={rowData}
        columnDefs={colDefs}
        onCellValueChanged={(event) => {
          console.log("change:", event)
          //if (event.rowIndex == null) return;
          //const updatedData = [...rowData];
          //updatedData[event.rowIndex] = event.data;
          //setRowData(updatedData);
        }}
      />
    </div>
  )
}

export default GridExample;