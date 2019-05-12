set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\concurrent"
"%javapath%\bin\javac.exe" -cp %root%/artifacts/info.kgeorgiy.java.advanced.concurrent.jar -d ./../../../../../../build *.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf IterativeParallelism.jar *

mkdir "%root%\artifacts\concurrent"
copy /Y "IterativeParallelism.jar" "%root%\artifacts\IterativeParallelism.jar"


cd "%root%\artifacts"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.concurrent list ru.ifmo.rain.demyanenko.concurrent.IterativeParallelism
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\concurrent"
rmdir /Q /S "%root%\java-labs\build"