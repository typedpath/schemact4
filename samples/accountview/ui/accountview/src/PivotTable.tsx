import React, { useMemo } from 'react';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-quartz.css';
import { ColDef, CellClassParams, ValueFormatterParams, CellStyle } from 'ag-grid-community';
import { UserInfo } from './functions/UserInfo';
import './Transactions.css';

type PivotTable = UserInfo['accounts'][number]['pivotTables'][number];

interface PivotTableProps {
  pivotTable: PivotTable;
}

const PivotTableComponent: React.FC<PivotTableProps> = ({ pivotTable }) => {
  // Prepare pivot table data for AG Grid
  const pivotTableData = useMemo(() => {
    const { header, valueColumns } = pivotTable;
    const rows: any[] = [];

    // Create rows for each label (e.g., category)
    header.labels.forEach((label, index) => {
      const row: any = { [header.labelTitle]: label };
      valueColumns.forEach((col) => {
        row[col.header] = (col.values[index] / 100).toFixed(2); // Convert pence to pounds
      });
      rows.push(row);
    });

    // Add footer row
    const footerRow: any = { [header.labelTitle]: header.footer };
    valueColumns.forEach((col) => {
      footerRow[col.header] = (col.footer / 100).toFixed(2); // Convert pence to pounds
    });
    rows.push(footerRow);

    console.log('PivotTable Data:', JSON.stringify(rows, null, 2)); // Debug: Log formatted data
    console.log('Footer Value:', header.footer); // Debug: Log footer value
    return rows;
  }, [pivotTable]);

  // Define pivot table column definitions
  const pivotTableColumnDefs: ColDef[] = useMemo(() => {
    return [
      {
        field: pivotTable.header.labelTitle, // e.g., "category"
        headerName: pivotTable.header.labelTitle,
        pinned: 'left',
        width: 150,
        cellClass: (params: CellClassParams) => {
          const rowLabel = params.data[pivotTable.header.labelTitle]?.trim().toLowerCase();
          const footerLabel = pivotTable.header.footer?.trim().toLowerCase();
          const isFooter = rowLabel === footerLabel;
          console.log(
            'Label Column - Row:',
            params.data[pivotTable.header.labelTitle],
            'Footer:',
            pivotTable.header.footer,
            'Is Footer:',
            isFooter
          ); // Debug
          return isFooter ? ['footer-cell'] : [];
        },
      },
      ...pivotTable.valueColumns.map((col) => ({
        field: col.header, // e.g., "Jan 24"
        headerName: col.header,
        width: 100,
        cellClass: (params: CellClassParams) => {
          const rowLabel = params.data[pivotTable.header.labelTitle]?.trim().toLowerCase();
          const footerLabel = pivotTable.header.footer?.trim().toLowerCase();
          const isFooter = rowLabel === footerLabel;
          console.log(
            'Value Column - Row:',
            params.data[pivotTable.header.labelTitle],
            'Footer:',
            pivotTable.header.footer,
            'Is Footer:',
            isFooter
          ); // Debug
          return ['cell-right', isFooter ? 'footer-cell' : ''];
        },
        valueFormatter: (params: ValueFormatterParams) => {
          const value = parseFloat(params.value);
          return isNaN(value) ? '' : `£${value.toFixed(2)}`;
        },
        cellStyle: (params: ValueFormatterParams): CellStyle => {
          const value = parseFloat(params.value);
          return isNaN(value) ? {} : { color: value < 0 ? 'red' : 'black' };
        },
      })),
    ];
  }, [pivotTable]);

  return (
    <div>
      <h3>{pivotTable.header.labelTitle} Pivot Table</h3>
      <div
        className="ag-theme-quartz"
        style={{ height: `${pivotTableData.length * 40 + 60}px`, width: '100%' }}
      >
        <AgGridReact
          rowData={pivotTableData}
          columnDefs={pivotTableColumnDefs}
          defaultColDef={{
            resizable: true,
            sortable: false,
            filter: false,
            editable: false,
          }}
          domLayout="autoHeight"
        />
      </div>
    </div>
  );
};

export default PivotTableComponent;