package main

import (
	"flag"
	"fmt"
	"github.com/google/uuid"
	"os"
	"strings"
	"time"
	"unibo/apos/matrix"
)

type Mode string

type Options struct {
	size          int
	workers       int
	outputToCsv   bool
	fileName      string
	repetitions   int
	enableLogging bool
	mode          Mode
}

type RunResult struct {
	Iteration         int
	MatA              [][]int
	MatB              [][]int
	Result            [][]int
	ElapsedTimeMillis int64
}

const (
	COORDINATOR Mode = "COORDINATOR"
	FAN         Mode = "FAN"
	PURE        Mode = "PURE"
)

func main() {
	options := getOptions()
	if options.enableLogging {
		fmt.Printf("[size: %d, goroutines: %d, output: %t, file: %s, repetitions: %d, mode: %s]\n",
			options.size, options.workers, options.outputToCsv, options.fileName, options.repetitions, options.mode)
	}

	matrixProduct := getMatrixProduct(options)
	results := run(matrixProduct, options.size, options.workers, options.repetitions, options.enableLogging)
	logStatistics(results, options.enableLogging)
	exportResults(results, options)
}

func exportResults(results []RunResult, options Options) {
	workspaceID := uuid.New().String()
	filename := options.fileName
	if filename == "" {
		filename = fmt.Sprintf("matrix_results_%d.csv", time.Now().Unix())
	}

	needsHeader := false
	if _, err := os.Stat(filename); os.IsNotExist(err) {
		needsHeader = true
	}

	file, err := os.OpenFile(filename, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		fmt.Printf("Error opening file: %v\n", err)
		return
	}
	defer file.Close()

	if needsHeader {
		header := "workspace_id,size,goroutines,mode,time_millis\n"
		if _, err := file.WriteString(header); err != nil {
			fmt.Printf("Error writing header: %v\n", err)
			return
		}
	}

	mode := "go_" + strings.ToLower(string(options.mode))
	for _, result := range results {
		line := fmt.Sprintf("%s,%d,%d,%s,%d\n",
			workspaceID, options.size, options.workers, mode, result.ElapsedTimeMillis)
		if _, err := file.WriteString(line); err != nil {
			fmt.Printf("Error writing result: %v\n", err)
			return
		}
	}

	if options.enableLogging {
		fmt.Printf("Results exported to %s\n", filename)
	}
}

func getMatrixProduct(options Options) matrix.Product {
	switch options.mode {
	case FAN:
		return &matrix.FanChanneledMatrixProductImpl{}
	case COORDINATOR:
		return &matrix.CoordinatorChanneledMatrixProductImpl{}
	case PURE:
		return &matrix.PureChanneledMatrixProductImpl{}
	default:
		throwError("invalid mode: %s", string(options.mode))
		return nil
	}
}

func getOptions() Options {
	size := flag.Int("s", 3, "Size of the matrices (NxN)")
	workers := flag.Int("c", 4, "Number of goroutines to use")
	outputToCsv := flag.Bool("o", false, "Store results in CSV file")
	fileName := flag.String("f", "", "CSV filename for storing results")
	repetitions := flag.Int("r", 1, "Number of times to repeat the calculation")
	enableLogging := flag.Bool("l", false, "Enable detailed logging")
	mode := flag.String("m", "COORDINATOR", "Multiplication mode: COORDINATOR, FAN or PURE")
	flag.Parse()

	options := Options{
		size:          *size,
		workers:       *workers,
		outputToCsv:   *outputToCsv,
		fileName:      *fileName,
		repetitions:   *repetitions,
		enableLogging: *enableLogging,
		mode:          parseMode(*mode),
	}

	return options
}

func logStatistics(results []RunResult, enableLogging bool) {
	if !enableLogging {
		return
	}

	var totalTime int64 = 0
	var minTime int64 = results[0].ElapsedTimeMillis
	var maxTime int64 = results[0].ElapsedTimeMillis

	for _, result := range results {
		timeMs := result.ElapsedTimeMillis / 1_000_000
		totalTime += timeMs
		if timeMs < minTime {
			minTime = timeMs
		}
		if timeMs > maxTime {
			maxTime = timeMs
		}
	}

	avgTime := float64(totalTime) / float64(len(results))
	fmt.Printf("execution statistics: [average: %.2f ms, min: %d ms, max: %d ms]\n",
		avgTime, minTime/1_000_000, maxTime/1_000_000)
}

func parseMode(s string) Mode {
	switch Mode(s) {
	case COORDINATOR, FAN, PURE:
		return Mode(s)
	default:
		throwError("invalid mode: %s", s)
		return COORDINATOR
	}
}

func run(matrixProduct matrix.Product, size, workers, repetitions int, enableLogging bool) []RunResult {
	matA := matrix.CreateRandomMatrix(size)
	matB := matrix.CreateRandomMatrix(size)

	results := make([]RunResult, 0, repetitions)
	for i := 0; i < repetitions; i++ {
		if enableLogging {
			fmt.Printf("running execution %d/%d...\n", i+1, repetitions)
		}

		start := time.Now()
		matC := matrixProduct.Multiply(matA, matB, workers)
		executionTime := time.Since(start)

		results = append(results, RunResult{
			Iteration:         i,
			MatA:              matA,
			MatB:              matB,
			Result:            matC,
			ElapsedTimeMillis: executionTime.Milliseconds(),
		})

		if enableLogging {
			fmt.Printf("execution %d: elapsed time: %d ms\n", i+1, executionTime.Milliseconds())
		}
	}

	if enableLogging {
		fmt.Println("executions completed")
	}

	return results
}

func throwError(msg string, a ...any) {
	err := fmt.Errorf(msg, a...)
	fmt.Println(err)
	os.Exit(1)
}
