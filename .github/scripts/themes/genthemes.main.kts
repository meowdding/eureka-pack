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

data class Theme(val name: String, val mappings: MutableMap<Int, Int> = mutableMapOf()) {
    val themeDir: Path get() = eurekaDir.resolve("eureka").resolve("theme_$name")

    operator fun component3() = themeDir
}


val themes: MutableList<Theme> = mutableListOf()
fun parseThemes() {
    val theme = colors.next()

    theme.substringAfter(",").split(",").forEach {
        themes.add(Theme(it.trim()))
    }
}
parseThemes()

fun String.parseColor() = this.removePrefix("#").trim().toInt(16)

while (colors.hasNext()) {
    val line = colors.next()
    val parts = line.split(",")
    val template = parts[0].parseColor() and 0xFFFFFF

    parts.drop(1).forEachIndexed { index, color ->
        themes[index].mappings[template] = color.parseColor() and 0xFFFFFF
    }
}

val templateDir: Path = eurekaDir.resolve("theme_template")
templateDir.visitFileTree {
    onVisitFile { file, _ ->
        if (file.extension.lowercase() != "png") {
            themes.forEach { (_, _, themeDir) ->
                Files.copy(
                    file,
                    themeDir.resolve(file.relativeTo(templateDir)).createParentDirectories(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            return@onVisitFile FileVisitResult.CONTINUE
        }
        val sourceImage: BufferedImage = ImageIO.read(Files.newInputStream(file))

        themes.forEach { (name, mappings, themeDir) ->
            val themedImage = BufferedImage(sourceImage.width, sourceImage.height, BufferedImage.TYPE_INT_ARGB)
            val themedFile: Path = themeDir.resolve(file.relativeTo(templateDir))

            repeat(sourceImage.width) { x ->
                repeat(sourceImage.height) { y ->
                    fun getColor() = sourceImage.getRGB(x, y)
                    fun setColor(color: Int) = themedImage.setRGB(x, y, color)

                    val color = getColor()
                    setColor(mappings[color and 0xFFFFFF]?.or(0xFF000000u.toInt()) ?: color)
                }
            }

            ImageIO.write(themedImage, "PNG", themedFile.createParentDirectories().outputStream())
        }

        FileVisitResult.CONTINUE
    }
}
