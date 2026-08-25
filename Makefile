.PHONY: build test clean run-parse run-extract run-score docker samples

JAR := target/resume-cli.jar
SAMPLES := samples
RESUME := $(SAMPLES)/resume.pdf
JD := $(SAMPLES)/jd.txt

build:
	mvn -B -DskipTests package

test:
	mvn -B test

clean:
	mvn -B clean

# Regenerate the Chinese sample PDF and JD under samples/.
# Requires GNU make (or Windows-compatible make). On Windows, prefer
# running the command below directly from PowerShell.
samples:
	mvn -B -q test-compile
	@echo "Now run: java -cp \"target/test-classes;target/classes;<pdfbox+fontbox+commons-logging>\" com.aiparse.cli.tools.SampleResumeGen"

# Run samples end-to-end in mock mode
run-parse: build
	java -jar $(JAR) parse $(RESUME)

run-extract: build
	java -jar $(JAR) extract --mock $(RESUME)

run-score: build
	java -jar $(JAR) score --mock $(RESUME) --jd $(JD)

docker:
	docker build -t resume-cli:latest .
