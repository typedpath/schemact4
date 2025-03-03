aws secretsmanager  create-secret --name github-token --description "Github access token" --secret-string $GITHUB_ACCESS_TOKEN --region $REGION
