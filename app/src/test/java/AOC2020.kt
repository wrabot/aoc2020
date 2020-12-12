import org.junit.Test
import kotlin.math.abs

class AOC2020 : BaseTest("AOC2020") {
    @Test
    fun day1() = test(1) { lines ->
        val numbers = lines.map { it.toInt() }.sorted()
        numbers.product(2020)!!.log()
        numbers.mapIndexedNotNull { index: Int, i: Int ->
            numbers.drop(index + 1).product(2020 - i)?.let { it * i }
        }.first().log()
    }

    private fun List<Int>.product(sum: Int) = find { v -> findLast { it + v == sum } != null }?.let { it * (sum - it) }

    @Test
    fun day2() = test(1) { lines ->
        lines.map { it.split("-", " ", ": ") }.count { (min, max, letter, password) ->
            password.count { it.toString() == letter } in min.toInt()..max.toInt()
        }.log()
        lines.map { it.split("-", " ", ": ") }.count { (first, second, letter, password) ->
            listOf(first, second).count { password[it.toInt() - 1].toString() == letter } == 1
        }.log()
    }

    @Test
    fun day3() = test(1) { lines ->
        val width = lines[0].length
        val height = lines.size
        val map = lines.joinToString("")
        map.slope(width, height, 3, 1).log()
        listOf(1 to 1, 3 to 1, 5 to 1, 7 to 1, 1 to 2).map { map.slope(width, height, it.first, it.second) }
            .reduce { acc, i -> acc * i }.log()
    }

    private fun String.slope(width: Int, height: Int, dx: Int, dy: Int) =
        (0 until height step dy).count { this[it * width + (it / dy * dx) % width] == '#' }

