import { Amplify } from 'aws-amplify';
//import { cognitoUserPoolsTokenProvider } from '@aws-amplify/auth/cognito';
//import { CookieStorage } from '@aws-amplify/utils';

const amplifyConfig = {
    Auth: {
        Cognito: {
            //region: 'us-east-1', // Replace with your AWS region from CDK
            userPoolId: 'us-east-1_cfouJh9iZ', // Replace with your UserPoolId from CDK
            userPoolClientId: '4ib6jfkike897o98stetmde6qu', // Replace with your UserPoolClientId from CDK
            loginWith: {
                email: true, // Matches your CDK signInAliases.email(true)
            },
        }
        },
};

// Configure Amplify
Amplify.configure(amplifyConfig);


// Use default storage (localStorage) for tokens
// No need for CookieStorage; cognitoUserPoolsTokenProvider defaults to localStorage
// Set up token storage (optional, for browser cookies)
//cognitoUserPoolsTokenProvider.setKeyValueStorage(new CookieStorage());