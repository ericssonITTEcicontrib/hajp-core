Follow below steps by step:
1. mvn clean install -DskipTests
2. reinitJenkinses.sh
3. cluster.sh start
4. cluster.sh stop
5. copyJars.sh
6. cluster.sh start
7. code change => back to 1 skip 2 to 4
