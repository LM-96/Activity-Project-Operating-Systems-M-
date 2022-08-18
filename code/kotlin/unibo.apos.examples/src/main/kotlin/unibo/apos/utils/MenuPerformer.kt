package unibo.apos.utils

import java.io.BufferedReader
import java.io.BufferedWriter

class IOMenuPerformer(
    val menu : Menu,
    val writer : BufferedWriter,
    val reader : BufferedReader,
) {

    fun askForChoice(question : String = "Type your choice") : MenuEntry? {
        writer.newLine()
        writer.write("*** ${menu.title} ***************")
        for(e in menu.getEntries().entries) {
            if(e.value.enabled) {
                writer.write("\n\t[${e.key}] -> ${e.value.description}")
            }
        }
        writer.newLine()
        writer.write("$question : ")
        writer.flush()
        val res = reader.readLine().trim()
        writer.newLine()
        writer.flush()

        val entry = menu[res]
        if(entry != null)
            menu.select(entry.id)

        return entry
    }

    fun askUntilValidChoice(question : String = "Type your choice") : MenuEntry {
        var choice = askForChoice(question)
        while(choice == null) {
            writer.write("\t\t !! INVALID CHOICE")
            writer.newLine()
            writer.flush()
            choice = askForChoice(question)
        }

        return choice
    }

}

fun stdMenuPerformer(menu : Menu) : IOMenuPerformer =
    IOMenuPerformer(menu, System.out.bufferedWriter(), System.`in`.bufferedReader())

fun stdMenuUntilExit(title : String,
                     exitEntryId : String = "exit",
                     exitEntryDescription : String = "exit",
                     question : String = "Type your choice",
                     builder : MenuBuilder.() -> Unit,
) {
    val menu = menu(title, builder)
    menu.addEntry(exitEntryId, exitEntryDescription)
    val performer = stdMenuPerformer(menu)
    var exit = false
    while(!exit) {
        exit = (performer.askUntilValidChoice().id == exitEntryId)
    }

}

fun stdAutoMenuUntilExit(title : String,
                     exitEntryId : String = "exit",
                     exitEntryDescription : String = "exit",
                     question : String = "Type your choice",
                     builder : EnumIdMenuBuilder.() -> Unit,
) {
    val menu = autoMenu(title, builder)
    menu.addEntry(exitEntryId, exitEntryDescription)
    val performer = stdMenuPerformer(menu)
    var exit = false
    while(!exit) {
        exit = (performer.askUntilValidChoice().id == exitEntryId)
    }

}