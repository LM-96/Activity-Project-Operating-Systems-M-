#!/bin/bash

BENCHMARK_DIR="$(pwd)/benchmark_results"
FIXED_SIZE_CSV="${BENCHMARK_DIR}/fixed_size_sel.csv"
FIXED_WORKERS_CSV="${BENCHMARK_DIR}/fixed_workers_sel.csv"
SERVER_PROJECT_BASE_DIR="$(pwd)/server"
SERVER_PROJECT_DIR="${SERVER_PROJECT_BASE_DIR}/unibo.apos.server"
SERVER_DIST_ZIP="${SERVER_PROJECT_DIR}/build/distributions/unibo.apos.server-1.0-SNAPSHOT.zip"
SERVER_DIST_DIR="${SERVER_PROJECT_DIR}/build/distributions/unibo.apos.server-1.0-SNAPSHOT"
SERVER_SCRIPT="${SERVER_DIST_DIR}/bin/unibo.apos.server"
OUTPUT_DIR="$(pwd)/graphs"

mkdir -p "$OUTPUT_DIR"

echo -e "${GREEN}Benchmark Graphs${NC}"

check_and_build_server() {
    if [ ! -f "$KOTLIN_SCRIPT" ]; then
        echo -e "${YELLOW}Server distribution not found at $SERVER_DIST_DIR${NC}"
        echo -e "${BLUE}Building Server project...${NC}"

        cd "$SERVER_PROJECT_BASE_DIR" || {
            echo -e "${RED}Failed to navigate to Server project directory${NC}"
            return 1
        }

        if ./gradlew distZip; then
            echo -e "${GREEN}Server project built successfully${NC}"

            if [ ! -f "$SERVER_DIST_ZIP" ]; then
                echo -e "${RED}Distribution zip file not found at $SERVER_DIST_ZIP${NC}"
                cd - > /dev/null
                return 1
            fi

            echo -e "${BLUE}Unzipping Server distribution...${NC}"
            unzip -o "$SERVER_DIST_ZIP" -d "$(dirname "$SERVER_DIST_DIR")" > /dev/null

            chmod +x "$SERVER_SCRIPT"

            echo -e "${GREEN}Server distribution prepared successfully${NC}"
        else
            echo -e "${RED}Failed to build Server project${NC}"
            cd - > /dev/null
            return 1
        fi

        cd - > /dev/null
    else
        echo -e "${GREEN}Server distribution found at $SERVER_DIST_DIR${NC}"
    fi
    return 0
}

create_graphs() {
  echo "Generating graphs..."

  echo "   Fixed size, ALL"
    "$SERVER_SCRIPT" "${FIXED_SIZE_CSV}" -o "${OUTPUT_DIR}/fixed_size_all.svg" --x WORKERS --y AVG_TIME

  echo "   Fixed size, PURE"
  "$SERVER_SCRIPT" "${FIXED_SIZE_CSV}" -o "${OUTPUT_DIR}/fixed_size_pure.svg" --x WORKERS --y AVG_TIME -m KT_PURE,GO_PURE,KT_NTV_PURE,KT_GRAAL_PURE

  echo "   Fixed size, COORDINATOR"
  "$SERVER_SCRIPT" "${FIXED_SIZE_CSV}" -o "${OUTPUT_DIR}/fixed_size_coordinator.svg" --x WORKERS --y AVG_TIME -m KT_COORDINATOR,GO_COORDINATOR,KT_NTV_COORDINATOR,KT_GRAAL_COORDINATOR

  echo "   Fixed size, FAN"
    "$SERVER_SCRIPT" "${FIXED_SIZE_CSV}" -o "${OUTPUT_DIR}/fixed_size_fan.svg" --x WORKERS --y AVG_TIME -m KT_FAN,GO_FAN,KT_NTV_FAN,KT_GRAAL_FAN

  echo "   Fixed workers, ALL"
      "$SERVER_SCRIPT" "${FIXED_WORKERS_CSV}" -o "${OUTPUT_DIR}/fixed_workers_ll.svg" --x SIZE --y AVG_TIME

  echo "   Fixed workers, PURE"
    "$SERVER_SCRIPT" "${FIXED_WORKERS_CSV}" -o "${OUTPUT_DIR}/fixed_workers_pure.svg" --x SIZE --y AVG_TIME -m KT_PURE,GO_PURE,KT_NTV_PURE,KT_GRAAL_PURE

  echo "   Fixed workers, COORDINATOR"
  "$SERVER_SCRIPT" "${FIXED_WORKERS_CSV}" -o "${OUTPUT_DIR}/fixed_workers_coordinator.svg" --x SIZE --y AVG_TIME -m KT_COORDINATOR,GO_COORDINATOR,KT_NTV_COORDINATOR,KT_GRAAL_COORDINATOR

  echo "   Fixed workers, FAN"
    "$SERVER_SCRIPT" "${FIXED_WORKERS_CSV}" -o "${OUTPUT_DIR}/fixed_workers_fan.svg" --x SIZE --y AVG_TIME -m KT_FAN,GO_FAN,KT_NTV_FAN,KT_GRAAL_FAN
}

echo "Checking and building required executables..."

check_and_build_server || {
    echo -e "${RED}Failed to prepare Server project. Exiting.${NC}"
    exit 1
}

echo -e "${GREEN}All executables are ready.${NC}"
echo "Starting graphs..."

create_graphs

echo -e "${GREEN}All graphs completed!${NC}"
echo "Results are saved in the $OUTPUT_DIR directory"

