package matrix

import (
	"math/rand"
)

type Pointer struct {
	Row int
	Col int
}

type Cell struct {
	Cell  Pointer
	Value int
}

type Product interface {
	Multiply(matA [][]int, matB [][]int, workers int) [][]int
}

type CoordinatorChanneledMatrixProductImpl struct{}
type FanChanneledMatrixProductImpl struct{}

func CreateRandomMatrix(size int) [][]int {
	matrix := make([][]int, size)
	for i := range matrix {
		matrix[i] = make([]int, size)
		for j := range matrix[i] {
			matrix[i][j] = rand.Intn(9) + 1 // 1-10 range
		}
	}
	return matrix
}

func EmptyPointer() Pointer {
	return Pointer{Row: -1, Col: -1}
}

// COORDINATOR ********************************************************************************************************

func (c *CoordinatorChanneledMatrixProductImpl) Multiply(matA [][]int, matB [][]int, workers int) [][]int {
	emptyPointer := EmptyPointer()
	rows := len(matA)
	cols := len(matB[0])
	cells := rows * cols
	result := createEmptyMatrix(rows, cols)

	requestWorkChannel := make(chan int, workers)
	ackChannel := make(chan int, workers)
	resultChannel := make(chan Cell, cells)
	workerChannels := make([]chan Pointer, workers)
	for i := range workerChannels {
		workerChannels[i] = make(chan Pointer, cells)
	}

	for i := 0; i < workers; i++ {
		go coordinatorWorker(i, requestWorkChannel, ackChannel, workerChannels[i], resultChannel, matA, matB)
	}

	currentRow := 0
	currentCol := 0
	completedCells := 0

	for completedCells < cells {
		select {
		case id := <-when(currentRow < rows && currentCol < cols, requestWorkChannel):
			workerChannels[id] <- Pointer{currentRow, currentCol}
			currentCol++
			if currentCol == cols {
				currentCol = 0
				currentRow++
			}

		case partial := <-resultChannel:
			cell := partial.Cell
			result[cell.Row][cell.Col] = partial.Value
			completedCells++
		}
	}

	for _, workerChannel := range workerChannels {
		workerChannel <- emptyPointer
	}
	for i := 0; i < workers; i++ {
		<-ackChannel
	}
	close(ackChannel)
	close(resultChannel)

	return result
}

func computeProductCell(a [][]int, b [][]int, pointer Pointer) Cell {
	sum := 0
	for k := 0; k < len(a[0]); k++ {
		sum += a[pointer.Row][k] * b[k][pointer.Col]
	}
	return Cell{Cell: pointer, Value: sum}
}

func coordinatorWorker(
	id int,
	requestWorkChannel chan<- int,
	ackChannel chan<- int,
	workerChannel <-chan Pointer,
	resultChannel chan<- Cell,
	a [][]int,
	b [][]int,
) {
	emptyPointer := EmptyPointer()
	requestWorkChannel <- id
	pointer := <-workerChannel
	for pointer != emptyPointer {
		result := computeProductCell(a, b, pointer)
		resultChannel <- result
		requestWorkChannel <- id
		pointer = <-workerChannel
	}

	ackChannel <- id
}

// FAN ****************************************************************************************************************

func (c *FanChanneledMatrixProductImpl) Multiply(matA [][]int, matB [][]int, workers int) [][]int {
	rows := len(matA)
	cols := len(matB[0])
	cells := rows * cols
	result := createEmptyMatrix(rows, cols)

	taskChannel := make(chan Pointer, cells)
	resultChannel := make(chan Cell, cells)

	for i := 0; i < workers; i++ {
		go fanWorker(taskChannel, resultChannel, matA, matB)
	}

	fanDistributeWork(taskChannel, rows, cols)
	fanCollectResult(resultChannel, result, cells)
	return result
}

func fanCollectResult(
	resultChannel chan Cell,
	result [][]int,
	totalCells int,
) {
	for i := 0; i < totalCells; i++ {
		partial := <-resultChannel
		cell := partial.Cell
		result[cell.Row][cell.Col] = partial.Value
	}

	close(resultChannel)
}

func fanDistributeWork(
	taskChannel chan Pointer,
	rows int,
	cols int,
) {
	for row := 0; row < rows; row++ {
		for col := 0; col < cols; col++ {
			taskChannel <- Pointer{Row: row, Col: col}
		}
	}

	close(taskChannel)
}

func fanWorker(
	taskChannel <-chan Pointer,
	resultChannel chan<- Cell,
	matA [][]int,
	matB [][]int,
) {
	for task := range taskChannel {
		resultChannel <- computeProductCell(matA, matB, task)
	}
}

// UTILITY ************************************************************************************************************

func createEmptyMatrix(rows int, cols int) [][]int {
	matrix := make([][]int, rows)
	for i := range matrix {
		matrix[i] = make([]int, cols)
	}

	return matrix
}

func when[T any](guard bool, channel chan T) chan T {
	if guard {
		return channel
	}

	return nil
}
