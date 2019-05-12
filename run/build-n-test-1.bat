set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\walk"
"%javapath%\bin\javac.exe" -d ./../../../../../../build *.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf RecursiveWalk.jar *

mkdir "%root%\artifacts\walk"
copy /Y "RecursiveWalk.jar" "%root%\artifacts\RecursiveWalk.jar"


cd "%root%\artifacts\walk"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.demyanenko.walk.RecursiveWalk
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\walk"
rmdir /Q /S "%root%\java-labs\build"
