cd ../erupt-web
pnpm run build
cd ../erupt/erupt-web
git add src/main/resources/public

cd ../erupt-ai-web
pnpm run build
cd ../erupt-ai
git add src/main/resources/static

cd ../deploy/erupt-cloud-server-docker
source deploy.sh