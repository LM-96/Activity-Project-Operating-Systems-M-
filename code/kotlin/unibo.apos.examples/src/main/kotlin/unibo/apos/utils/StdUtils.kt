package unibo.apos.utils

fun readValidInt(question : String = "insert an integer") : Int {
    var isValid = false
    var num : Int = -1

    while(!isValid) {
        try {
            print("$question: ")
            num = readLine()!!.toInt()
            isValid = true
        } catch (nfe : NumberFormatException) {
            println("invalid input, please insert a number!")
        }
    }

    return num
}