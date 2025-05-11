import React, { useState, useEffect } from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css'; // Import default Amplify UI styles
import { AuthSession, fetchAuthSession } from '@aws-amplify/auth';
import './amplify-config';
import { Hub, HubPayload } from '@aws-amplify/core';
import GridExample from './GridGxample';
import onLogin from './functions/onLogin';
import UploadFileClient from './functions/UploadFileClient';
import Accounts from './Accounts';
import { UserInfo } from './functions/UserInfo';

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

  const [session, setSession] = useState<AuthSession | null>(null)
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [Authorisation, setAuthorization] = useState<string | undefined>(undefined)

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
    try {
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();
      setAuthorization(idToken)
      if (!idToken) throw new Error('No ID token');
      console.log('post login x ')
      let userInfo = await (await onLogin(idToken)).data
      setUserInfo(userInfo)
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
              {userInfo && Authorisation && <Accounts accounts={userInfo?.accounts} setUserInfo={setUserInfo} Authorization_in={Authorisation}></Accounts>}
              <button
                onClick={async () => {
                  try {
                    const session = await fetchAuthSession();
                    console.log('JWT Token:', session.tokens?.idToken?.toString());
                    setSession(null);
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

