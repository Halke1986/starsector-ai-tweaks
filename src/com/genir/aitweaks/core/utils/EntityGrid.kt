package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.combat.CombatEntityAPI
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max
import kotlin.math.min

class EntityGrid(entities: List<CombatEntityAPI>?) {
    companion object {
        const val CELL_SIZE = 300f
    }

    private val grid: Array<Array<EntityRecord>?>
    private val gridMinX: Float
    private val gridMaxX: Float
    private val gridMinY: Float
    private val gridMaxY: Float
    private val gridSizeX: Int
    private val gridSizeY: Int
    private val entityNumber: Int

    init {
        if (entities.isNullOrEmpty()) {
            grid = arrayOf()
            gridMinX = 0f
            gridMaxX = 0f
            gridMinY = 0f
            gridMaxY = 0f
            gridSizeX = 0
            gridSizeY = 0
            entityNumber = 0
        } else {
            var minX = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE

            entities.forEach { entity ->
                val l = entity.location
                val r = entity.collisionRadius

                minX = min(minX, l.x - r)
                maxX = max(maxX, l.x + r)
                minY = min(minY, l.y - r)
                maxY = max(maxY, l.y + r)
            }

            gridMinX = minX
            gridMaxX = maxX
            gridMinY = minY
            gridMaxY = maxY

            gridSizeX = ((maxX - minX) / CELL_SIZE).toInt() + 1
            gridSizeY = ((maxY - minY) / CELL_SIZE).toInt() + 1

            grid = arrayOfNulls(gridSizeX * gridSizeY)
            entityNumber = entities.size

            val tmpGrid = arrayOfNulls<MutableList<EntityRecord>?>(gridSizeX * gridSizeY)
            entities.forEachIndexed { idx, entity ->
                addRecord(EntityRecord(entity, idx), tmpGrid)
            }

            for (idx in tmpGrid.indices) {
                tmpGrid[idx]?.let { cell ->
                    grid[idx] = cell.toTypedArray()
                }
            }
        }
    }

    fun getEntities(l: Vector2f, r: Float): Sequence<CombatEntityAPI> {
        return EntityIterator(l, r).asSequence()
    }

    private fun addRecord(record: EntityRecord, grid: Array<MutableList<EntityRecord>?>) {
        val entity = record.entity
        val l = entity.location
        val r = entity.collisionRadius

        val minX = ((l.x - r - gridMinX) / CELL_SIZE).toInt().coerceAtLeast(0)
        val maxX = ((l.x + r - gridMinX) / CELL_SIZE).toInt().coerceAtMost(gridSizeX - 1)
        val minY = ((l.y - r - gridMinY) / CELL_SIZE).toInt().coerceAtLeast(0)
        val maxY = ((l.y + r - gridMinY) / CELL_SIZE).toInt().coerceAtMost(gridSizeY - 1)

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val idx = y * gridSizeX + x
                val cell: MutableList<EntityRecord> = grid[idx] ?: kotlin.run {
                    val newCell = mutableListOf<EntityRecord>()
                    grid[idx] = newCell
                    newCell
                }

                cell.add(record)
            }
        }
    }

    data class EntityRecord(val entity: CombatEntityAPI, val n: Int)

    private inner class EntityIterator(l: Vector2f, r: Float) : Iterator<CombatEntityAPI> {
        private val visited = BooleanArray(entityNumber)

        private val xMin = ((l.x - r - gridMinX) / CELL_SIZE).toInt().coerceAtLeast(0)
        private val xMax = ((l.x + r - gridMinX) / CELL_SIZE).toInt().coerceAtMost(gridSizeX - 1)
        private val yMin = ((l.y - r - gridMinY) / CELL_SIZE).toInt().coerceAtLeast(0)
        private val yMax = ((l.y + r - gridMinY) / CELL_SIZE).toInt().coerceAtMost(gridSizeY - 1)

        private var x = xMin
        private var y = yMin
        private var cell: Array<EntityRecord>? = null
        private var cellIdx = 0

        private var next: EntityRecord? = findNext()

        override fun hasNext(): Boolean {
            return next != null
        }

        override fun next(): CombatEntityAPI {
            val record = next!!

            visited[record.n] = true
            next = findNext()
            return record.entity
        }

        private fun findNext(): EntityRecord? {
            while (cell != null || y < yMax) {
                val found = findNextInCell(cell)
                if (found != null) {
                    return found
                }

                // Find next cell.
                cell = null
                if (y < yMax) {
                    cell = grid[y * gridSizeX + x]
                    cellIdx = 0

                    if (x < xMax) {
                        x++
                    } else {
                        x = xMin
                        y++
                    }
                }
            }

            return null
        }

        private fun findNextInCell(cell: Array<EntityRecord>?): EntityRecord? {
            if (cell == null) {
                return null
            }

            while (cellIdx < cell.size) {
                val next = cell[cellIdx]
                cellIdx++

                if (!visited[next.n]) {
                    return next
                }
            }

            return null
        }
    }
}
