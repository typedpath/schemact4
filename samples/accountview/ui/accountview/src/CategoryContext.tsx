// CategoryContext.tsx
import React, { createContext, useState, useContext } from 'react';

interface CategoryContextType {
  categoryOptions: string[];
  setCategoryOptions: React.Dispatch<React.SetStateAction<string[]>>;
}

const CategoryContext = createContext<CategoryContextType | undefined>(undefined);

export const CategoryProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [categoryOptions, setCategoryOptions] = useState<string[]>([
    'Groceries',
    'Utilities',
    'Entertainment',
    'Transportation',
    'Dining',
    'Other',
  ]);

  return (
    <CategoryContext.Provider value={{ categoryOptions, setCategoryOptions }}>
      {children}
    </CategoryContext.Provider>
  );
};

export const useCategories = (): CategoryContextType => {
  const context = useContext(CategoryContext);
  if (!context) {
    throw new Error('useCategories must be used within a CategoryProvider');
  }
  return context;
};