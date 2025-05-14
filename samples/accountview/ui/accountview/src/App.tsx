import React, { useState, useEffect } from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import { AuthSession, fetchAuthSession } from '@aws-amplify/auth';
import './amplify-config';
import { Hub, HubPayload } from '@aws-amplify/core';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Accounts from './Accounts';
import AccountDetails from './AccountDetails';
import onLogin from './functions/onLogin';
import { UserInfo } from './functions/UserInfo';
import TransactionGroupDetail from './TransactionGroupDetail';

interface AuthUserData {
  userId: string;
  signInDetails?: { loginId: string };
  attributes?: { sub: string; email: string };
}

interface AuthHubPayload extends HubPayload {
  event: string;
  data?: AuthUserData;
}

const App: React.FC = () => {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [Authorization, setAuthorization] = useState<string | undefined>(undefined);

  useEffect(() => {
    const listener = (data: { payload: AuthHubPayload }) => {
      if (data.payload.event === 'signedIn') {
        console.log('User signed in:', data.payload.data);
        handlePostLogin(data.payload.data);
      }
    };

    const remove = Hub.listen('auth', listener);
    return () => remove();
  }, []);

  const handlePostLogin = async (user: any) => {
    try {
      const session = await fetchAuthSession();
      const idToken = session.tokens?.idToken?.toString();
      setAuthorization(idToken);
      if (!idToken) throw new Error('No ID token');
      console.log('post login idToken:', idToken);
      let userInfo = (await onLogin(idToken)).data;
      setUserInfo(userInfo);
    } catch (error) {
      console.error('Error calling Lambda:', error);
    }
  };

  return (
    <Authenticator>
      {({ signOut, user }) => (
        <Router>
          <div style={{ padding: '20px', textAlign: 'center' }}>
            {user ? (
              <>
                <Routes>
                  <Route
                    path="/"
                    element={
                      userInfo && Authorization ? (
                        <Accounts
                          accounts={userInfo.accounts}
                          setUserInfo={setUserInfo}
                          Authorization_in={Authorization}
                        />
                      ) : (
                        <p>Loading accounts...</p>
                      )
                    }
                  />
                  <Route
                    path="/account/:accountNumber"
                    element={
                      userInfo && Authorization ? (
                        <AccountDetails
                          userInfo={userInfo}
                          setUserInfo={setUserInfo}
                          Authorization_in={Authorization}
                        />
                      ) : (
                        <p>Loading account details...</p>
                      )
                    }
                  />
                  <Route
                    path="/accounts/:accountNumber/transactions/:group/:fromDate/:toDate"
                    element={<TransactionGroupDetail />}
                  />
                </Routes>
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
                  Sign Out
                </button>
              </>
            ) : (
              <h1>Please sign in</h1>
            )}
          </div>
        </Router>
      )}
    </Authenticator>
  );
};

export default App;