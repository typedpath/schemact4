npm run build
aws s3 cp build  s3://accountview.testedsoftware.org --recursive
