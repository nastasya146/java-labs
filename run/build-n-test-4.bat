set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\implementor"
"%javapath%\bin\javac.exe" -cp %root%/artifacts/info.kgeorgiy.java.advanced.implementor.jar -d ./../../../../../../build *.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf Implementator.jar *

mkdir "%root%\artifacts\implementor"
copy /Y "Implementator.jar" "%root%\artifacts\Implementator.jar"


cd "%root%\artifacts"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.demyanenko.implementor.Implementator
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\implementor"
rmdir /Q /S "%root%\java-labs\build"