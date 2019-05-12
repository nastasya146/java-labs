set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\mapper"
"%javapath%\bin\javac.exe" -cp %root%/artifacts/* -d ./../../../../../../build *.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf ParallelMapperImpl.jar *

mkdir "%root%\artifacts\mapper"
copy /Y "ParallelMapperImpl.jar" "%root%\artifacts\ParallelMapperImpl.jar"


cd "%root%\artifacts"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.mapper list ru.ifmo.rain.demyanenko.mapper.ParallelMapperImpl,ru.ifmo.rain.demyanenko.mapper.IterativeParallelism
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\mapper"
rmdir /Q /S "%root%\java-labs\build"