set root=C:\Users\mi\Documents\java
set javapath=C:\Program Files\Java\jdk-11.0.2

cd "%root%\java-labs\src\ru\ifmo\rain\demyanenko\crawler"
"%javapath%\bin\javac.exe" -cp %root%/artifacts/* -d ./../../../../../../build *.java taskCreator/*.java taskManager/*.java task/*.java

cd "%root%\java-labs\build" 
"%javapath%\bin\jar.exe" cvf WebCrawler.jar *

mkdir "%root%\artifacts\crawler"
copy /Y "WebCrawler.jar" "%root%\artifacts\WebCrawler.jar"


cd "%root%\artifacts"
"%javapath%\bin\java.exe" -cp . -p %root%\artifacts -m info.kgeorgiy.java.advanced.crawler hard ru.ifmo.rain.demyanenko.crawler.WebCrawler
cd "%root%\java-labs\run\"
rmdir /Q /S "%root%\artifacts\crawler"
rmdir /Q /S "%root%\java-labs\build"