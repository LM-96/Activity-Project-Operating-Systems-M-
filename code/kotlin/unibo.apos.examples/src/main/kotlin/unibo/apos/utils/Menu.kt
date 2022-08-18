package unibo.apos.utils

data class MenuEntry(
    val id : String,
    val description : String,
    var enabled : Boolean = true
)

data class SelectableMenuEntry(
    val menuEntry : MenuEntry,
    var onSelection: (MenuEntry) -> Unit = {}
)

class Menu(val title : String) {

    private val entries = mutableMapOf<String, SelectableMenuEntry>()
    private val onEntrySelected = mutableListOf<(MenuEntry) -> Unit>()

    operator fun get(id : String) : MenuEntry? {
        return entries[id]?.menuEntry
    }

    fun addEntry(id : String, description: String, enabled: Boolean = true, onSelection: (MenuEntry) -> Unit = {}) {
        if(entries.containsKey(id))
            throw IllegalArgumentException("id \'$id\' is already present")

        entries[id] = SelectableMenuEntry(MenuEntry(id, description, enabled), onSelection)
    }

    fun enableEntry(id : String) : Boolean {
        if(!entries.containsKey(id))
            return false

        entries[id]!!.menuEntry.enabled = true
        return true
    }

    fun disableEntry(id : String) : Boolean {
        if(!entries.containsKey(id))
            return false

        entries[id]!!.menuEntry.enabled = false
        return true
    }

    fun removeEntry(id : String) : MenuEntry? {
        return entries[id]?.menuEntry
    }

    fun addOnEntrySelectedCallback(callback : (MenuEntry) -> Unit) =
        onEntrySelected.add(callback)

    fun removeOnEntrySelectedCallback(callback : (MenuEntry) -> Unit) =
        onEntrySelected.remove(callback)

    fun changeOnSelectionOf(id : String, newOnSelection : (MenuEntry) -> Unit)  =
        entries[id]?.apply { this.onSelection = newOnSelection }

    fun getEntries() : Map<String, MenuEntry> {
        return entries.values
            .map { it.menuEntry }
            .associateBy { it.id }
    }

    fun select(id : String) {
        val entry = entries[id] ?: throw IllegalArgumentException("no entry with id \'$id\'")
        entry.onSelection(entry.menuEntry)
        onEntrySelected.forEach { it(entry.menuEntry) }
    }

}

@DslMarker annotation class MenuDsl

@MenuDsl object enabled
@MenuDsl object disabled
@MenuDsl data class MenuBuilderIdContinuation(val id : String, val enabled : Boolean)
@MenuDsl data class MenuBuilderDescriptionContinuation(val entry : MenuEntry)

@MenuDsl class MenuBuilder(private val title : String) {

    private val basicEntries = mutableMapOf<String, SelectableMenuEntry>()
    private var onEntrySelectedCallbacks = mutableListOf<(MenuEntry) -> Unit>()

    fun build() : Menu {
        val menu = Menu(title)
        for(be in basicEntries.values) {
            menu.addEntry(be.menuEntry.id, be.menuEntry.description, be.menuEntry.enabled, be.onSelection)
        }
        onEntrySelectedCallbacks.forEach {
            menu.addOnEntrySelectedCallback(it)
        }

        return menu
    }

    infix fun enabled.entry(id : String) : MenuBuilderIdContinuation {
        return MenuBuilderIdContinuation(id, true)
    }

    infix fun disabled.entry(id : String) : MenuBuilderIdContinuation {
        return MenuBuilderIdContinuation(id, false)
    }

    infix fun MenuBuilderIdContinuation.description(description: String) : MenuBuilderDescriptionContinuation {
        val entry = SelectableMenuEntry(MenuEntry(id, description, enabled))
        this@MenuBuilder.basicEntries[id] = entry
        return MenuBuilderDescriptionContinuation(entry.menuEntry)
    }

    infix fun MenuBuilderDescriptionContinuation.onSelection(onSelection: (MenuEntry) -> Unit) {
        this@MenuBuilder.basicEntries[entry.id]!!
    }

    @MenuDsl infix fun onUserSelection(action : (MenuEntry) -> Unit) {
        onEntrySelectedCallbacks.add(action)
    }
}

@MenuDsl class EnumIdMenuBuilder(private val title : String) {

    private val basicEntries = mutableMapOf<String, SelectableMenuEntry>()
    private var onEntrySelectedCallbacks = mutableListOf<(MenuEntry) -> Unit>()
    var counter = 1

    fun build() : Menu {
        val menu = Menu(title)
        for(be in basicEntries.values) {
            menu.addEntry(be.menuEntry.id, be.menuEntry.description, be.menuEntry.enabled, be.onSelection)
        }
        onEntrySelectedCallbacks.forEach {
            menu.addOnEntrySelectedCallback(it)
        }

        return menu
    }

    infix fun enabled.entry(description: String) : MenuBuilderDescriptionContinuation =
        this@EnumIdMenuBuilder.addEntry(this@EnumIdMenuBuilder.counter++.toString(), description, true)

    infix fun disabled.entry(description: String) : MenuBuilderDescriptionContinuation =
        this@EnumIdMenuBuilder.addEntry(this@EnumIdMenuBuilder.counter++.toString(), description, false)

    private fun addEntry(id : String, description: String, enabled : Boolean) : MenuBuilderDescriptionContinuation {
        val entry = SelectableMenuEntry(MenuEntry(id, description, enabled))
        this.basicEntries[id] = entry
        return MenuBuilderDescriptionContinuation(entry.menuEntry)
    }

    infix fun MenuBuilderDescriptionContinuation.onSelection(onSelection: (MenuEntry) -> Unit) {
        this@EnumIdMenuBuilder.basicEntries[entry.id]!!.onSelection = onSelection
    }

    @MenuDsl infix fun onUserSelection(action : (MenuEntry) -> Unit) {
        onEntrySelectedCallbacks.add(action)
    }
}

fun menu(title : String, builder : MenuBuilder.() -> Unit) : Menu =
    MenuBuilder(title).apply(builder).build()

fun autoMenu(title : String, builder : EnumIdMenuBuilder.() -> Unit) : Menu =
    EnumIdMenuBuilder(title).apply(builder).build()
