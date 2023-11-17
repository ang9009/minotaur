import java.util.*

data class Node(val x: Int, val y: Int)

// Manhattan distance heuristic
fun heuristic(point: Node, goal: Node): Int {
    return Math.abs(point.x - goal.x) + Math.abs(point.y - goal.y)
}

// Checks if a given position is valid: if it is within the maze, and it is an empty space
fun isPositionValid(x: Int, y: Int, maze: Array<IntArray>): Boolean {
    return y in 0 until maze.size && x in 0 until maze[0].size && maze[y][x] == 0;
}

// Returns the next possible neighbors given the current node and a maze
fun getNextTheseusPositions(current: Node, maze: Array<IntArray>): List<Node> {
    val moves = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val states = mutableListOf<Node>();

    for ((dx, dy) in moves) {
        val x = current.x + dx
        val y = current.y + dy
        val neighbor = Node(x, y)

        if (isPositionValid(x, y, maze)) { 
            states.add(neighbor)
        }
    }

    return states
}

// Return change in i or j based on given horizontal/vertical coordinates
fun moveMinotaur(minotaurCoord: Int, theseusCoord: Int): Int {
    return when {
        minotaurCoord < theseusCoord -> 1
        minotaurCoord > theseusCoord -> -1
        else -> 0
    }
}

// fun getNextMinotaurPosition(minotaurPos: Node, theseusPos: Node) {
//     repeat(2) {
//         val dx = moveMinotaur(minotaurPos.j, theseusPos.j)
//     }
// }

fun astar(maze: Array<IntArray>, start: Node, goal: Node): List<Node>? {
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

        // If we find the goal, reconstruct the path and return
        if (current == goal) {
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

        for (neighbor in getNextTheseusPositions(current, maze)) {
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
    val maze = arrayOf(
        intArrayOf(0, 1, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 1, 0, 1, 0, 1, 1, 1),
        intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
        intArrayOf(1, 1, 0, 1, 0, 1, 1, 0),
        intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
        intArrayOf(1, 0, 1, 1, 0, 1, 0, 1),
        intArrayOf(0, 0, 1, 0, 0, 1, 0, 0)
    )

    val start = Node(1, 2)
    val goal = Node(7, 0)

    val path = astar(maze, start, goal)
    println("Shortest path: $path")
}

main();