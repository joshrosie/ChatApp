JAVAC=/usr/bin/javac
.SUFFIXES: .java .class

SRCDIR=src
BINDIR=bin
CLASSPATH=".:lib/*:$(BINDIR)"

SRCDIRX=./src
DOCDIR=./java_doc
SOURCELIST=$(shell find $(SRCDIRX) -name '*.java' | sed "s,[.]/,,g")

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(CLASSPATH) $<

CLASSES = clientObj.class realiableThread.class senderThread.class receiverThread.class udpClient.class udpServer.class udpDriver.class
CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)

clean:
	rm $(BINDIR)/*.class
	@if [ -d $(DOCDIR) ]; then rm -r $(DOCDIR); fi;

runServerWAN_error:
	@java -cp $(CLASSPATH) udpDriver "sWan" "true"
	
runServerWAN_noerror:
	@java -cp $(CLASSPATH) udpDriver "sWan" "false"
	
runServerLOCAL_error:
	@java -cp $(CLASSPATH) udpDriver "sLocal" "true"
	
runServerLOCAL_noerror:
	@java -cp $(CLASSPATH) udpDriver "sLocal" "false"
	
runClientWAN:
	@java -cp $(CLASSPATH)udpDriver "cWan"

runClientLOCAL:
	@java -cp $(CLASSPATH) udpDriver "cLocal"
	
runJavaDoc:
	@javadoc -d $(DOCDIR) -linksource $(SOURCELIST)