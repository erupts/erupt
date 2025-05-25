cd ../erupt-web
npm run build
cd ../erupt/erupt-web
git add src/main/resources/public

cd ../erupt-ai-web
pnpm run build
cd ../erupt-ai
git add ../erupt-ai/src/main/resources/static