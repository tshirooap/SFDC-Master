buildしたwscでは、出力されるが、uber.jarを使用する
java -classpath /users/tshiroo/Documents/Works/github/SFDC/wsc/wsc/target/force-wsc-32.1.1.jar com.sforce.ws.tools.wsdlc mun1-partner.wsdl mun1-partner.jar
Exception in thread "main" java.lang.NoClassDefFoundError: org/stringtemplate/v4/STGroupDir
	at java.lang.Class.getDeclaredMethods0(Native Method)
	at java.lang.Class.privateGetDeclaredMethods(Class.java:2570)
	at java.lang.Class.getMethod0(Class.java:2813)
	at java.lang.Class.getMethod(Class.java:1663)
	at sun.launcher.LauncherHelper.getMainMethod(LauncherHelper.java:494)
	at sun.launcher.LauncherHelper.checkAndLoadMain(LauncherHelper.java:486)
Caused by: java.lang.ClassNotFoundException: org.stringtemplate.v4.STGroupDir
	at java.net.URLClassLoader$1.run(URLClassLoader.java:366)
	at java.net.URLClassLoader$1.run(URLClassLoader.java:355)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.net.URLClassLoader.findClass(URLClassLoader.java:354)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:425)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:308)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:358)
	... 6 more




 java -classpath /users/tshiroo/Documents/Works/github/SFDC/wsc/wsc/target/force-wsc-32.1.1-uber.jar com.sforce.ws.tools.wsdlc mun1-partner.wsdl mun1-partner.jar