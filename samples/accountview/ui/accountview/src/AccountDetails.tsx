import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { UserInfo } from './functions/UserInfo';
import uploadTransactonGroup from './functions/uploadTransactonGroup';

interface AccountDetailsProps {
  userInfo: UserInfo | null;
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
  Authorization_in: string;
}

const AccountDetails: React.FC<AccountDetailsProps> = ({ userInfo, setUserInfo, Authorization_in }) => {
  const { accountNumber } = useParams<{ accountNumber: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState<{
    file: File | null;
    fromInclusiveDate: string;
    toInclusiveDate: string;
  }>({
    file: null,
    fromInclusiveDate: '',
    toInclusiveDate: '',
  });
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const account = userInfo?.accounts.find(acc => acc.accountNumber === accountNumber);

  if (!account) {
    return (
      <div style={{ padding: '20px' }}>
        <h2>Account Not Found</h2>
        <button onClick={() => navigate('/')} style={{ marginBottom: '10px' }}>
          Back to Accounts
        </button>
      </div>
    );
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    setFormData(prev => ({ ...prev, file: selectedFile }));
    setError(null);
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const { file, fromInclusiveDate, toInclusiveDate } = formData;

    // Validation
    if (!file) {
      setError('Please select a file');
      return;
    }
    if (!fromInclusiveDate) {
      setError('Please select a from date');
      return;
    }
    if (!toInclusiveDate) {
      setError('Please select a to date');
      return;
    }
    if (new Date(toInclusiveDate) < new Date(fromInclusiveDate)) {
      setError('To date must be after from date');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      /*const formDataToSend = new FormData();
      formDataToSend.append('file', file);
      formDataToSend.append('accountNumber', accountNumber!);
      formDataToSend.append('fromInclusiveDate', fromInclusiveDate);
      formDataToSend.append('toInclusiveDate', toInclusiveDate);

      const response = await fetch('/api/uploadTransactionFile', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${Authorization_in}`,
        },
        body: formDataToSend,
      });
*/
      const response = await uploadTransactonGroup(Authorization_in, file,
        fromInclusiveDate, toInclusiveDate, accountNumber!!)


      if (response.status != 200) {
        const errorData = await response.statusText;
        console.error('Upload error:', errorData);
        throw new Error(errorData || 'Failed to upload file');
      }

      const updatedUserInfo: UserInfo = response.data
      setUserInfo(updatedUserInfo);

      // Reset form
      setFormData({ file: null, fromInclusiveDate: '', toInclusiveDate: '' });
      (e.target as HTMLFormElement).reset();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Account Details</h2>
      <button onClick={() => navigate('/')} style={{ marginBottom: '10px' }}>
        Back to Accounts
      </button>
      <div style={{ marginBottom: '20px' }}>
        <p><strong>Name:</strong> {account.name}</p>
        <p><strong>Sort Code:</strong> {account.sortCode}</p>
        <p><strong>Account Number:</strong> {account.accountNumber}</p>
        <h3>Transaction Groups ({account.transactionGroups.length})</h3>
        {account.transactionGroups.length > 0 ? (
          <ul>
            {account.transactionGroups.map((group, index) => (
              <li key={index}>
                From: {group.fromInclusiveDate}, To: {group.toInclusiveDate}, File: {group.rawTransactionFile.filename} (
                {group.rawTransactionFile.contentType})
              </li>
            ))}
          </ul>
        ) : (
          <p>No transaction groups</p>
        )}
      </div>
      <h3>Upload Transaction File</h3>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '10px' }}>
          <div>
            <label htmlFor="fromInclusiveDate">From Date</label>
            <input
              id="fromInclusiveDate"
              type="date"
              name="fromInclusiveDate"
              value={formData.fromInclusiveDate}
              onChange={handleDateChange}
              disabled={isSubmitting}
            />
          </div>
          <div>
            <label htmlFor="toInclusiveDate">To Date</label>
            <input
              id="toInclusiveDate"
              type="date"
              name="toInclusiveDate"
              value={formData.toInclusiveDate}
              onChange={handleDateChange}
              disabled={isSubmitting}
            />
          </div>
          <div>
            <label htmlFor="file">Transaction File</label>
            <input
              id="file"
              type="file"
              onChange={handleFileChange}
              disabled={isSubmitting}
              accept=".csv,.pdf,.txt"
            />
          </div>
          <button type="submit" disabled={isSubmitting || !formData.file || !formData.fromInclusiveDate || !formData.toInclusiveDate}>
            {isSubmitting ? 'Uploading...' : 'Upload File'}
          </button>
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
      </form>
    </div>
  );
};

export default AccountDetails;