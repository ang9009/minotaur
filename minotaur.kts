import java.util.*

// i is the row index, j is the column index
data class Node(val i: Int, val j: Int)

// Manhattan distance heuristic
fun heuristic(point: Node, goal: Node): Int {
    return Math.abs(point.i - goal.i) + Math.abs(point.j - goal.j)
}

// Checks if a given position is valid: if it is within the maze, and it is an empty space
fun isPositionValid(i: Int, j: Int, grid: Array<IntArray>): Boolean {
    return i in 0 until grid.size && j in 0 until grid[0].size && grid[i][j] == 0;
}

// Returns the next possible neighbors given the current node and a grid
fun getNextStates(current: Node, grid: Array<IntArray>): List<Node> {
    val moves = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val states = mutableListOf<Node>();

    for ((dx, dy) in moves) {
        val i = current.i + dx
        val j = current.j + dy
        val neighbor = Node(i, j)

        if (isPositionValid(i, j, grid)) { 
            states.add(neighbor)
        }
    }

    return states
}

fun astar(grid: Array<IntArray>, start: Node, goal: Node): List<Node>? {
    // Stores nodes based on priority (cost), based on how close they are to the goal
    // and how many steps they are from the starting point
    val openSet = PriorityQueue(compareBy<Pair<Int, Node>> { it.first })
    openSet.add(0 to start)
    // Map that stores previous node for each visited node (parent node)
    val cameFrom = mutableMapOf<Node, Node>()
    // gScore cost for each node
    val gScore = mutableMapOf(start to 0)

    // Main loop, runs until priority queue is empty (all nodes have been processed)
    while (openSet.isNotEmpty()) {
        // Gets pair at front of queue, where current is the current Node
        val (_, current) = openSet.poll()

        if (current == goal) {
            // Reconstruct the path and return
            val path = mutableListOf<Node>()
            var cur: Node? = current
            // Reconstruct the path using the parent node from each node, reversed to get the original path
            while (cur != null) {
                path.add(cur)
                cur = cameFrom[cur]
            }
            path.reverse()
            return path
        }

        for (neighbor in getNextStates(current, grid)) {
        // The gScore of moving from the current node to its neighbour is just the score
        // of the current node + 1, since moving from one tile to another has a uniform cost
        // of 1
            val tentativeG = gScore[current]!! + 1

            if (!gScore.containsKey(neighbor) || tentativeG < gScore[neighbor]!!) {
                gScore[neighbor] = tentativeG
                val fScore = tentativeG + heuristic(neighbor, goal)
                openSet.add(fScore to neighbor)
                cameFrom[neighbor] = current
            }
        }
    }

    // If all nodes have been explored and a path has not been found, return null
    return null
}

fun main() {
    val grid = arrayOf(
        intArrayOf(0, 1, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 1, 0, 1, 0, 1, 1, 1),
        intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
        intArrayOf(1, 1, 0, 1, 0, 1, 1, 0),
        intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
        intArrayOf(1, 0, 1, 1, 0, 1, 0, 1),
        intArrayOf(0, 0, 1, 0, 0, 1, 0, 0)
    )

    val start = Node(2, 1)
    val goal = Node(0, 7)

    val path = astar(grid, start, goal)
    // Reformat for x and y
    val newPath = path?.map {node -> "(x=${node.j + 1}, y=${node.i + 1})"}
    println("Shortest path: $newPath")
}

main();