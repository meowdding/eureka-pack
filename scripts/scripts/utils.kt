import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import me.owdding.catharsis.utils.codecs.SavableData
import me.owdding.catharsis.utils.extensions.unsafeCast
import net.minecraft.resources.Identifier
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.text.endsWith

private val gson = GsonBuilder().setPrettyPrinting().create()
private val thousandsPlace = listOf("", "M", "MM", "MMM")
private val hundredsPlace = listOf("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM")
private val tensPlace = listOf("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC")
private val onesPlace = listOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")

fun Int.toRomanNumeral(): String {
    return thousandsPlace[this / 1000] + hundredsPlace[this % 1000 / 100] + tensPlace[this % 100 / 10] + onesPlace[this % 10]
}

fun <T> T.toJson(codec: Codec<T>): JsonElement = codec.encodeStart(JsonOps.INSTANCE, this).getOrThrow()
fun JsonElement.prettyPrint(): String = gson.toJson(this)

interface EurekaContext {
    val path: Path
    val templates: Path

    fun getOverlay(id: String): EurekaContext

    fun save(identifier: Identifier, element: JsonElement) {
        val path = path.resolve("assets")
            .resolve(identifier.namespace)
            .resolve(identifier.path)

        println("Saving $path")

        path.createParentDirectories().writeText(element.prettyPrint())
    }

    fun hasTexture(texture: Identifier): Boolean {
        return path.resolve("assets").resolve(texture.namespace).resolve("textures").resolve(texture.withPath {
            if (it.endsWith(".png")) it else "$it.png"
        }.path).exists()
    }

    fun <Type : SavableData<Type>> save(identifier: Identifier, data: SavableData<Type>) {
        save(data.toFileName(identifier), data.toJson(data.codec.unsafeCast()))
    }
}

fun Identifier.asTexturePath(context: EurekaContext) = context.path.resolve("assets").resolve(this.namespace).resolve("textures").resolve(this.withPath {
    if (it.endsWith(".png")) it else "$it.png"
}.path)

data class EurekaContextOverlay(
    override val path: Path,
    override val templates: Path
) : EurekaContext {

    override fun getOverlay(id: String) = throw UnsupportedOperationException("")
}

data class EurekaContextRoot(
    override val path: Path,
    override val templates: Path
) : EurekaContext {
    override fun getOverlay(id: String) = EurekaContextOverlay(path.resolve(id), templates)
}

fun interface EurekaDataProvider {
    fun provide(context: EurekaContext)

    fun modify(context: EurekaContext): EurekaContext = context
}

interface EurekaOverlayDataProvider : EurekaDataProvider {
    val overlay: String

    override fun modify(context: EurekaContext): EurekaContext = context.getOverlay(overlay)
}

fun main() {
    bootstrap()

    val providers = listOf<EurekaDataProvider>(
        
    )
    val context = EurekaContextRoot(Path("catharsis"), Path("gui_skyblock_gui_templates"))

    providers.forEach {
        it.provide(it.modify(context))
    }
}

fun json(init: JsonObject.() -> Unit): JsonObject = JsonObject().apply(init)

context(jsonObject: JsonObject)
operator fun String.invoke(init: JsonObject.() -> Unit) {
    jsonObject.add(this, JsonObject().apply(init))
}

context(jsonObject: JsonObject)
operator fun String.invoke(vararg entries: JsonObject.() -> Unit) {
    jsonObject.add(this, JsonArray().apply {
        entries.forEach {
            add(JsonObject().apply(it))
        }
    })
}

context(jsonObject: JsonObject)
operator fun String.invoke(number: Number) {
    jsonObject.addProperty(this, number)
}

context(jsonObject: JsonObject)
operator fun String.invoke(vararg entries: Number) {
    jsonObject.add(this, JsonArray().apply {
        entries.forEach(::add)
    })
}
context(jsonObject: JsonObject)
operator fun String.invoke(identifier: Identifier) = this(identifier.toString())

context(jsonObject: JsonObject)
operator fun String.invoke(vararg entries: Identifier) = this(*entries.map { it.toString() }.toTypedArray())

context(jsonObject: JsonObject)
operator fun String.invoke(boolean: Boolean) {
    jsonObject.addProperty(this, boolean)
}

context(jsonObject: JsonObject)
operator fun String.invoke(vararg entries: Boolean) {
    jsonObject.add(this, JsonArray().apply {
        entries.forEach(::add)
    })
}

context(jsonObject: JsonObject)
operator fun String.invoke(string: String) {
    jsonObject.addProperty(this, string)
}

context(jsonObject: JsonObject)
operator fun String.invoke(vararg entries: String) {
    jsonObject.add(this, JsonArray().apply {
        entries.forEach(::add)
    })
}



fun JsonObject.obj(name: String, init: JsonObject.() -> Unit) = this.add(name, JsonObject().apply(init))
fun JsonObject.array(name: String, init: JsonArray.() -> Unit) = this.add(name, JsonArray().apply(init))
fun JsonArray.obj(init: JsonObject.() -> Unit) = this.add(JsonObject().apply(init))
fun JsonArray.array(init: JsonArray.() -> Unit) = this.add(JsonArray().apply(init))