package unibo.apos.matrix.validation.exceptions

/**
 * An exception that could be thrown in the cases in which a row has an invalid size
 *
 * @property expectedSize the expected size of the row
 *
 * @param row the invalid row
 */
class InvalidRowsSizeException(row: Array<*>, val expectedSize: Int) :
    InvalidRowException(row, "the row has an invalid size of ${row.size} instead of $expectedSize")