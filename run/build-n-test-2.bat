set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\arrayset"
"%javapath%\bin\javac.exe" -d ./../../../../../../build *.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf ArraySet.jar *

mkdir "%root%\artifacts\arrayset"
copy /Y "ArraySet.jar" "%root%\artifacts\ArraySet.jar"


cd "%root%\artifacts\arrayset"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.arrayset NavigableSet ru.ifmo.rain.demyanenko.arrayset.ArraySet
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\arrayset"
rmdir /Q /S "%root%\java-labs\build"