# devops-mentoring-lab

DevOps mentoring lab for Java based application with full CI/CD solution (Kubernetes, Terraform, Ansible)

## PreRequisites

Install `SDKMAN`:

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
```

Install `JDK`:

```bash
# listázd a JDK-kat és nézd meg a "tem" (Temurin) jelűt
sdk list java

# LTS (ajánlott)
sdk install java 21-tem
sdk default java 21-tem

# vagy a legújabb stabil (nem LTS)
sdk install java 24-tem
sdk default java 24-tem

sdk use java 24-tem
```

Install `maven`:

```bash
sdk install maven
sdk default maven
```

> Always check which Java is used by `maven`

Check the install versions:

```bash
java -version
mvn -v
```

## Application initialization

You can generate a SpringBoot base project skeleton on portal -> `https://start.spring.io/`

You can generate from CLI:

```bash
mkdir spring-boot-hello && cd spring-boot-hello

curl -fsSL "https://start.spring.io/starter.tgz?type=maven-project&language=java&groupId=com.example&artifactId=spring-boot-hello&name=spring-boot-hello&packageName=com.example.hello&javaVersion=24&dependencies=web" \
| tar -xz
```

After create the 1st controller in `spring-boot-hello/src/main/java/com/example/hello/HelloController.java`

Build the application:

```bash
cd spring-boot-hello
mvn clean package
```

Run the application with `java` or `mvn`

```bash
cd spring-boot-hello  # Navigate into the project root where the `pom.xml` is stored
java -jar target/*.jar
java -jar target/*.jar --logging.level.root=DEBUG
# OR
mvn spring-boot:run
```

Test the application:

```bash
curl -i http://localhost:8080/hello
curl -i "http://localhost:8080/hello?name=Tibor"
```

Install the built package:

```bash
mvn install # It will store the JAR in ~/.m2/repository

java -jar ~/.m2/repository/com/example/spring-boot-hello/0.0.1-SNAPSHOT/spring-boot-hello-0.0.1-SNAPSHOT.jar
```

Set new version of project and after get:

```bash
# Set new version
mvn versions:set -DnewVersion=1.0.0-SNAPSHOT -DgenerateBackupPoms=true  # it will create a backup file next to the pom.xml

# Check the 'set' version
mvn -q help:evaluate -Dexpression=project.version -DforceStdout

# Accept the new version and delete the backup file
mvn versions:commit
```

Test the version from actuator endpoint:

```bash
curl http://localhost:8080/actuator/info
```

Run different tests:

```bash
mvn test
mvn -Dtest=HelloApiWebMvcTest test
mvn -Dtest=HelloApiIT test
mvn -Dtest=HelloControllerUnitTest test
mvn -Dtest=SpringBootHelloApplicationTests test
```

## Artificat management (Nexus)

```bash
mkdir -p $HOME/nexus-data
sudo chown -R 200:200 $HOME/nexus-data  # Suggested permission (nexus user ID is 200 in container)

docker run -d --name nexus -p 8081:8081 -p 5000:5000 -v $HOME/nexus-data:/nexus-data sonatype/nexus3:latest
curl -sI http://localhost:8081 | head -n 1
```

Get default password for `admin`:

```bash
docker exec -it nexus cat /nexus-data/admin.password
```

> After getting password you have to change and need to create the `~/.m2/settings.xml` configuration file with `Nexus` repos, users and passwords.

```bash
mvn -B -DskipTests deploy

# Explicit configuration file
mvn -s ~/.m2/settings.xml -B -DskipTests deploy
```

Validate the artificate and download it from another folder:

```bash
mvn -U dependency:get -DrepoUrl=http://localhost:8081/repository/maven-snapshots/ -Dartifact=com.example:spring-boot-hello:1.0.0-SNAPSHOT

# OR

mvn -s ~/.m2/settings.xml -U dependency:get \
  -DremoteRepositories=nexus-snapshots::default::http://localhost:8081/repository/maven-snapshots/ \
  -Dartifact=com.example:spring-boot-hello:1.0.0-SNAPSHOT
```

## Docker image support

Nexus UI -> Settings -> Security -> Realms -> Add `Docker Bearar Token Realm`

Create new repo -> Settings -> Repositories -> Create -> docker (hosted) (Name: docker-hosted | HTTP port: 5000 | Deployment policy: Allow redeploy)

Create the `/etc/docker/daemon.json` with insecure registry.

Test call -> `curl -sI http://localhost:5000/v2/`

Login with `admin` user with `docker login localhost:5000`

### Build and push artifacts with Dockerfile

Build JAR and image (prerequisite to write `Dockerfile`) locally:

```bash
mvn -q clean package

VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)

docker build -t spring-boot-hello:${VERSION} .

docker run --rm -p 8080:8080 spring-boot-hello:${VERSION}

docker tag spring-boot-hello:${VERSION} localhost:5000/spring-boot-hello:${VERSION}
docker push localhost:5000/spring-boot-hello:${VERSION}
```

### Build and push artifacts with Maven

JAR build + OCI image make with `mvn`:

```bash
mvn -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=nexus.local:5000/spring-boot-hello:$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)
```

Push the image into Nexus:

```bash
docker login nexus.local:5000
docker push localhost:5000/spring-boot-hello:$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)
```

## Release new version of app

```bash
mvn versions:set -DnewVersion=2.0.0 -DgenerateBackupPoms=false
mvn versions:commit
```

Change the application code to v2!! -> `HelloController.java`
Update the proper test codes

Build the JAR package -> `mvn -B clean package`
Deploy the new JAR into Nexus -> `mvn -DskipTests deploy`
Build Docker image file and push -> `export VERSION=2.0.0 && mvn -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=nexus.local:5000/spring-boot-hello:${VERSION}`