    @Test
    fun day4() = test(1) { lines ->
        val passports = mutableListOf(mutableMapOf<String, String>())
        lines.forEach { line ->
            if (line.isBlank()) {
                passports.add(mutableMapOf())
            } else {
                line.split(" ").map { it.split(":") }.forEach { (key, value) ->
                    passports.last()[key] = value
                }
            }
        }
        val mandatory = mapOf<String, (String) -> Boolean>(
            "byr" to { (it.toIntOrNull() ?: 0) in 1920..2002 },
            "iyr" to { (it.toIntOrNull() ?: 0) in 2010..2020 },
            "eyr" to { (it.toIntOrNull() ?: 0) in 2020..2030 },
            "hgt" to {
                when {
                    it.endsWith("cm") -> it.removeSuffix("cm").toInt() in 150..193
                    it.endsWith("in") -> it.removeSuffix("in").toInt() in 59..76
                    else -> false
                }
            },
            "hcl" to { it.matches("#[0-9a-f]{6}".toRegex()) },
            "ecl" to { it in listOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth") },
            "pid" to { it.matches("[0-9]{9}".toRegex()) }
        )
        passports.count { (it.keys intersect mandatory.keys).size == 7 }.log()
        passports.count { passport ->
            mandatory.all { it.value(passport[it.key].orEmpty()) }
        }.log()
    }

    @Test
    fun day5() = test(1) { lines ->
        val ids = lines.map {
            it.replace('F', '0')
                .replace('B', '1')
                .replace('L', '0')
                .replace('R', '1')
                .toInt(2)
        }.sorted()
        ids.max().log()
        (ids.filterIndexed { index, i -> index + ids.first() != i }.first() - 1).log()
    }

    @Test
    fun day6() = test(1) { lines ->
        val groups = mutableListOf(mutableListOf<String>())
        lines.forEach {
            if (it.isBlank()) {
                groups.add(mutableListOf())
            } else {
                groups.last().add(it)
            }
        }
        groups.map { it.reduce { acc, s -> acc + s }.toSet().count() }.sum().log()
        groups.map { g -> g.map { it.toSet() }.reduce { acc, s -> acc intersect s }.count() }.sum().log()
    }

    @Test
    fun day7() = test(1) { lines ->
        val rules = lines.map { it.split(" bags contain ") }.map { (a, b) ->
            a to if (b.startsWith("no")) emptyList() else b.split(", ").map {
                it.split(" ").let { (n, u, v) -> n.toInt() to "$u $v" }
            }
        }.toMap()
        val bags = mutableSetOf<String>()
        rules.keys.forEach {
            if (rules.contains(it, "shiny gold")) bags.add(it)
        }
        bags.count().log()
        rules.count("shiny gold").log()
    }

    private fun Map<String, List<Pair<Int, String>>>.contains(container: String, name: String): Boolean =
        getValue(container).any { it.second == name || contains(it.second, name) }

    private fun Map<String, List<Pair<Int, String>>>.count(container: String): Int =
        getValue(container).map { it.first * (1 + count(it.second)) }.sum()

    @Test
    fun day8() = test(1) { lines ->
        val code = lines.map { it.split(" ") }.map { (op, arg) -> Ins(op, arg.toInt()) }
        code.run().second.log()
        for (fixIndex in code.indices) {
            val original = code[fixIndex].op
            val fixOp = when (original) {
                "nop" -> "jmp"
                "jmp" -> "nop"
                else -> null
            }
            if (fixOp != null) {
                code[fixIndex].op = fixOp
                val result = code.run()
                if (result.first == code.size) {
                    result.second.log()
                    break
                }
                code[fixIndex].op = original
            }
        }
    }

    private fun List<Ins>.run(): Pair<Int, Int> {
        forEach { it.used = false }
        var acc = 0
        var index = 0
        while (index < size && !this[index].used) {
            this[index].used = true
            when (this[index].op) {
                "jmp" -> index += this[index].arg
                "acc" -> acc += this[index++].arg
                else -> index++
            }
        }
        return index to acc
    }

    data class Ins(var op: String, val arg: Int, var used: Boolean = false)

    @Test
    fun day9() = test(1) { lines ->
        val numbers = lines.map { it.toLong() }
        var invalid = 0L
        find@ for (i in 25 until numbers.size) {
            for (first in 1..25) {
                for (second in (first + 1)..25) {
                    if (numbers[i] == numbers[i - first] + numbers[i - second]) continue@find
                }
            }
            invalid = numbers[i]
            break
        }
        invalid.log()
        sum@ for (i in numbers.indices) {
            var sum = 0L
            for (j in i until numbers.size) {
                sum += numbers[j]
                if (sum > invalid) continue@sum
                if (sum == invalid) {
                    val block = numbers.subList(i, j)
                    val weakness = block.min()!! + block.max()!!
                    weakness.log()
                    break@sum
                }
            }
            break
        }
    }

    @Test
    fun day10() = test(3) { lines ->
        val adapters = lines.map { it.toInt() }.sortedDescending()
        val links = adapters + 0
        val groups = links.groupBy({ it }) { v -> links.filter { it in v - 3 until v } }
            .mapValues { it.value.flatten() }
        val list = mutableListOf(links.first())
        while (list.last() != 0) {
            list.add(groups.getValue(list.last()).first())
        }
        val diff = list.zipWithNext { a, b -> a - b }
        val result = diff.count { it == 1 } * (diff.count { it == 3 } + 1)
        result.log()
        val counts = mutableMapOf(0 to 1L)
        adapters.reversed().forEach {
            counts[it] = groups.getValue(it).fold(0L) { acc, i -> acc + counts[i]!! }
        }
        counts[adapters.first()].log()
    }

    @Test
    fun day11() = test(2) { lines ->
        val width = lines[0].length
        val height = lines.size

        var map = lines.flatMap { it.toList() }
        while (true) {
            map = map.step1(width, height) ?: break
        }
        map.count { it == '#' }.log()

        map = lines.flatMap { it.toList() }
        while (true) {
            map = map.step2(width, height) ?: break
        }
        map.count { it == '#' }.log()
    }

    private fun List<Char>.step2(width: Int, height: Int): List<Char>? {
        var modified = false
        val map = mapIndexed { index, c ->
            val x = index % width
            val y = index / width
            when (c) {
                'L' -> if (adjacent2(width, height, x, y) == 0) '#' else 'L'
                '#' -> if (adjacent2(width, height, x, y) >= 5) 'L' else '#'
                else -> c
            }.apply { if (c != this) modified = true }
        }
        return if (modified) map else null
    }

    private fun List<Char>.adjacent2(width: Int, height: Int, x0: Int, y0: Int) = directions.count {
        var d = 1
        var occupied: Boolean? = null
        while (occupied == null) {
            val x = x0 + it.first * d
            val y = y0 + it.second * d
            occupied = if ((x in 0 until width) && (y in 0 until height)) {
                when (get(y * width + x)) {
                    '#' -> true
                    'L' -> false
                    else -> null
                }
            } else false
            d++
        }
        occupied
    }

    private fun List<Char>.step1(width: Int, height: Int): List<Char>? {
        var modified = false
        val map = mapIndexed { index, c ->
            val x = index % width
            val y = index / width
            when (c) {
                'L' -> if (adjacent1(width, height, x, y) == 0) '#' else 'L'
                '#' -> if (adjacent1(width, height, x, y) >= 4) 'L' else '#'
                else -> c
            }.apply { if (c != this) modified = true }
        }
        return if (modified) map else null
    }

    private fun List<Char>.adjacent1(width: Int, height: Int, x0: Int, y0: Int) = directions.count {
        val x = x0 + it.first
        val y = y0 + it.second
        (x in 0 until width) && (y in 0 until height) && (get(y * width + x) == '#')
    }

    private val directions = listOf(1 to 0, 1 to -1, 0 to -1, -1 to -1, -1 to 0, -1 to 1, 0 to 1, 1 to 1)

    @Test
    fun day12() = test(2) { lines ->
        day12part1(lines)
        day12part2(lines)
    }

    private fun day12part1(lines: List<String>) {
        var x = 0
        var y = 0
        var dir = 0
        lines.map { Pair(it.first(), it.drop(1).toInt()) }.forEach { (op, value) ->
            when (op) {
                'R' -> dir = (dir - value + 360) % 360
                'L' -> dir = (dir + value) % 360
                'E' -> x += value
                'N' -> y -= value
                'W' -> x -= value
                'S' -> y += value
                'F' -> when (dir) {
                    0 -> x += value
                    90 -> y -= value
                    180 -> x -= value
                    270 -> y += value
                    else -> error("Should not happen")
                }
                else -> error("Should not happen")
            }
        }
        (abs(x) + abs(y)).log()
    }

    private fun day12part2(lines: List<String>) {
        var x = 0
        var y = 0
        var wx = 10
        var wy = -1
        lines.map { Pair(it.first(), it.drop(1).toInt()) }.forEach { (op, value) ->
            when (op) {
                'R' -> rotate(wx, wy, 360 - value).let {
                    wx = it.first
                    wy = it.second
                }
                'L' -> rotate(wx, wy, value).let {
                    wx = it.first
                    wy = it.second
                }
                'E' -> wx += value
                'N' -> wy -= value
                'W' -> wx -= value
                'S' -> wy += value
                'F' -> {
                    x += value * wx
                    y += value * wy
                }
                else -> error("Should not happen")
            }
        }
        (abs(x) + abs(y)).log()
    }

    private fun rotate(x: Int, y: Int, degree: Int) = when (degree) {
        90 -> y to -x
        180 -> -x to -y
        270 -> -y to x
        else -> error("Should not happen")
    }

    @Test
    fun day13() = test(1) { lines ->
    }
}