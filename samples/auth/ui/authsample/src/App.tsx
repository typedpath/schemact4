import React from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css'; // Import default Amplify UI styles
import { fetchAuthSession } from '@aws-amplify/auth';
import './amplify-config';

const App: React.FC = () => {
  return (
    <Authenticator>
      {({ signOut, user }) => (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          {user ? (
            <>
              <button
                onClick={async () => {
                    console.log('hello')
                  const session = await fetchAuthSession();
                  console.log('JWT Token:', session.tokens?.idToken?.toString());
                  if (signOut) signOut();
                }}
                style={{ padding: '10px', marginTop: '10px' }}
              >
                Sign Out xx
              </button>
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

