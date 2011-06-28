# Probably ought to be using ant or maven or something
all:
	javac -g *.java
	jar cm JarManifest *.class > SettlementGameScoreBoard.jar

clean: 
	-rm *.class
	-rm *.jar
