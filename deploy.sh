#cd ../erupt-web
#yarn run build
#cd ../erupt/erupt-web
#git add src/main/resources/public
#
#cd ../deploy/erupt-cloud-server-docker
#source deploy.sh

mvn clean deploy -P release