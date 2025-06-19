import React from 'react';
import { Link } from 'react-router-dom';
import { AuthSession, fetchAuthSession } from '@aws-amplify/auth'; // Add fetchAuthSession import
import { UserInfo } from './functions/UserInfo';
import { useCategories } from './CategoryContext';

interface NavBarProps {
  signOut: (() => void) | undefined;
  setUserInfo: React.Dispatch<React.SetStateAction<UserInfo | null>>;
  setAuthorization: React.Dispatch<React.SetStateAction<string | undefined>>;
  setSession: React.Dispatch<React.SetStateAction<AuthSession | null>>;
}

const NavBar: React.FC<NavBarProps> = ({
  signOut,
  setUserInfo,
  setAuthorization,
  setSession,
}) => {
  const { setCategoryOptions } = useCategories();

  const handleSignOut = async () => {
    try {
      const session = await fetchAuthSession();
      console.log('JWT Token:', session.tokens?.idToken?.toString());
      setSession(null);
      setUserInfo(null);
      setAuthorization(undefined);
      setCategoryOptions([]); // Reset categories on sign-out
      if (signOut) await signOut();
    } catch (error) {
      console.error('Error during sign-out:', error);
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: '#f8f9fa',
        padding: '10px 20px',
        borderBottom: '1px solid #dee2e6',
        position: 'sticky',
        top: 0,
        zIndex: 1000,
      }}
    >
      <div>
        <Link
          to="/"
          style={{
            padding: '8px 16px',
            marginRight: '10px',
            backgroundColor: '#007bff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
          }}
        >
          Accounts
        </Link>
        <Link
          to="/categories"
          style={{
            padding: '8px 16px',
            marginRight: '10px',
            backgroundColor: '#007bff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
          }}
        >
          Categories
        </Link>
        <Link
          to="/autocfilters"
          style={{
            padding: '8px 16px',
            backgroundColor: '#007bff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
          }}
        >
          Auto-Categorization
        </Link>
      </div>
      <button
        onClick={handleSignOut}
        style={{
          padding: '8px 16px',
          backgroundColor: '#dc3545',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
        }}
      >
        Sign Out
      </button>
    </div>
  );
};

export default NavBar;