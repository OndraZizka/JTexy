mvn clean install -DskipTests

mvn dependency:copy-dependencies -DexcludeScope=test -DincludeScope=runtime

cd target
mv dependency lib
#zip JTexy.zip *.jar lib/*.jar
zip -r JTexy-1.0-SNAPSHOT.zip *.jar lib/*
cd ..
