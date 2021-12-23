export JAVA_HOME=/home/dominik/Tools/jdk-17.0.1/
export PATH=$PATH:$JAVA_HOME/bin 
export MVN=/home/dominik/Tools/apache-maven-3.8.3/bin/mvn

cd spring-crud-app
$MVN clean package
cd ../identity-provider
$MVN clean package
