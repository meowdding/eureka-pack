#!/usr/bin/env kotlin

import java.awt.image.BufferedImage
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO
import kotlin.io.path.createParentDirectories
import kotlin.io.path.extension
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.visitFileTree

val scriptDir: Path = __FILE__.parentFile.toPath().toAbsolutePath()
val eurekaDir: Path = scriptDir.parent.parent.parent

val colors = scriptDir.resolve("colors.csv").readText().lines().map {
    it.substringBefore("//").trim()
}.filter { it.isNotEmpty() }.iterator()

data class Template(val name: String, val mappings: MutableMap<Int, Int> = mutableMapOf()) {
    val templateDir: Path get() = eurekaDir.resolve("eureka").resolve("template_$name")

    operator fun component3() = templateDir
}


val templates: MutableList<Template> = mutableListOf()
fun parseTemplates() {
    val template = colors.next()

    template.substringAfter(",").split(",").forEach {
        templates.add(Template(it.trim()))
    }
}
parseTemplates()

fun String.parseColor() = this.removePrefix("#").trim().toInt(16)

while (colors.hasNext()) {
    val line = colors.next()
    val parts = line.split(",")
    val template = parts[0].parseColor() and 0xFFFFFF

    parts.drop(1).forEachIndexed { index, color ->
        templates[index].mappings[template] = color.parseColor() and 0xFFFFFF
    }
}

val templateDir: Path = eurekaDir.resolve("template")
templateDir.visitFileTree {
    onVisitFile { file, _ ->
        if (file.extension.lowercase() != "png") {
            templates.forEach { (_, _, templateDir) ->
                Files.copy(
                    file,
                    templateDir.resolve(file.relativeTo(templateDir)).createParentDirectories(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            return@onVisitFile FileVisitResult.CONTINUE
        }
        val sourceImage: BufferedImage = ImageIO.read(Files.newInputStream(file))

        templates.forEach { (name, mappings, templateDir) ->
            val templatedImage = BufferedImage(sourceImage.width, sourceImage.height, BufferedImage.TYPE_INT_ARGB)
            val templatedFile: Path = templateDir.resolve(file.relativeTo(templateDir))

            repeat(sourceImage.width) { x ->
                repeat(sourceImage.height) { y ->
                    fun getColor() = sourceImage.getRGB(x, y)
                    fun setColor(color: Int) = templatedImage.setRGB(x, y, color)

                    val color = getColor()
                    setColor(mappings[color and 0xFFFFFF]?.or(0xFF000000u.toInt()) ?: color)
                }
            }

            ImageIO.write(templatedImage, "PNG", templatedFile.createParentDirectories().outputStream())
        }

        FileVisitResult.CONTINUE
    }
}
