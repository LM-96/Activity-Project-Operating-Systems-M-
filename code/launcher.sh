#!/bin/bash

# Matrix Multiplication Benchmark Script
# This script runs both Kotlin and Go implementations of matrix multiplication
# and compares their performance under different configurations

KOTLIN_PROJECT_BASE_DIR="$(pwd)/kotlin"
KOTLIN_PROJECT_DIR="${KOTLIN_PROJECT_BASE_DIR}/unibo.apos.examples"
KOTLIN_DIST_ZIP="${KOTLIN_PROJECT_DIR}/build/distributions/unibo.apos.examples-1.0-SNAPSHOT.zip"
KOTLIN_DIST_DIR="${KOTLIN_PROJECT_DIR}/build/distributions/unibo.apos.examples-1.0-SNAPSHOT"
KOTLIN_SCRIPT="${KOTLIN_DIST_DIR}/bin/unibo.apos.examples"
GO_PROJECT_DIR="$(pwd)/go"
GO_BINARY="${GO_PROJECT_DIR}/matrix-app"
OUTPUT_DIR="./benchmark_results"
DATE_SUFFIX=$(date +"%Y%m%d_%H%M%S")
OUTPUT_FILE_FIXED_WORKERS="${OUTPUT_DIR}/fixed_workers_${DATE_SUFFIX}.csv"
OUTPUT_FILE_FIXED_SIZE="${OUTPUT_DIR}/fixed_size_${DATE_SUFFIX}.csv"

FIXED_MATRIX_SIZE=250
WORKER_COUNTS=(1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 25 30 35 40 45 50)

FIXED_WORKERS=8
MATRIX_SIZES=(10 20 30 40 50 60 70 80 90 100 150 200 250 300 350 400 450 500 750 1000 1500 2000)

CONCURRENCY_MODES=("COORDINATOR" "FAN" "PURE")

REPETITIONS=10

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

mkdir -p "$OUTPUT_DIR"

echo -e "${GREEN}Matrix Multiplication Benchmark${NC}"

check_and_build_kotlin() {
    if [ ! -f "$KOTLIN_SCRIPT" ]; then
        echo -e "${YELLOW}Kotlin distribution not found at $KOTLIN_DIST_DIR${NC}"
        echo -e "${BLUE}Building Kotlin project...${NC}"

        cd "$KOTLIN_PROJECT_BASE_DIR" || {
            echo -e "${RED}Failed to navigate to Kotlin project directory${NC}"
            return 1
        }

        if ./gradlew distZip; then
            echo -e "${GREEN}Kotlin project built successfully${NC}"

            if [ ! -f "$KOTLIN_DIST_ZIP" ]; then
                echo -e "${RED}Distribution zip file not found at $KOTLIN_DIST_ZIP${NC}"
                cd - > /dev/null
                return 1
            fi

            echo -e "${BLUE}Unzipping Kotlin distribution...${NC}"
            unzip -o "$KOTLIN_DIST_ZIP" -d "$(dirname "$KOTLIN_DIST_DIR")" > /dev/null

            chmod +x "$KOTLIN_SCRIPT"

            echo -e "${GREEN}Kotlin distribution prepared successfully${NC}"
        else
            echo -e "${RED}Failed to build Kotlin project${NC}"
            cd - > /dev/null
            return 1
        fi

        cd - > /dev/null
    else
        echo -e "${GREEN}Kotlin distribution found at $KOTLIN_DIST_DIR${NC}"
    fi
    return 0
}

check_and_build_go() {
    if [ ! -f "$GO_BINARY" ]; then
        echo -e "${YELLOW}Go binary not found at $GO_BINARY${NC}"
        echo -e "${BLUE}Building Go project...${NC}"

        cd "$GO_PROJECT_DIR" || {
            echo -e "${RED}Failed to navigate to Go project directory${NC}"
            return 1
        }

        if go build -o matrix-app; then
            echo -e "${GREEN}Go project built successfully${NC}"
        else
            echo -e "${RED}Failed to build Go project${NC}"
            cd - > /dev/null
            return 1
        fi

        cd - > /dev/null
    else
        echo -e "${GREEN}Go binary found at $GO_BINARY${NC}"
    fi
    return 0
}

run_session_fixed_workers() {

    echo -e "${BLUE}Session 1: Analyzing execution time with varying number of workers (matrix size: $OUTPUT_FILE_FIXED_SIZE)${NC}"
    echo "Results will be saved to $OUTPUT_FILE_FIXED_SIZE"

    # Run with different worker counts
    for workers in "${WORKER_COUNTS[@]}"; do
        echo "Running with $workers workers..."

        for mode in "${CONCURRENCY_MODES[@]}"; do
            echo "  Kotlin - $mode mode"
            "$KOTLIN_SCRIPT" -s "$FIXED_MATRIX_SIZE" -c "$workers" -m "$mode" -r "$REPETITIONS" -o -f "$OUTPUT_FILE_FIXED_SIZE" -l
        done

        for mode in "${CONCURRENCY_MODES[@]}"; do
            echo "  Go - $mode mode"
            "$GO_BINARY" -s "$FIXED_MATRIX_SIZE" -c "$workers" -m "$mode" -r "$REPETITIONS" -o -f "$OUTPUT_FILE_FIXED_SIZE" -l
        done
    done

    echo "Session with fixed workers completed. Results saved to $OUTPUT_FILE_FIXED_FIXED_WORKERS"
}

run_session_fixed_size() {
    echo -e "${BLUE}Session 2: Analyzing execution time with varying matrix size (workers: $FIXED_WORKERS)${NC}"
    echo "Results will be saved to $OUTPUT_FILE_FIXED_WORKERS"

    for size in "${MATRIX_SIZES[@]}"; do
        echo "Running with matrix size $size..."

        for mode in "${CONCURRENCY_MODES[@]}"; do
            echo "  Kotlin - $mode mode"
            "$KOTLIN_SCRIPT" -s "$size" -c "$FIXED_WORKERS" -m "$mode" -r "$REPETITIONS" -o -f "$OUTPUT_FILE_FIXED_WORKERS" -l
        done

        for mode in "${CONCURRENCY_MODES[@]}"; do
            echo "  Go - $mode mode"
            "$GO_BINARY" -s "$size" -c "$FIXED_WORKERS" -m "$mode" -r "$REPETITIONS" -o -f "$OUTPUT_FILE_FIXED_WORKERS" -l
        done
    done

    echo "Session with fixed size completed. Results saved to $OUTPUT_FILE_FIXED_WORKERS"
}

echo "Checking and building required executables..."

check_and_build_kotlin || {
    echo -e "${RED}Failed to prepare Kotlin project. Exiting.${NC}"
    exit 1
}

check_and_build_go || {
    echo -e "${RED}Failed to prepare Go project. Exiting.${NC}"
    exit 1
}

echo -e "${GREEN}All executables are ready.${NC}"
echo "Starting benchmark sessions..."

run_session_fixed_workers

echo ""

run_session_fixed_size

echo -e "${GREEN}All benchmarks completed!${NC}"
echo "Results are saved in the $OUTPUT_DIR directory"
