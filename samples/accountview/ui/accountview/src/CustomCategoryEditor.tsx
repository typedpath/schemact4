// CustomCategoryEditor.tsx
import React, { forwardRef, useState, useImperativeHandle } from 'react';
import { ICellEditorParams } from 'ag-grid-community';

const CustomCategoryEditor = forwardRef((props: ICellEditorParams, ref) => {
  const [value, setValue] = useState(props.value || '');
  const categoryOptions = [
    'Groceries',
    'Utilities',
    'Entertainment',
    'Transportation',
    'Dining',
    'Other',
  ];

  useImperativeHandle(ref, () => ({
    getValue() {
      return value;
    },
    isCancelBeforeStart() {
      return false;
    },
    isCancelAfterEnd() {
      return false;
    },
  }));

  return (
    <div>
      <input
        type="text"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        list="category-options"
        style={{ width: '100%', padding: '4px', boxSizing: 'border-box' }}
        autoFocus
      />
      <datalist id="category-options">
        {categoryOptions.map((option) => (
          <option key={option} value={option} />
        ))}
      </datalist>
    </div>
  );
});

export default CustomCategoryEditor;