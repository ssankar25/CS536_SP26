SRC = $(shell find src -name "*.java")
OUT = out
FILE ?=

.PHONY: all run clean

all:
	mkdir -p $(OUT)
	javac -d $(OUT) $(SRC)

run: all
ifndef FILE
	@echo "usage: make run FILE=filename.madl"
	@exit 1
else
	java -cp $(OUT) madlang.Main $(FILE)
endif

clean:
	rm -rf $(OUT)
