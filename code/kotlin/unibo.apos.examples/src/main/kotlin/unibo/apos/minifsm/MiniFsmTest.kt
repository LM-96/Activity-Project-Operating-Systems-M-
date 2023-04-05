package unibo.apos.minifsm

fun main() {

    val mainFsm = miniFsm {
        fsmName = "main"
        initialState = "s0"

        var counter = 0

        state("s0") {
            action = {
                println("$fsmName [state=$stateName]: counter = $counter")
                counter++
                Thread.sleep(1000L)
            }
            transition {
                destination = "s1"
                canTransit = { counter < 5 }
                elseDestination = "s2"
            }
        }

        state("s1") {
            action = {
                println("$fsmName [state=$stateName]: counter = $counter")
                counter++
                Thread.sleep(1000L)
            }
            transition {
                destination = "s0"
            }
        }
        
        state("s2") {
            action = {
                println("$fsmName [state=$stateName]: goodbye")
            }
        }
    }

    mainFsm.work()
}