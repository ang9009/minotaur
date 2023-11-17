import java.util.*
// import khoury.*

data class Position(val x: Int, val y: Int)
data class State(val theseusPos: Position, val minotaurPos: Position)

// Manhattan distance heuristic
fun heuristic(point: Position, goal: Position): Int {
    return Math.abs(point.x - goal.x) + Math.abs(point.y - goal.y)
}

// Checks if a given position is valid: if it is within the maze, and it is an empty space
fun isPositionValid(pos: Position, maze: Array<IntArray>): Boolean {
    return pos.y in 0 until maze.size && pos.x in 0 until maze[0].size && maze[pos.y][pos.x] == 0;
}

// Returns the next possible neighbors given the current node and a maze
fun getNextTheseusPositions(current: Position, maze: Array<IntArray>): List<Position> {
    val moves = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val states = mutableListOf<Position>();

    for ((dx, dy) in moves) {
        val x = current.x + dx
        val y = current.y + dy
        val neighbor = Position(x, y)

        if (isPositionValid(neighbor, maze)) { 
            states.add(neighbor)
        }
    }

    return states
}

// Return change in x or y based on given horizontal/vertical coordinates
fun moveMinotaur(minotaurCoord: Int, theseusCoord: Int): Int {
    return when {
        minotaurCoord < theseusCoord -> 1
        minotaurCoord > theseusCoord -> -1
        else -> 0
    }
}

fun getNextMinotaurPosition(minotaurPos: Position, theseusPos: Position, maze: Array<IntArray>): Position {
    var currMinotaurX = minotaurPos.x;
    var currMinotaurY = minotaurPos.y;

    for(i in 0 until 2) {
        val dx = moveMinotaur(currMinotaurX, theseusPos.x)
        if(dx != 0 && isPositionValid(Position(currMinotaurX + dx, currMinotaurY), maze)) {
            currMinotaurX += dx
            continue
        }

        val dy = moveMinotaur(currMinotaurY, theseusPos.y)
        if(dy != 0 && isPositionValid(Position(currMinotaurX, currMinotaurY + dy), maze)) {
            currMinotaurY += dy
        }
    }

    return Position(currMinotaurX, currMinotaurY)
}

// Returns next possible states
fun getNextStates(state: State, maze: Array<IntArray>): List<State> {
    val states = mutableListOf<State>();

    // Adding possible states based on whether or not the minotaur gets to Theseus
    for(theseusPos in getNextTheseusPositions(state.theseusPos, maze)) {
        val minotaurPos = getNextMinotaurPosition(state.minotaurPos, theseusPos, maze)

        if(minotaurPos == theseusPos) {
            continue
        }

        states.add(State(theseusPos, minotaurPos))
    }

    return states
}

fun aStar(maze: Array<IntArray>, start: State, goal: Position): List<State>? {
    // Stores nodes based on priority (cost), based on how close they are to the goal
    // and how many steps they are from the starting point
    val openSet = PriorityQueue(compareBy<Pair<Int, State>> { it.first })
    openSet.add(0 to start)
    // Map that tracks previous state for each state so that the route can be reconstructed
    val cameFrom = mutableMapOf<State, State>()
    // gScore cost for each theseus position
    val gScore = mutableMapOf(start to 0)
    
    // Main loop, runs until priority queue is empty (all nodes have been processed)
    while (openSet.isNotEmpty()) {
        // Gets pair at front of queue, where current is the current Position
        val (_, current) = openSet.poll()

        // If we find the goal, reconstruct the path and return
        if (current.theseusPos == goal) {
            val path = mutableListOf<State>()
            var cur: State? = current
            // Reconstruct the path using the parent node from each node, reversed to get the original path
            while (cur != null) {
                path.add(cur)
                cur = cameFrom[cur]
            }
            path.reverse()
            return path
        }

        for (state in getNextStates(current, maze)) {
        // The gScore of moving from the current node to its neighbour is just the score
        // of the current node + 1, since moving from one tile to another has a uniform cost
        // of 1
            val tentativeG = gScore[current]!! + 1

            if (!gScore.containsKey(state) || tentativeG < gScore[state]!!) {
                gScore[state] = tentativeG
                val fScore = tentativeG + heuristic(state.theseusPos, goal)
                openSet.add(fScore to state)
                cameFrom[state] = current
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

    val start = State(Position(1, 2), Position(5, 2))
    val goal = Position(7, 0)

    val path = aStar(maze, start, goal)
    val newPath = path?.map {state -> "${state.theseusPos}, ${state.minotaurPos}"}
    println(newPath)
}

// @EnabledTest
// fun testGetNextMinotaurPosition() {
//     val maze = arrayOf(
//         intArrayOf(0, 1, 0, 0, 0, 0, 0, 0),
//         intArrayOf(0, 1, 0, 1, 0, 1, 1, 1),
//         intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
//         intArrayOf(1, 1, 0, 1, 0, 1, 1, 0),
//         intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
//         intArrayOf(1, 0, 1, 1, 0, 1, 0, 1),
//         intArrayOf(0, 0, 1, 0, 0, 1, 0, 0)
//     )    

//     testSame(getNextMinotaurPosition(Position(5, 2), Position(2, 2), maze), Position(4, 2), "First step")
//     testSame(getNextMinotaurPosition(Position(4, 2), Position(2, 3), maze), Position(4, 3), "Second step")
// }


// @EnabledTest
// fun testGetNextStates() {
//     val maze = arrayOf(
//         intArrayOf(0, 1, 0, 0, 0, 0, 0, 0),
//         intArrayOf(0, 1, 0, 1, 0, 1, 1, 1),
//         intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
//         intArrayOf(1, 1, 0, 1, 0, 1, 1, 0),
//         intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
//         intArrayOf(1, 0, 1, 1, 0, 1, 0, 1),
//         intArrayOf(0, 0, 1, 0, 0, 1, 0, 0)
//     ) 
//     val state1 = State(Position(0, 2), Position(4, 2))
//     val state2 = State(Position(2, 2), Position(4, 2))

//     val state3 = State(Position(1, 2), Position(4, 2))
//     val state4 = State(Position(2, 1), Position(4, 1))
//     val state5 = State(Position(2, 3), Position(4, 3))

//     testSame(getNextStates(State(Position(1, 2), Position(5, 2)), maze), listOf(state1, state2), "First step")
//     testSame(getNextStates(State(Position(2, 2), Position(4, 2)), maze), listOf(state3, state4, state5), "Second step")
// }


main()
// runEnabledTests(this)