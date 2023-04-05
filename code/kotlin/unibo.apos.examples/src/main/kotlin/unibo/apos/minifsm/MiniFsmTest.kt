package unibo.apos.minifsm

import unibo.apos.minifsm.builders.miniWork

fun main() {

    miniWork("main") {
        initialState = "s0"

        var counter = 0

        state("s0") {
            action = {
                miniPrintln("counter = $counter")
                counter++
                Thread.sleep(500L)
            }
            transition {
                destination = "s1"
                canTransit = { counter < 5 }
                elseDestination = "s2"
            }
        }

        state("s1") {
            action = {
                miniPrintln("counter = $counter")
                counter++
                Thread.sleep(500L)
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
}