sudo /etc/init.d/tomcat6 stop
sleep 15

cd /usr/share/tomcat6/webapps
sudo rm -Rf *

sudo wget http://maven.ala.org.au/repository/au/org/ala/biocache-service/1.0-SNAPSHOT/biocache-service-1.0-SNAPSHOT.war
sudo mv biocache-service-1.0-SNAPSHOT.war biocache-service.war
sudo unzip biocache-service.war -d biocache-service
sudo cp /data/biocache-conf/biocache-service/biocache.properties biocache-service/WEB-INF/classes/
sudo cp /data/biocache-conf/biocache-service/web.xml biocache-service/WEB-INF/web.xml 
sudo cp /data/biocache-conf/biocache-service/wms.properties biocache-service/WEB-INF/classes/
sudo chown -R tomcat biocache-service

sudo wget http://maven.ala.org.au/repository/au/org/ala/hubs-webapp/1.0-SNAPSHOT/hubs-webapp-1.0-SNAPSHOT.war
sudo mv hubs-webapp-1.0-SNAPSHOT.war hubs-webapp.war
sudo unzip hubs-webapp.war -d hubs-webapp
sudo cp /data/biocache-conf/hubs-webapp/hubs.properties hubs-webapp/WEB-INF/classes/
sudo cp /data/biocache-conf/hubs-webapp/web.xml hubs-webapp/WEB-INF/web.xml 
sudo chown -R tomcat hubs-webapp
# sudo wget http://maven.ala.org.au/repository/au/org/ala/ala-datacheck/1.0-SNAPSHOT/ala-datacheck-0.1.war

sudo cp /tmp/datacheck.war datacheck.war


sudo /etc/init.d/tomcat6 start