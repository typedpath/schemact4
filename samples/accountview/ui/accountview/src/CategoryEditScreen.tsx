// CategoryEditScreen.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCategories } from './CategoryContext';

const CategoryEditScreen: React.FC = () => {
  const navigate = useNavigate();
  const { categoryOptions, setCategoryOptions } = useCategories();
  const [newCategory, setNewCategory] = useState('');

  const handleAddCategory = () => {
    if (newCategory.trim() && !categoryOptions.includes(newCategory.trim())) {
      setCategoryOptions([...categoryOptions, newCategory.trim()]);
      setNewCategory('');
    }
  };

  const handleDeleteCategory = (category: string) => {
    setCategoryOptions(categoryOptions.filter((cat) => cat !== category));
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <h2>Manage Categories</h2>
      <button
        onClick={() => navigate(-1)}
        style={{ marginBottom: '10px', padding: '5px 10px' }}
      >
        Back to Transactions
      </button>
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={newCategory}
          onChange={(e) => setNewCategory(e.target.value)}
          placeholder="Enter new category"
          style={{ padding: '5px', marginRight: '10px' }}
        />
        <button
          onClick={handleAddCategory}
          style={{ padding: '5px 10px' }}
          disabled={!newCategory.trim()}
        >
          Add Category
        </button>
      </div>
      <h3>Categories</h3>
      {categoryOptions.length === 0 ? (
        <p>No categories available.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {categoryOptions.map((category) => (
            <li
              key={category}
              style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}
            >
              <span>{category}</span>
              <button
                onClick={() => handleDeleteCategory(category)}
                style={{ padding: '2px 8px', background: '#ff4d4d', color: 'white', border: 'none' }}
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default CategoryEditScreen;