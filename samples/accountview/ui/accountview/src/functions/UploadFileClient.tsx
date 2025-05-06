import React, { useState } from 'react';
import axios from 'axios';
import { getCurrentUser, fetchAuthSession } from 'aws-amplify/auth';
import { Amplify } from 'aws-amplify';
import { } from '../amplify-config'; // Adjust path if needed
import uploadFile from './uploadFile';

// Configure Amplify (can be moved to app entry point)
//Amplify.configure(amplifyConfig);

const UploadFileClient: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setFile(event.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setMessage('Please select a file.');
      return;
    }

    setUploading(true);
    setMessage('');

    try {
      // Get the current authenticated user
      await getCurrentUser();

      // Fetch the ID token from the auth session
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();

      if (!idToken) {
        throw new Error('No ID token available');
      }

      /*      // Prepare FormData for file upload
            const formData = new FormData();
            formData.append('file', file);
      */
      const response = await uploadFile(idToken, file)

      // Make the POST request with axios
      /* const response = await axios.post(
         'https://your-api-id.execute-api.us-east-1.amazonaws.com/functions/upload', // Replace with your API Gateway endpoint
         formData,
         {
           headers: {
             Authorization: `Bearer ${idToken}`,
             'Content-Type': 'multipart/form-data'
           }
         }
       );
 */
      setMessage(`File uploaded successfully: ${response.data.fileUrl}`);
    } catch (error: any) {
      console.error('Upload error:', error);
      setMessage(`Upload failed: ${error.response?.data?.message || error.message || 'Unknown error'}`);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <h2>Upload File</h2>
      <input type="file" onChange={handleFileChange} disabled={uploading} />
      <button onClick={handleUpload} disabled={uploading || !file}>
        {uploading ? 'Uploading...' : 'Upload'}
      </button>
      {message && <p>{message}</p>}
    </div>
  );
};

export default UploadFileClient;