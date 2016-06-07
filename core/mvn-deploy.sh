
cd target

svn co -N https://jtexy.googlecode.com/svn/maven/cz/dynawest/jtexy
cd jtexy
svn co -N https://jtexy.googlecode.com/svn/maven/cz/dynawest/jtexy/JTexy
cp -r -u ~/.m2/repository/cz/dynawest/jtexy/JTexy .
svn add JTexy/*
svn commit

cd ..