$classpath = "../../shared/modules/info.kgeorgiy.java.advanced.implementor"
$dir = "../java-solutions/info/kgeorgiy/ja/ponomarenko/implementor/*.java"

javac $dir -d out -cp $classpath
jar -cmf MANIFEST.MF impler.jar -C out .
rm out -r
