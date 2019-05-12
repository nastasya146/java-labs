set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\student"
"%javapath%\bin\javac.exe" -cp %root%/artifacts/* -d ./../../../../../../build *.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf StudentDB.jar *

mkdir "%root%\artifacts\student"
copy /Y "StudentDB.jar" "%root%\artifacts\StudentDB.jar"


cd "%root%\artifacts"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.student StudentGroupQuery ru.ifmo.rain.demyanenko.student.StudentDB
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\student"
rmdir /Q /S "%root%\java-labs\build"