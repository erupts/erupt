cd ~/git/erupt/
mvn -DskipTests=true deploy -P release,disable-javadoc-doclint

cd ~/git/erupt-pro/
mvn -DskipTests=true deploy