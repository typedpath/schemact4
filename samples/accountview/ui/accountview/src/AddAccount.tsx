import React, { useState } from 'react';
import { UserInfo } from './functions/UserInfo';

interface AddAccountProps {
  onAccountAdded: (newAccount: UserInfo['accounts'][number]) => void;
}

// Form input type
interface NewAccountForm {
  name: string;
  sortCode: string;
  accountNumber: string;
}

const AddAccount: React.FC<AddAccountProps> = ({ onAccountAdded }) => {
  const [formData, setFormData] = useState<NewAccountForm>({
    name: '',
    sortCode: '',
    accountNumber: '',
  });
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Basic validation
    if (!formData.name || !formData.sortCode || !formData.accountNumber) {
      setError('All fields are required');
      return;
    }

    setIsSubmitting(true);

    try {
      /*   const response = await fetch('/api/addAccount', {
           method: 'POST',
           headers: {
             'Content-Type': 'application/json',
           },
           body: JSON.stringify({
             name: formData.name,
             sortCode: formData.sortCode,
             accountNumber: formData.accountNumber,
           }),
         });
   
         if (!response.ok) {
           throw new Error('Failed to add account');
         }
   
         const newAccount: UserInfo['accounts'][number] = await response.json();
         // Assuming API returns { name, sortCode, accountNumber, transactionGroups }
   */
      // Notify parent of new account
      onAccountAdded({
        name: formData.name,
        sortCode: formData.sortCode,
        accountNumber: formData.accountNumber,
        transactionGroups: []
      });

      // Reset form
      setFormData({ name: '', sortCode: '', accountNumber: '' });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: '20px' }}>
      <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        helllo
        <input
          type="text"
          name="name"
          placeholder="Account Name"
          value={formData.name}
          onChange={handleInputChange}
          disabled={isSubmitting}
          style={{ padding: '5px' }}
        />
        <input
          type="text"
          name="sortCode"
          placeholder="Sort Code (e.g., XX-XX-XX)"
          value={formData.sortCode}
          onChange={handleInputChange}
          disabled={isSubmitting}
          style={{ padding: '5px' }}
        />
        <input
          type="text"
          name="accountNumber"
          placeholder="Account Number"
          value={formData.accountNumber}
          onChange={handleInputChange}
          disabled={isSubmitting}
          style={{ padding: '5px' }}
        />
        <button type="submit" disabled={isSubmitting} style={{ padding: '5px 10px' }}>
          {isSubmitting ? 'Adding...' : 'Add Account'}
        </button>
      </div>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
};

export default AddAccount;