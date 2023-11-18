import java.util.PriorityQueue

data class Position(val x: Int, val y: Int)

data class State(val theseusPos: Position, val minotaurPos: Position)

// Calculates Manhattan distance heuristic given a position and the goal
fun heuristic(
    point: Position,
    goal: Position,
): Int {
    return Math.abs(point.x - goal.x) + Math.abs(point.y - goal.y)
}

// Checks if a given position is valid: if it is within the maze, and it is an empty space
fun isPositionValid(
    pos: Position,
    maze: Array<IntArray>,
): Boolean {
    return (pos.x in 0 until maze[0].size) && (pos.y in 0 until maze.size) && (maze[pos.y][pos.x] != 1)
}

// Returns the next possible neighbors given the current node and a maze
fun getNextTheseusPositions(
    current: Position,
    maze: Array<IntArray>,
): List<Position> {
    val moves = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val states = mutableListOf<Position>()

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
fun moveMinotaur(
    minotaurCoord: Int,
    theseusCoord: Int,
): Int {
    return when {
        minotaurCoord < theseusCoord -> 1
        minotaurCoord > theseusCoord -> -1
        else -> 0
    }
}

// Returns the next Minotaur position, given Theseus' position and the Minotaur's current position
fun getNextMinotaurPosition(
    minotaurPos: Position,
    theseusPos: Position,
    maze: Array<IntArray>,
): Position {
    var currMinotaurX = minotaurPos.x
    var currMinotaurY = minotaurPos.y

    // The Minotaur should move twice, and it should always try to move horizontally before it tries to
    // move vertically
    for (i in 0 until 2) {
        val dx = moveMinotaur(currMinotaurX, theseusPos.x)
        if (dx != 0 && isPositionValid(Position(currMinotaurX + dx, currMinotaurY), maze)) {
            currMinotaurX += dx
            continue
        }

        val dy = moveMinotaur(currMinotaurY, theseusPos.y)
        if (dy != 0 && isPositionValid(Position(currMinotaurX, currMinotaurY + dy), maze)) {
            currMinotaurY += dy
        }
    }

    return Position(currMinotaurX, currMinotaurY)
}

// Returns next possible states
fun getNextStates(
    state: State,
    maze: Array<IntArray>,
): List<State> {
    val states = mutableListOf<State>()

    // If the Minotaur catches Theseus in any of the possible positions Theseus can occupy,
    // it should not be a possible state
    for (theseusPos in getNextTheseusPositions(state.theseusPos, maze)) {
        val minotaurPos = getNextMinotaurPosition(state.minotaurPos, theseusPos, maze)

        if (minotaurPos == theseusPos) {
            continue
        }

        states.add(State(theseusPos, minotaurPos))
    }

    return states
}

// The main A* algorithm. Takes in the maze, initial state, and the goal
fun aStar(
    maze: Array<IntArray>,
    start: State,
    goal: Position,
) {
    // Priority queue to order states based on cost (heuristic + distance from start). Elements are stored in Pair objects, where the first
    // item is the cost, and the second item is the state.
    val queue = PriorityQueue(compareBy<Pair<Int, State>> { it.first })
    // Initialize priority queue with starting state
    queue.add(0 to start)
    // Map that tracks previous state for each state so that the route can be reconstructed
    val cameFrom = mutableMapOf<State, State>()
    // gScore cost for each Theseus position
    val gScore = mutableMapOf(start to 0)

    // Main loop, runs until priority queue is empty (all nodes have been processed)
    while (queue.isNotEmpty()) {
        // Gets current state from front of priority queue
        val (_, current) = queue.poll()

        // If Theseus reaches the goal, reconstruct the path and terminate
        if (current.theseusPos == goal) {
            val path = mutableListOf<State>()
            var curr: State? = current

            while (curr != null) {
                path.add(curr)
                curr = cameFrom[curr]
            }

            // Reverse the list to get the correct order, then print the positions
            println("Goal reached!")
            path.reversed().forEach {
                println("${it.theseusPos}, ${it.minotaurPos}")
            }

            return
        }

        for (state in getNextStates(current, maze)) {
            // The gScore of moving from the current node to its neighbour is just the score
            // of the current node + 1, since moving from one tile to another has a uniform cost
            // of 1
            val tentativeG = gScore[current]!! + 1

            // If Theseus' position is the same, but the Minotaur's isn't, this should be considered as a
            // different state. Thus, we must use the current state as a key in the gScore function.
            // This allows for backtracking, which is often needed
            if (!gScore.containsKey(state) || tentativeG < gScore[state]!!) {
                gScore[state] = tentativeG
                val fScore = tentativeG + heuristic(state.theseusPos, goal)
                queue.add(fScore to state)
                cameFrom[state] = current
            }
        }
    }

    // If all nodes have been explored (priority queue is emptied), no solution was found
    println("No solution found")
}

fun main() {
    val maze1 =
        arrayOf(
            intArrayOf(0, 1, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 1, 0, 1, 0, 1, 1, 1),
            intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
            intArrayOf(1, 1, 0, 1, 0, 1, 1, 0),
            intArrayOf(0, 0, 0, 1, 0, 0, 0, 0),
            intArrayOf(1, 0, 1, 1, 0, 1, 0, 1),
            intArrayOf(0, 0, 1, 0, 0, 1, 0, 0),
        )

    val maze2 =
        arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 1),
            intArrayOf(0, 1, 1, 1, 0, 1),
            intArrayOf(0, 0, 0, 1, 0, 0),
            intArrayOf(0, 1, 1, 1, 0, 1),
            intArrayOf(0, 0, 0, 0, 0, 1),
        )

    // Maze 1
    val start = State(Position(1, 2), Position(5, 2))
    val goal = Position(7, 0)
    aStar(maze1, start, goal)
}

main()
