SRC = $(shell find src -name "*.java")
OUT = out

all:
	mkdir -p $(OUT)
	javac -d $(OUT) $(SRC)

run: all
	java -cp $(OUT) madlang.Main $(FILE)

clean:
	rm -rf $(OUT)
