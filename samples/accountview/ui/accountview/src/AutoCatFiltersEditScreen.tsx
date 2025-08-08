import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAuthSession } from 'aws-amplify/auth';
import { AxiosResponse } from 'axios';
import { useCategories } from './CategoryContext';
import saveAutoCatFilters from './functions/saveAutoCatFilters';
import { UserInfo } from './functions/UserInfo';

interface AutoCatFilter {
  name: string;
  pattern: string;
  type: string;
  category: string;
  frequency: string;
  sourceCategory: string;
}

interface AutoCatFiltersEditScreenProps {
  userInfo: UserInfo | null;
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
}

const AutoCatFiltersEditScreen: React.FC<AutoCatFiltersEditScreenProps> = ({ userInfo, setUserInfo }) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { categoryOptions } = useCategories();
  const [filters, setFilters] = useState<AutoCatFilter[]>([]);
  const [newFilter, setNewFilter] = useState<AutoCatFilter>({
    name: '',
    pattern: searchParams.get('pattern') || '', // Prefill pattern from query param
    type: 'contains',
    category: categoryOptions[0] || '',
    frequency: '',
    sourceCategory: '',
  });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Initialize filters from userInfo.autoCatFilters
  useEffect(() => {
    if (userInfo?.autoCatFilters) {
      setFilters(userInfo.autoCatFilters);
    }
  }, [userInfo]);

  // Update newFilter.category when categoryOptions change
  useEffect(() => {
    if (categoryOptions.length > 0 && !categoryOptions.includes(newFilter.category)) {
      setNewFilter((prev) => ({ ...prev, category: categoryOptions[0] }));
    }
  }, [categoryOptions, newFilter.category]);

  // Update newFilter.pattern if query param changes (e.g., navigating back with a different pattern)
  useEffect(() => {
    const pattern = searchParams.get('pattern') || '';
    setNewFilter((prev) => ({ ...prev, pattern }));
  }, [searchParams]);

  const handleAddOrUpdateFilter = () => {
    if (!newFilter.name.trim() || !newFilter.pattern.trim() || !newFilter.category) {
      setError('Name, pattern, and category are required');
      return;
    }

    if (editingId !== null) {
      // Update existing filter
      setFilters(filters.map((filter) =>
        filter.name === editingId ? newFilter : filter
      ));
      console.log('handleAddOrUpdateFilter newFilter:', newFilter);
      setEditingId(null);
    } else {
      // Add new filter
      setFilters([...filters, newFilter]);
    }

    // Reset form
    setNewFilter({
      name: '',
      pattern: '',
      type: 'contains',
      category: categoryOptions[0] || '',
      frequency: '',
      sourceCategory: '',
    });
    setError(null);
    setSuccess(null);
  };

  const handleEditFilter = (filter: AutoCatFilter) => {
    setNewFilter(filter);
    setEditingId(filter.name);
  };

  const handleDeleteFilter = (name: string) => {
    setFilters(filters.filter((filter) => filter.name !== name));
    setError(null);
    setSuccess(null);
  };

  const handleSaveAutoCatFilters = async () => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    console.log('handleSaveAutoCatFilters');
    try {
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();
      if (!idToken) throw new Error('No ID token available');

      if (filters.length === 0) throw new Error('No filters to save');
      console.log('handleSaveAutoCatFilters filters.length', filters);

      // Validate filters
      for (const filter of filters) {
        if (!filter.name.trim()) throw new Error('All filters must have a name');
        if (!filter.pattern.trim()) throw new Error('All filters must have a pattern');
        //if (!categoryOptions.includes(filter.category)) throw new Error(`Invalid category: ${filter.category}`);
      }
      console.log('handleSaveAutoCatFilters network');

      const response: AxiosResponse<UserInfo> = await saveAutoCatFilters(idToken, filters );
      console.log('handleSaveAutoCatFilters response', response);

      setUserInfo(response.data);
      setSuccess('Auto-categorization filters saved successfully!');
      setFilters(response.data.autoCatFilters || filters);
    } catch (err: any) {
      setError(err.message || 'Failed to save filters');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h2>{editingId !== null ? 'Edit Auto-Categorization Filter' : 'Manage Auto-Categorization Filters'}</h2>
      {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}
      {success && <div style={{ color: 'green', marginBottom: '10px' }}>{success}</div>}
      <div style={{ marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <input
          type="text"
          value={newFilter.name}
          onChange={(e) => setNewFilter({ ...newFilter, name: e.target.value })}
          placeholder="Filter name (e.g., Grocery Filter)"
          style={{ padding: '8px' }}
          disabled={loading}
        />
        <input
          type="text"
          value={newFilter.pattern}
          onChange={(e) => setNewFilter({ ...newFilter, pattern: e.target.value })}
          placeholder="Pattern (e.g., Walmart)"
          style={{ padding: '8px' }}
          disabled={loading}
        />
        <select
          value={newFilter.type}
          onChange={(e) => {
            setNewFilter({ ...newFilter, type: e.target.value });
            console.log('changed newFilter.type ', newFilter, e.target.value);
          }}
          style={{ padding: '8px' }}
          disabled={loading}
        >
          <option value="contains">Contains</option>
          <option value="regex">Regex</option>
        </select>
        <select
          value={newFilter.category}
          onChange={(e) => setNewFilter({ ...newFilter, category: e.target.value })}
          style={{ padding: '8px' }}
          disabled={loading}
        >
          <option value="">Select Category</option>
          {categoryOptions.map((cat) => (
            <option key={cat} value={cat}>
              {cat}
            </option>
          ))}
        </select>
        <input
          type="text"
          value={newFilter.frequency}
          onChange={(e) => setNewFilter({ ...newFilter, frequency: e.target.value })}
          placeholder="Frequency (e.g., monthly)"
          style={{ padding: '8px' }}
          disabled={loading}
        />
        <input
          type="text"
          value={newFilter.sourceCategory}
          onChange={(e) => setNewFilter({ ...newFilter, sourceCategory: e.target.value })}
          placeholder="Source Category (e.g., Uncategorized)"
          style={{ padding: '8px' }}
          disabled={loading}
        />
        <button
          onClick={handleAddOrUpdateFilter}
          style={{ padding: '8px' }}
          disabled={loading || !newFilter.name.trim() || !newFilter.pattern.trim() || !newFilter.category}
        >
          {editingId !== null ? 'Update Filter' : 'Add Filter'}
        </button>
      </div>
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={handleSaveAutoCatFilters}
          disabled={loading || filters.length === 0}
          style={{
            padding: '8px',
            backgroundColor: loading || filters.length === 0 ? '#ccc' : '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: loading || filters.length === 0 ? 'not-allowed' : 'pointer',
          }}
        >
          {loading ? 'Saving...' : 'Save Filters'} filters.length {filters.length}
        </button>
      </div>
      <h3>Current Filters</h3>
      {filters.length === 0 ? (
        <p>No filters available.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {filters.map((filter) => (
            <li
              key={filter.name}
              style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', padding: '10px', border: '1px solid #ddd' }}
            >
              <div>
                <strong>{filter.name}</strong>
                <p>Pattern: {filter.pattern} ({filter.type})</p>
                <p>Category: {filter.category}</p>
                <p>Frequency: {filter.frequency || 'N/A'}</p>
                <p>Source Category: {filter.sourceCategory || 'N/A'}</p>
              </div>
              <div>
                <button
                  onClick={() => handleEditFilter(filter)}
                  style={{ padding: '5px 10px', marginRight: '5px' }}
                  disabled={loading}
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDeleteFilter(filter.name)}
                  style={{ padding: '5px 10px', background: '#ff4d4d', color: 'white', border: 'none' }}
                  disabled={loading}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default AutoCatFiltersEditScreen;