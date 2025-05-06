import React, { useState, useEffect } from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css'; // Import default Amplify UI styles
import { AuthSession, fetchAuthSession } from '@aws-amplify/auth';
import './amplify-config';
import { Hub, HubPayload } from '@aws-amplify/core';
import GridExample from './GridGxample';
import onLogin from './functions/onLogin';
import UploadFileClient from './functions/UploadFileClient';

interface AuthUserData {
  userId: string;
  signInDetails?: { loginId: string };
  attributes?: { sub: string; email: string };
}

interface AuthHubPayload extends HubPayload {
  event: string;
  data?: AuthUserData; // Refine based on Amplify Gen 2 types
}


const App: React.FC = () => {

  const [hasNotifiedLogin, setHasNotifiedLogin] = useState<boolean>(false)
  const [session, setSession] = useState<AuthSession | null>(null)

  useEffect(() => {
    const listener = (data: { payload: AuthHubPayload }) => {
      if (data.payload.event === 'signedIn') {
        console.log('User signed in:', data.payload.data);
        //navigate('/protected');
        handlePostLogin(data.payload.data);
      }
    };

    //tell grok
    const remove = Hub.listen('auth', listener);
    return () => remove();
  }/*, [navigate]*/);

  const handlePostLogin = async (user: any) => {
    if (hasNotifiedLogin) return; // Prevent duplicate calls
    try {
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();
      if (!idToken) throw new Error('No ID token');
      console.log('post login x ')
      await onLogin(idToken)
      setHasNotifiedLogin(true);
    } catch (error) {
      console.error('Error calling Lambda:', error);
    }
  };


  return (
    <Authenticator>
      {({ signOut, user }) => (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          {user ? (
            <>
              <button
                onClick={async () => {
                  try {
                    const session = await fetchAuthSession();
                    console.log('JWT Token:', session.tokens?.idToken?.toString());
                    setSession(null);
                    setHasNotifiedLogin(false);
                    if (signOut) await signOut();
                  } catch (error) {
                    console.error('Error during sign-out:', error);
                  }
                }}
                style={{ padding: '10px', marginTop: '10px' }}
              >
                Sign Out xx
              </button>
              <div>
                <UploadFileClient />
                <GridExample />

              </div>
            </>
          ) : (
            <h1>Please sign in</h1>
          )}
        </div>
      )}
    </Authenticator>
  );
};

export default App;

