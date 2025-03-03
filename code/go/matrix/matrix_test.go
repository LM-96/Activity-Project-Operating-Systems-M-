package matrix

import (
	"gonum.org/v1/gonum/mat"
	"reflect"
	"testing"
)

func TestMatrixMultiplication(t *testing.T) {
	implementations := []Product{
		&CoordinatorChanneledMatrixProductImpl{},
		&FanChanneledMatrixProductImpl{},
	}

	t.Run("Should return the right result when multiplying two 3x3 matrices", func(t *testing.T) {
		matA := [][]int{
			{1, 2, 3},
			{4, 5, 6},
			{7, 8, 9},
		}
		matB := [][]int{
			{9, 8, 7},
			{6, 5, 4},
			{3, 2, 1},
		}
		expected := computeExpectedResult(matA, matB)

		for _, impl := range implementations {
			result := impl.Multiply(matA, matB, 4)
			if !reflect.DeepEqual(result, expected) {
				t.Errorf("Expected %v, got %v", expected, result)
			}
		}
	})

	t.Run("Should return the same matrix when multiplying by identity matrix", func(t *testing.T) {
		size := 5
		identity := createIdentityMatrix(size)
		randomMatrix := CreateRandomMatrix(size)

		for _, impl := range implementations {
			result := impl.Multiply(randomMatrix, identity, 4)
			if !reflect.DeepEqual(result, randomMatrix) {
				t.Errorf("Expected %v, got %v", randomMatrix, result)
			}
		}
	})

	t.Run("Should return zero matrix when multiplying by zero matrix", func(t *testing.T) {
		size := 4
		zeroMatrix := createZeroMatrix(size)
		randomMatrix := CreateRandomMatrix(size)

		for _, impl := range implementations {
			result := impl.Multiply(randomMatrix, zeroMatrix, 3)
			if !reflect.DeepEqual(result, zeroMatrix) {
				t.Errorf("Expected %v, got %v", zeroMatrix, result)
			}
		}
	})

	t.Run("Should return consistent results when using different worker counts", func(t *testing.T) {
		matA := [][]int{
			{1, 2},
			{3, 4},
		}
		matB := [][]int{
			{5, 6},
			{7, 8},
		}
		expected := computeExpectedResult(matA, matB)

		for _, impl := range implementations {
			for workers := 1; workers <= 8; workers++ {
				result := impl.Multiply(matA, matB, workers)
				if !reflect.DeepEqual(result, expected) {
					t.Errorf("With %d workers: Expected %v, got %v", workers, expected, result)
				}
			}
		}
	})

	t.Run("Should handle non-square matrices correctly", func(t *testing.T) {
		matA := [][]int{
			{1, 2, 3},
			{4, 5, 6},
		}
		matB := [][]int{
			{7, 8},
			{9, 10},
			{11, 12},
		}
		expected := computeExpectedResult(matA, matB)

		for _, impl := range implementations {
			result := impl.Multiply(matA, matB, 2)
			if !reflect.DeepEqual(result, expected) {
				t.Errorf("Expected %v, got %v", expected, result)
			}
		}
	})
}

func createIdentityMatrix(size int) [][]int {
	matrix := make([][]int, size)
	for i := range matrix {
		matrix[i] = make([]int, size)
		for j := range matrix[i] {
			if i == j {
				matrix[i][j] = 1
			} else {
				matrix[i][j] = 0
			}
		}
	}
	return matrix
}

func createZeroMatrix(size int) [][]int {
	matrix := make([][]int, size)
	for i := range matrix {
		matrix[i] = make([]int, size)
	}
	return matrix
}

func computeExpectedResult(matA [][]int, matB [][]int) [][]int {
	rowsA := len(matA)
	colsB := len(matB[0])
	denseA := intMatrixToDense(matA)
	denseB := intMatrixToDense(matB)
	denseC := mat.NewDense(rowsA, colsB, nil)
	denseC.Mul(denseA, denseB)
	return denseToIntMatrix(denseC, rowsA, colsB)
}

func intMatrixToDense(matrix [][]int) *mat.Dense {
	rows := len(matrix)
	cols := len(matrix[0])

	data := make([]float64, rows*cols)
	for i := 0; i < rows; i++ {
		for j := 0; j < cols; j++ {
			data[i*cols+j] = float64(matrix[i][j])
		}
	}

	return mat.NewDense(rows, cols, data)
}

func denseToIntMatrix(dense *mat.Dense, rows, cols int) [][]int {
	result := make([][]int, rows)
	for i := range result {
		result[i] = make([]int, cols)
		for j := 0; j < cols; j++ {
			result[i][j] = int(dense.At(i, j))
		}
	}
	return result
}
