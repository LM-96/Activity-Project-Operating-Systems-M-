#!/bin/bash

# Matrix Multiplication Benchmark Script
# This script runs both Kotlin and Go implementations of matrix multiplication
# and compares their performance under different configurations

KOTLIN_PROJECT_BASE_DIR="$(pwd)/kotlin"
KOTLIN_PROJECT_DIR="${KOTLIN_PROJECT_BASE_DIR}/unibo.apos.examples"
KOTLIN_NATIVE_DIR="${KOTLIN_PROJECT_DIR}/build/native"
KOTLIN_EXECUTABLE="${KOTLIN_NATIVE_DIR}/nativeCompile/kapos-matrix-app"
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

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

mkdir -p "$OUTPUT_DIR"

echo -e "${GREEN}Matrix Multiplication Benchmark${NC}"

check_and_build_kotlin() {
    if [ ! -f "$KOTLIN_EXECUTABLE" ]; then
        echo -e "${YELLOW}Kotlin native executable not found at $KOTLIN_NATIVE_DIR${NC}"
        echo -e "${BLUE}Building Kotlin project...${NC}"

        cd "$KOTLIN_PROJECT_BASE_DIR" || {
            echo -e "${RED}Failed to navigate to Kotlin project directory${NC}"
            return 1
        }

        if ./gradlew nativeCompile; then
            echo -e "${GREEN}Kotlin native built successfully${NC}"

            if [ ! -f "$KOTLIN_EXECUTABLE" ]; then
                echo -e "${RED}Kotlin native executable not found at $KOTLIN_EXECUTABLE${NC}"
                cd - > /dev/null
                return 1
            fi
        else
            echo -e "${RED}Failed to build Kotlin native${NC}"
            cd - > /dev/null
            return 1
        fi

        # Return to original directory
        cd - > /dev/null
    else
        echo -e "${GREEN}Kotlin native executable found at $KOTLIN_EXECUTABLE${NC}"
    fi
    return 0
}

run_session_fixed_workers() {

    echo -e "${BLUE}Session 1: Analyzing execution time with varying number of workers (matrix size: $OUTPUT_FILE_FIXED_SIZE)${NC}"
    echo "Results will be saved to $OUTPUT_FILE_FIXED_SIZE"

    # Run with different worker counts
    for workers in "${WORKER_COUNTS[@]}"; do
        echo "Running with $workers workers..."

        # Run Kotlin Native implementation with different concurrency types
        for mode in "${CONCURRENCY_MODES[@]}"; do
            echo "  Kotlin - $mode mode"
            "$KOTLIN_EXECUTABLE" -s "$FIXED_MATRIX_SIZE" -c "$workers" -m "$mode" -r "$REPETITIONS" -o -f "$OUTPUT_FILE_FIXED_SIZE" -l
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
            "$KOTLIN_EXECUTABLE" -s "$size" -c "$FIXED_WORKERS" -m "$mode" -r "$REPETITIONS" -o -f "$OUTPUT_FILE_FIXED_WORKERS" -l
        done
    done

    echo "Session with fixed size completed. Results saved to $OUTPUT_FILE_FIXED_WORKERS"
}

echo "Checking and building required executables..."

check_and_build_kotlin || {
    echo -e "${RED}Failed to prepare Kotlin project. Exiting.${NC}"
    exit 1
}

echo -e "${GREEN}All executables are ready.${NC}"
echo "Starting benchmark sessions..."

run_session_fixed_workers

echo ""

run_session_fixed_size

echo -e "${GREEN}All benchmarks completed!${NC}"
echo "Results are saved in the $OUTPUT_DIR directory"
