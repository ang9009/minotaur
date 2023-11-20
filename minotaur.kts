import java.util.PriorityQueue

// Classes representing a position and the state of the game
data class Position(val x: Int, val y: Int)
data class State(val theseusPos: Position, val minotaurPos: Position)

// Calculates Manhattan distance heuristic given a position and the exit
fun getHScore(
    pos: Position,
    exit: Position,
): Int {
    return Math.abs(pos.x - exit.x) + Math.abs(pos.y - exit.y)
}

// Checks if a given position is valid: if it is within the maze, and if it is a non-wall
fun isPositionValid(
    pos: Position,
    maze: Array<CharArray>,
): Boolean {
    return (pos.x in 0 until maze[0].size) && (pos.y in 0 until maze.size) && (maze[pos.y][pos.x] != '#')
}

// Returns the next possible Theseus positions given his current position and a maze
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

// Return change in x or y based on the Minotaur's and Theseus' horizontal/vertical coordinates
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

// Returns a pair with the starting state representing the starting positions of Theseus and the Minotaur,
// and theposition of the exit, given a maze
fun getStartStateAndGoal(maze: Array<CharArray>): Pair<State?, Position?> {
    var theseusPos: Position? = null
    var minotaurPos: Position? = null
    var exitPos: Position? = null
    var found = false

    for (y in maze.indices) {
        for (x in maze[0].indices) {
            if (maze[y][x] == 'T') {
                theseusPos = Position(x, y)
            } else if (maze[y][x] == 'M') {
                minotaurPos = Position(x, y)
            } else if (maze[y][x] == 'E') {
                exitPos = Position(x, y)
            }

            if (theseusPos != null && minotaurPos != null && exitPos != null) {
                found = true
                break
            }
        }

        if (found == true) break
    }

    val start = State(theseusPos!!, minotaurPos!!)
    return (start to exitPos)
}

// The main A* algorithm. Takes in the maze
fun solver(maze: Array<CharArray>) {
    val (start, exit) = getStartStateAndGoal(maze)
    // Priority queue to order states based on cost (heuristic + distance from start). Elements are stored in Pairs (tuples)
    // where the first item is the cost, and the second item is the state.
    val queue = PriorityQueue(compareBy<Pair<Int, State>> { it.first })
    // Initialize priority queue with starting state
    queue.add(0 to start!!)
    // Map that tracks previous state for each state so that the route can be reconstructed
    val previousStates = mutableMapOf<State, State>()
    // Stores gScore cost for each State, where States are used as keys to access the corresponding gScore. This allows for
    // backtracking, as a State where Theseus is in the same position but the Minotaur is not is considered distinct.
    // Backtracking also results in a higher gScore, so this should be stored as well.
    val gScore = mutableMapOf<State, Int>(start!! to 0)

    // Main loop, runs until priority queue is empty (all nodes have been processed)
    while (queue.isNotEmpty()) {
        // Gets current state from front of priority queue
        val (_, current) = queue.poll()

        // If Theseus reaches the exit, reconstruct the path and return
        if (current.theseusPos == exit!!) {
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
            if (!gScore.containsKey(state) || currGScore < gScore[state]!!) {
                gScore[state] = currGScore
                val fScore = currGScore + getHScore(state.theseusPos, exit)
                queue.add(fScore to state)
                previousStates[state] = current
            }
        }
    }

    // If all nodes have been explored (priority queue is emptied), no solution was found
    println("Theseus could not escape...")
}

fun main() {
    // Based on level 1
    val maze1 =
        arrayOf(
            charArrayOf('#', '#', '#', '#', '#', '#', '#'),
            charArrayOf('#', ' ', ' ', 'T', ' ', ' ', '#'),
            charArrayOf('#', ' ', '#', '#', '#', ' ', '#'),
            charArrayOf('#', ' ', ' ', ' ', '#', ' ', 'E'),
            charArrayOf('#', ' ', '#', '#', '#', ' ', '#'),
            charArrayOf('#', ' ', ' ', 'M', ' ', ' ', '#'),
            charArrayOf('#', '#', '#', '#', '#', '#', '#'),
        )

    // Based on level 2
    val maze2 =
        arrayOf(
            charArrayOf('#', '#', '#', '#', '#', '#'),
            charArrayOf('#', ' ', ' ', 'M', ' ', '#'),
            charArrayOf('#', ' ', '#', '#', ' ', '#'),
            charArrayOf('#', ' ', ' ', 'T', ' ', 'E'),
            charArrayOf('#', ' ', '#', ' ', ' ', '#'),
            charArrayOf('#', ' ', '#', ' ', ' ', '#'),
            charArrayOf('#', '#', '#', ' ', ' ', '#'),
            charArrayOf('#', ' ', ' ', ' ', ' ', '#'),
            charArrayOf('#', '#', '#', '#', '#', '#'),
        )

    // Based on level 6
    val maze3 =
        arrayOf(
            charArrayOf('#', '#', '#', '#', '#', '#', '#', '#'),
            charArrayOf('#', ' ', ' ', ' ', 'T', ' ', ' ', '#'),
            charArrayOf('#', ' ', ' ', ' ', '#', '#', ' ', '#'),
            charArrayOf('#', ' ', ' ', ' ', ' ', '#', ' ', '#'),
            charArrayOf('#', ' ', ' ', ' ', '#', '#', ' ', '#'),
            charArrayOf('#', ' ', '#', ' ', ' ', '#', ' ', '#'),
            charArrayOf('#', ' ', '#', '#', ' ', ' ', ' ', '#'),
            charArrayOf('#', ' ', '#', 'E', 'M', ' ', ' ', '#'),
            charArrayOf('#', '#', '#', '#', '#', '#', '#', '#'),
        )

    // An unsolvable maze where the exit is blocked
        val exitBlocked =
            arrayOf(
                charArrayOf('#', '#', '#', '#', '#', '#'),
                charArrayOf('#', ' ', 'T', ' ', ' ', '#'),
                charArrayOf('#', ' ', ' ', ' ', '#', '#'),
                charArrayOf('#', ' ', ' ', ' ', '#', 'E'),
                charArrayOf('#', ' ', ' ', ' ', '#', '#'),
                charArrayOf('#', ' ', 'M', ' ', ' ', '#'),
                charArrayOf('#', '#', '#', '#', '#', '#'),
            )

    // An unsolvable maze where the Minotaur is too close to be avoided
        val minotaurTooClose =
            arrayOf(
                charArrayOf('#', '#', '#', '#', '#', '#'),
                charArrayOf('#', 'T', ' ', ' ', ' ', '#'),
                charArrayOf('#', ' ', ' ', ' ', ' ', '#'),
                charArrayOf('#', ' ', ' ', ' ', ' ', 'E'),
                charArrayOf('#', ' ', ' ', ' ', ' ', '#'),
                charArrayOf('#', 'M', ' ', ' ', ' ', '#'),
                charArrayOf('#', '#', '#', '#', '#', '#'),
            )


    solver(maze1)
    solver(maze2)
    solver(maze3)
    solver(exitBlocked)
    solver(minotaurTooClose)
}

main()
