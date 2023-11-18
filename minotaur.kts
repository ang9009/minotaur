import java.util.PriorityQueue

// Classes representing the position of an agent and the state of the game
data class Position(val x: Int, val y: Int)

data class State(val theseusPos: Position, val minotaurPos: Position)

// Calculates Manhattan distance heuristic given a position and the goal
fun getHeuristic(
    point: Position,
    goal: Position,
): Int {
    return Math.abs(point.x - goal.x) + Math.abs(point.y - goal.y)
}

// Checks if a given position is valid: if it is within the maze, and if it is an empty space
fun isPositionValid(
    pos: Position,
    maze: Array<CharArray>,
): Boolean {
    return (pos.x in 0 until maze[0].size) && (pos.y in 0 until maze.size) && (maze[pos.y][pos.x] != '#')
}

// Returns the next possible neighbors given the current node and a maze
fun getNextTheseusPositions(
    current: Position,
    maze: Array<CharArray>,
): List<Position> {
    // Theseus can move left, up, right, down, or stay in place
    val moves = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1, 0 to 0)
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
    maze: Array<CharArray>,
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
    maze: Array<CharArray>,
): List<State> {
    val states = mutableListOf<State>()

    // Only add the states where the Minotaur does not catch Theseus
    for (theseusPos in getNextTheseusPositions(state.theseusPos, maze)) {
        val minotaurPos = getNextMinotaurPosition(state.minotaurPos, theseusPos, maze)

        if (minotaurPos == theseusPos) {
            continue
        }

        states.add(State(theseusPos, minotaurPos))
    }

    return states
}

// Returns a pair with the starting state representing the starting positions of Theseus and the Minotaur
// , and then position of the goal, given a maze
fun getStartStateAndGoal(maze: Array<CharArray>): Pair<State?, Position?> {
    var theseusPos: Position? = null
    var minotaurPos: Position? = null
    var goalPos: Position? = null
    var found = false

    for (y in maze.indices) {
        for (x in maze[0].indices) {
            if (maze[y][x] == 'T') {
                theseusPos = Position(x, y)
            } else if (maze[y][x] == 'M') {
                minotaurPos = Position(x, y)
            } else if (maze[y][x] == 'G') {
                goalPos = Position(x, y)
            }

            if (theseusPos != null && minotaurPos != null && goalPos != null) {
                found = true
                break
            }
        }

        if (found == true) break
    }

    val start = State(theseusPos!!, minotaurPos!!)
    return (start to goalPos)
}

// The main A* algorithm. Takes in the maze
fun aStar(maze: Array<CharArray>) {
    val (start, goal) = getStartStateAndGoal(maze)
    // Priority queue to order states based on cost (heuristic + distance from start). Elements are stored in Pair objects, where the first
    // item is the cost, and the second item is the state.
    val queue = PriorityQueue(compareBy<Pair<Int, State>> { it.first })
    // Initialize priority queue with starting state
    queue.add(0 to start!!)
    // Map that tracks previous state for each state so that the route can be reconstructed
    val previousStates = mutableMapOf<State, State>()
    // gScore cost for each Theseus position
    val gScore = mutableMapOf(start!! to 0)

    // Main loop, runs until priority queue is empty (all nodes have been processed)
    while (queue.isNotEmpty()) {
        // Gets current state from front of priority queue
        val (_, current) = queue.poll()

        // If Theseus reaches the goal, reconstruct the path and return
        if (current.theseusPos == goal!!) {
            val path = mutableListOf<State>()
            var curr: State? = current

            while (curr != null) {
                path.add(curr)
                curr = previousStates[curr]
            }

            // Reverse the list to get the correct order, then print the positions
            println("Theseus escaped!")
            path.reversed().forEach {
                println("${it.theseusPos}, ${it.minotaurPos}")
            }

            return
        }

        for (state in getNextStates(current, maze)) {
            // The gScore of moving from the current node to its neighbour is just the score
            // of the current node + 1, since moving from one tile to another has a uniform cost
            // of 1
            val currGScore = gScore[current]!! + 1

            // If Theseus' position is the same, but the Minotaur's isn't, this should be considered as a
            // different state. Thus, we must use the current state as a key in the gScore function.
            // This allows for backtracking, which is often needed
            if (!gScore.containsKey(state) || currGScore < gScore[state]!!) {
                gScore[state] = currGScore
                val fScore = currGScore + getHeuristic(state.theseusPos, goal)
                queue.add(fScore to state)
                previousStates[state] = current
            }
        }
    }

    // If all nodes have been explored (priority queue is emptied), no solution was found
    println("Theseus could not escape...")
}

fun main() {
    val maze1 =
        arrayOf(
            charArrayOf(' ', ' ', 'T', ' ', ' ', '#'),
            charArrayOf(' ', '#', '#', '#', ' ', '#'),
            charArrayOf(' ', ' ', ' ', '#', ' ', 'G'),
            charArrayOf(' ', '#', '#', '#', ' ', '#'),
            charArrayOf(' ', ' ', 'M', ' ', ' ', '#'),
        )

    val maze2 =
        arrayOf(
            charArrayOf(' ', ' ', 'M', ' ', '#'),
            charArrayOf(' ', '#', '#', ' ', '#'),
            charArrayOf(' ', ' ', 'T', ' ', 'G'),
            charArrayOf(' ', '#', ' ', ' ', '#'),
            charArrayOf(' ', '#', ' ', ' ', '#'),
            charArrayOf('#', '#', ' ', ' ', '#'),
            charArrayOf(' ', ' ', ' ', ' ', '#'),
        )

    val maze3 =
        arrayOf(
            charArrayOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', 'G'),
            charArrayOf(' ', '#', ' ', '#', ' ', '#', '#', '#'),
            charArrayOf(' ', 'T', ' ', '#', ' ', 'M', ' ', ' '),
            charArrayOf('#', '#', ' ', '#', ' ', '#', '#', ' '),
            charArrayOf(' ', ' ', ' ', '#', ' ', ' ', ' ', ' '),
            charArrayOf('#', ' ', '#', '#', ' ', '#', ' ', '#'),
            charArrayOf(' ', ' ', '#', ' ', ' ', '#', ' ', ' '),
        )

    aStar(maze1)
    aStar(maze2)
    aStar(maze3)
}

main()
