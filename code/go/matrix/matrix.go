package matrix

import (
	"math/rand"
)

type CellPointer struct {
	Row int
	Col int
}

type Cell struct {
	Cell  CellPointer
	Value int
}

type ProductUnit struct {
	Row     []int
	Col     []int
	Pointer CellPointer
}

type Product interface {
	Multiply(matA [][]int, matB [][]int, workers int) [][]int
}

func (cellPointer CellPointer) IsEmpty() bool {
	return cellPointer.Row == -1 && cellPointer.Col == -1
}

func (productUnit ProductUnit) IsEmpty() bool {
	return productUnit.Row == nil && productUnit.Col == nil && productUnit.Pointer.IsEmpty()
}

type CoordinatorChanneledMatrixProductImpl struct{}
type FanChanneledMatrixProductImpl struct{}

type PureChanneledMatrixProductImpl struct{}

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

func EmptyPointer() CellPointer {
	return CellPointer{Row: -1, Col: -1}
}

func EmptyProductUnit() ProductUnit {
	return ProductUnit{Row: nil, Col: nil, Pointer: EmptyPointer()}
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
	workerChannels := make([]chan CellPointer, workers)
	for i := range workerChannels {
		workerChannels[i] = make(chan CellPointer, cells)
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
			workerChannels[id] <- CellPointer{currentRow, currentCol}
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

func computeProductCellOfPointer(a [][]int, b [][]int, pointer CellPointer) Cell {
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
	workerChannel <-chan CellPointer,
	resultChannel chan<- Cell,
	a [][]int,
	b [][]int,
) {
	requestWorkChannel <- id
	pointer := <-workerChannel
	for !pointer.IsEmpty() {
		result := computeProductCellOfPointer(a, b, pointer)
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

	taskChannel := make(chan CellPointer, cells)
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
	taskChannel chan CellPointer,
	rows int,
	cols int,
) {
	for row := 0; row < rows; row++ {
		for col := 0; col < cols; col++ {
			taskChannel <- CellPointer{Row: row, Col: col}
		}
	}

	close(taskChannel)
}

func fanWorker(
	taskChannel <-chan CellPointer,
	resultChannel chan<- Cell,
	matA [][]int,
	matB [][]int,
) {
	for task := range taskChannel {
		resultChannel <- computeProductCellOfPointer(matA, matB, task)
	}
}

// PURE ***************************************************************************************************************

func (c *PureChanneledMatrixProductImpl) Multiply(matA [][]int, matB [][]int, workers int) [][]int {
	emptyProductUnit := EmptyProductUnit()
	rows := len(matA)
	cols := len(matB[0])
	cells := rows * cols
	result := createEmptyMatrix(rows, cols)

	requestWorkChannel := make(chan int, workers)
	ackChannel := make(chan int, workers)
	resultChannel := make(chan Cell, cells)
	workerChannels := make([]chan ProductUnit, workers)
	for i := range workerChannels {
		workerChannels[i] = make(chan ProductUnit, cells)
	}

	for i := 0; i < workers; i++ {
		go pureWorker(i, requestWorkChannel, ackChannel, workerChannels[i], resultChannel)
	}

	currentRow := 0
	currentCol := 0
	completedCells := 0

	for completedCells < cells {
		select {
		case id := <-when(currentRow < rows && currentCol < cols, requestWorkChannel):
			pointer := CellPointer{Row: currentRow, Col: currentCol}
			workerChannels[id] <- getProductUnit(matA, matB, pointer)
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
		workerChannel <- emptyProductUnit
	}
	for i := 0; i < workers; i++ {
		<-ackChannel
	}
	close(ackChannel)
	close(resultChannel)

	return result
}

func computeProductCellOfUnit(productUnit ProductUnit) Cell {
	sum := 0
	row := productUnit.Row
	col := productUnit.Col
	for k := 0; k < len(row); k++ {
		sum += row[k] * col[k]
	}
	return Cell{Cell: productUnit.Pointer, Value: sum}
}

func getProductUnit(matA [][]int, matB [][]int, pointer CellPointer) ProductUnit {
	rowA := matA[pointer.Row]
	colIndex := pointer.Col
	colB := make([]int, len(matB))
	for i, row := range matB {
		colB[i] = row[colIndex]
	}

	return ProductUnit{Row: rowA, Col: colB, Pointer: pointer}
}

func pureWorker(
	id int,
	requestWorkChannel chan<- int,
	ackChannel chan<- int,
	workerChannel <-chan ProductUnit,
	resultChannel chan<- Cell,
) {
	requestWorkChannel <- id
	productUnit := <-workerChannel
	for !productUnit.IsEmpty() {
		result := computeProductCellOfUnit(productUnit)
		resultChannel <- result
		requestWorkChannel <- id
		productUnit = <-workerChannel
	}

	ackChannel <- id
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
