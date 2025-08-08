import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAuthSession } from 'aws-amplify/auth';
import { AxiosResponse } from 'axios';
import { useCategories } from './CategoryContext';
import saveCategories from './functions/saveCategories';
import { UserInfo } from './functions/UserInfo';

interface CategoryEditScreenProps {
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
}

const CategoryEditScreen: React.FC<CategoryEditScreenProps> = ({ setUserInfo }) => {
  const navigate = useNavigate();
  const { categoryOptions, setCategoryOptions } = useCategories();
  const [newCategory, setNewCategory] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleAddCategory = () => {
    if (newCategory.trim() && !categoryOptions.includes(newCategory.trim())) {
      setCategoryOptions([...categoryOptions, newCategory.trim()]);
      setNewCategory('');
      setSuccess(null);
      setError(null);
    }
  };

  const handleDeleteCategory = (category: string) => {
    setCategoryOptions(categoryOptions.filter((cat) => cat !== category));
    setSuccess(null);
    setError(null);
  };

  const handleSaveCategories = async () => {
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();
      if (!idToken) throw new Error('No ID token available');

      if (categoryOptions.length === 0) throw new Error('No categories to save');

      const response: AxiosResponse<UserInfo> = await saveCategories(idToken, categoryOptions);
      setSuccess('Categories saved successfully!');
      setUserInfo(response.data); // Update userInfo
      setCategoryOptions(response.data.categories || categoryOptions);
    } catch (err: any) {
      setError(err.message || 'Failed to save categories');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <h2>Categories</h2>
      {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}
      {success && <div style={{ color: 'green', marginBottom: '20px' }}>{success}</div>}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={newCategory}
          onChange={(e) => setNewCategory(e.target.value)}
          placeholder="Enter new category"
          style={{ padding: '8px', marginRight: '10px' }}
          disabled={loading}
        />
        <button
          onClick={handleAddCategory}
          style={{ padding: '8px' }}
          disabled={loading || !newCategory.trim()}
        >
          Add Category
        </button>
      </div>
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={handleSaveCategories}
          disabled={loading || categoryOptions.length === 0}
          style={{
            padding: '8px',
            backgroundColor: loading || categoryOptions.length === 0 ? '#ccc' : '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: loading || categoryOptions.length === 0 ? 'not-allowed' : 'pointer',
          }}
        >
          {loading ? 'Saving...' : 'Save Categories'}
        </button>
      </div>
      <h3>Current Categories</h3>
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
                disabled={loading}
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