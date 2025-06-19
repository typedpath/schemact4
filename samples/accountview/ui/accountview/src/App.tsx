import React, { useState, useEffect } from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import { AuthSession, fetchAuthSession, getCurrentUser } from '@aws-amplify/auth';
import './amplify-config';
import { Hub, HubPayload } from '@aws-amplify/core';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Accounts from './Accounts';
import AccountDetails from './AccountDetails';
import onLogin from './functions/onLogin';
import { UserInfo } from './functions/UserInfo';
import TransactionGroupDetail from './TransactionGroupDetail';
import CategoryEditScreen from './CategoryEditScreen';
import AutoCatFiltersEditScreen from './AutoCatFiltersEditScreen';
import { CategoryProvider, useCategories } from './CategoryContext';
import NavBar from './NavBar';

interface AuthUserData {
  userId: string;
  signInDetails?: { loginId: string };
  attributes?: { sub: string; email: string };
}

interface AuthHubPayload extends HubPayload {
  event: string;
  data?: AuthUserData;
}

// Custom hook for auth listener and category updates
const useAuthListener = (
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>,
  setAuthorization: React.Dispatch<React.SetStateAction<string | undefined>>,
  setCategoryOptions: React.Dispatch<React.SetStateAction<string[]>>,
  setError: React.Dispatch<React.SetStateAction<string | null>>,
) => {
  useEffect(() => {
    const handlePostLogin = async (user: any) => {
      try {
        console.log('handlePostLogin called with user:', user);
        const session = await fetchAuthSession();
        const idToken = session.tokens?.idToken?.toString();
        console.log('Fetched idToken:', idToken);
        if (!idToken) throw new Error('No ID token available');
        setAuthorization(idToken);
        const response = await onLogin(idToken);
        console.log('onLogin response:', response);
        const userInfo = response.data;
        setUserInfo(userInfo);
        if (userInfo.categories && Array.isArray(userInfo.categories)) {
          setCategoryOptions(userInfo.categories);
        } else {
          console.warn('No valid categories found in userInfo');
          setCategoryOptions([]);
        }
        setError(null);
      } catch (error: any) {
        console.error('Error in handlePostLogin:', error);
        setError(error.message || 'Failed to authenticate user');
      }
    };

    // Check for existing signed-in user
    const checkUser = async () => {
      try {
        const currentUser = await getCurrentUser();
        console.log('Existing user found on mount:', currentUser);
        handlePostLogin(currentUser);
      } catch (error) {
        console.log('No user signed in on mount:', error);
      }
    };

    checkUser();

    const listener = (data: { payload: AuthHubPayload }) => {
      console.log('Hub event:', data.payload.event, 'Data:', data.payload.data);
      if (data.payload.event === 'signedIn') {
        console.log('Detected signedIn event:', data.payload.data);
        handlePostLogin(data.payload.data);
      }
    };

    console.log('Setting up Hub listener');
    const remove = Hub.listen('auth', listener);
    return () => {
      console.log('Removing Hub listener');
      remove();
    };
  }, [setUserInfo, setAuthorization, setCategoryOptions, setError]);
};

const AuthenticatedApp: React.FC<{
  user: any;
  signOut: (() => void) | undefined;
  userInfo: UserInfo | null;
  Authorization: string | undefined;
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>; // Fixed type
  setSession: React.Dispatch<React.SetStateAction<AuthSession | null>>;
  setAuthorization: React.Dispatch<React.SetStateAction<string | undefined>>;
}> = ({ user, signOut, userInfo, Authorization, setUserInfo, setSession, setAuthorization }) => {
  const { setCategoryOptions } = useCategories();
  const [error, setError] = useState<string | null>(null);

  useAuthListener(setUserInfo, setAuthorization, setCategoryOptions, setError);

  console.log('AuthenticatedApp rendering, user:', user);

  if (error) {
    return <div style={{ color: 'red', textAlign: 'center' }}>Error: {error}</div>;
  }

  return (
    <Router>
      <div style={{ padding: '20px' }}>
        <NavBar
          signOut={signOut}
          setUserInfo={setUserInfo}
          setAuthorization={setAuthorization}
          setSession={setSession}
        />
        <div style={{ textAlign: 'center' }}>
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
            <Route
              path="/categories"
              element={<CategoryEditScreen setUserInfo={setUserInfo} />} // Added setUserInfo prop
            />
            <Route
              path="/autocfilters"
              element={<AutoCatFiltersEditScreen userInfo={userInfo} setUserInfo={setUserInfo} />}
            />
          </Routes>
        </div>
      </div>
    </Router>
  );
};

const App: React.FC = () => {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [Authorization, setAuthorization] = useState<string | undefined>(undefined);

  return (
    <Authenticator>
      {({ signOut, user }) => (
        <CategoryProvider>
          {user ? (
            <AuthenticatedApp
              user={user}
              signOut={signOut}
              userInfo={userInfo}
              Authorization={Authorization}
              setUserInfo={setUserInfo}
              setSession={setSession}
              setAuthorization={setAuthorization}
            />
          ) : (
            <div style={{ padding: '20px', textAlign: 'center' }}>
              <h1>Please sign in</h1>
            </div>
          )}
        </CategoryProvider>
      )}
    </Authenticator>
  );
};

export default App;