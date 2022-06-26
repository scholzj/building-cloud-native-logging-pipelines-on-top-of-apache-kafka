# Makefile.java contains the shared tasks for building Java applications. This file is
# included into the Makefile files which contain some Java sources which should be build.
#

java_build:
	echo "Building JAR file ..."
	mvn -Pnative -Dquarkus.native.container-build=true package

java_clean:
	echo "Cleaning Maven build ..."
	mvn clean
