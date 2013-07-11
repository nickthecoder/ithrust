SET CLASSPATH=lib\itchy.jar;lib\jame.jar;lib\bsh.jar;lib\jdom.jar
SET MAIN=uk.co.nickthecoder.thrust.Thrust

move lib\native\win32\*.dll .

java -classpath "%CLASSPATH%" "%MAIN%"
