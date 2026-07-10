# Recursion Patterns Cheat Sheet for DSA


# Choose the Correct Pattern: Identification Guide

## Step 1: What does the problem ask for?

| Problem asks for | Likely pattern |
|---|---|
| Generate all results | Backtracking |
| Generate subsets/subsequences | Choose / not choose |
| Generate combinations | For-loop backtracking with `start` |
| Generate permutations | Permutation recursion |
| Count number of ways | Count recursion, often DP |
| Minimum / maximum / longest / shortest | Min-max recursion, often DP |
| True / false possible | Boolean recursion, often DP |
| Traverse tree | Tree recursion |
| Traverse graph/grid | DFS recursion |
| Split array/string/range | Divide and conquer or interval DP |

---

## Step 2: What is the choice at each step?

| Choice type | Pattern |
|---|---|
| Pick or skip current element | Choose / not choose |
| Pick one from remaining candidates | For-loop backtracking |
| Pick any unused element | Permutation recursion |
| Move in directions | Grid DFS |
| Visit neighbors | Graph DFS |
| Split at position `k` | Interval DP |
| Take min/max over options | DP recursion |
| Use same item again | Unbounded knapsack |

---

## Step 3: Does order matter?

### Order does not matter

Use combinations/subsets.

```text
start index moves forward
```

Example:

```text
[1, 2] same as [2, 1]
```

Use:

```text
backtrack(i + 1)
```

### Order matters

Use permutations.

```text
use visited array
```

Example:

```text
[1, 2] different from [2, 1]
```

Use:

```text
for i from 0 to n - 1:
    if not used[i]:
        choose arr[i]
```

---

# Choose / Not Choose vs For-Loop Backtracking

Both can generate subsets, but they feel different.

## Choose / not choose

Use when each element has exactly two decisions:

```text
take arr[i]
skip arr[i]
```

Template:

```text
function solve(i):
    choose arr[i]
    solve(i + 1)

    skip arr[i]
    solve(i + 1)
```

Good for:

```text
subsequence
subset sum
0/1 knapsack
partition
```

## For-loop backtracking

Use when you are choosing the next element from a list of candidates.

Template:

```text
function solve(start):
    for i from start to n - 1:
        choose arr[i]
        solve(i + 1)
        undo arr[i]
```

Good for:

```text
combinations
combination sum
subsets with duplicates
choose k elements
```

---

# Backtracking vs DP

This is the most important distinction.

## Use backtracking when you need all actual solutions

Example:

```text
Print all subsets.
Return all valid N-Queens boards.
Generate all permutations.
Find all paths in a maze.
```

You usually do not memoize because each path matters.

## Use DP when you only need count, min, max, or possible

Example:

```text
Count subsets with sum K.
Find minimum coins.
Find maximum profit.
Check if subset sum is possible.
Find longest common subsequence length.
```

You should memoize if states repeat.

---

# How to Know If Memoization Is Needed

Ask:

```text
Can the same state be reached in multiple ways?
```

Example:

```text
ways(5)
= ways(4) + ways(3)

ways(4)
= ways(3) + ways(2)
```

`ways(3)` is repeated, so use memoization.

### Memoization template

```text
function solve(state):
    if base_case:
        return base_answer

    if state in memo:
        return memo[state]

    answer = compute_using_recursion

    memo[state] = answer

    return answer
```

### State must include everything that affects the answer

For example:

```text
dp(i, target)
dp(i, capacity)
dp(row, col)
dp(left, right)
dp(mask, last)
dp(index, previousIndex)
```

Do not include temporary `path` in DP state unless it actually affects the answer.

---

# Quick Pattern Table

| Problem type | Pattern to choose |
|---|---|
| Print all subsequences | Choose / not choose |
| Count subsequences with sum K | Choose / not choose + DP |
| Generate subsets | Choose / not choose or for-loop |
| Generate unique subsets | Sort + for-loop backtracking |
| Generate combinations | For-loop backtracking |
| Generate permutations | Used array or swap recursion |
| Combination sum reuse allowed | For-loop with same index `i` |
| Combination sum reuse not allowed | For-loop with `i + 1` |
| 0/1 knapsack | Choose / not choose + DP |
| Coin change unlimited | Unbounded knapsack DP |
| Maze all paths | Grid backtracking |
| Number of islands | Grid DFS |
| Unique paths count | Grid DP |
| LCS / edit distance | 2D DP recursion |
| Matrix chain multiplication | Interval DP |
| Burst balloons | Interval DP |
| Visit all nodes minimum cost | Bitmask DP |
| Binary tree height | Tree recursion |
| Tree max path sum | Tree recursion / tree DP |
| Graph connected components | Graph DFS |

---

# A Simple Decision Flow

Use this mental flow:

```text
1. Is the input a tree?
      Yes -> Tree recursion / Tree DP

2. Is the input a graph or grid?
      Yes -> DFS / BFS / grid recursion
      If generating all paths -> backtracking
      If counting/min/max paths -> DP

3. Are you generating all possibilities?
      Yes -> Backtracking

4. Does each item have take/skip choice?
      Yes -> Choose / not choose

5. Are you choosing from many candidates?
      Yes -> For-loop backtracking

6. Does order matter?
      Yes -> Permutation recursion

7. Are you asked for count/min/max/possible?
      Yes -> Recursion returning value

8. Do states repeat?
      Yes -> Add memoization, now it is DP recursion

9. Is the state a range [l, r]?
      Yes -> Interval DP

10. Do you need to track used elements and n is small?
      Yes -> Bitmask DP
```

---

# The Most Useful Master Templates

## Generate all answers

```text
function backtrack(state, path):
    if complete:
        result.add(copy(path))
        return

    for choice in choices:
        if invalid(choice):
            continue

        path.add(choice)
        backtrack(next_state, path)
        path.removeLast()
```

## Count answers

```text
function count(state):
    if success:
        return 1

    if failure:
        return 0

    total = 0

    for choice in choices:
        total += count(next_state)

    return total
```

## Minimize answer

```text
function minAnswer(state):
    if success:
        return 0

    if failure:
        return INF

    answer = INF

    for choice in choices:
        answer = min(answer, cost(choice) + minAnswer(next_state))

    return answer
```

## Maximize answer

```text
function maxAnswer(state):
    if finished:
        return 0

    answer = -INF

    for choice in choices:
        answer = max(answer, gain(choice) + maxAnswer(next_state))

    return answer
```

## Boolean possible

```text
function possible(state):
    if success:
        return true

    if failure:
        return false

    for choice in choices:
        if possible(next_state):
            return true

    return false
```

## DP recursion

```text
function dp(state):
    if base_case:
        return base_answer

    if memo[state] exists:
        return memo[state]

    answer = compute_from_choices

    memo[state] = answer

    return answer
```

---

# Final Rule of Thumb

Think like this:

```text
Generate all solutions? 
    -> Backtracking

Each element has take/skip?
    -> Choose / not choose

Choose next item from candidates?
    -> For-loop backtracking

Order matters?
    -> Permutations

Need count/min/max/possible?
    -> Recursive DP style

Same state repeats?
    -> Add memoization

Input is tree?
    -> Tree recursion

Input is graph/grid?
    -> DFS/backtracking/DP depending on task

State is a range?
    -> Interval DP

Need track used subset?
    -> Bitmask DP
```

The key skill is not memorizing templates. It is learning to define the state:

```text
What information do I need to uniquely describe the current subproblem?
```

Once the state is clear, the recursion pattern usually becomes obvious.

----

Almost every recursive problem can be broken into these 6 things:

```text
1. State        -> What information defines the current problem?
2. Base case    -> When should recursion stop?
3. Choices      -> What options do I have from this state?
4. Transition   -> How does each choice move to the next state?
5. Answer       -> Am I returning something or collecting results?
6. Optimization -> Do I need undo/backtracking or memoization?
```

The universal recursion template is:

```text
function solve(state):
    if base_case:
        return answer_for_base_case

    answer = initial_value

    for choice in possible_choices:
        apply choice

        sub_answer = solve(next_state)

        combine answer with sub_answer

        undo choice if needed

    return answer
```

---

## 1. Linear Recursion

Use this when you process one item at a time.

Common problems:

```text
sum of array
print array
reverse array
check sorted
find max/min
```

### Pseudocode

```text
function solve(i):
    if i == n:
        return

    process arr[i]

    solve(i + 1)
```

For returning an answer:

```text
function sum(i):
    if i == n:
        return 0

    return arr[i] + sum(i + 1)
```

### How to identify

Use this when the problem says:

```text
process all elements
check all elements
compute something over the array
```

and each recursive call simply moves from `i` to `i + 1`.

---

## 2. Choose / Not Choose Recursion

This is one of the most important DSA recursion patterns.

Use it when every element has two options:

```text
choose it
do not choose it
```

Common problems:

```text
subsets
subsequences
0/1 knapsack
target sum
partition problems
pick or skip problems
```

### Basic subset pseudocode

```text
function generate(i, path):
    if i == n:
        result.add(copy(path))
        return

    // choose arr[i]
    path.add(arr[i])
    generate(i + 1, path)
    path.removeLast()

    // do not choose arr[i]
    generate(i + 1, path)
```

### Count subsequences with target sum

```text
function count(i, target):
    if i == n:
        if target == 0:
            return 1
        else:
            return 0

    choose = count(i + 1, target - arr[i])

    notChoose = count(i + 1, target)

    return choose + notChoose
```

### 0/1 knapsack

```text
function knapsack(i, capacity):
    if i == n:
        return 0

    notTake = knapsack(i + 1, capacity)

    take = 0
    if weight[i] <= capacity:
        take = value[i] + knapsack(i + 1, capacity - weight[i])

    return max(take, notTake)
```

### How to identify

Use choose / not choose when:

```text
Each item can be used at most once.
Each item has two options: take or skip.
Order does not matter.
You are forming a subset or subsequence.
```

Typical keywords:

```text
subsequence
subset
pick some elements
target sum
partition
0/1
include or exclude
```

---

## 3. For-Loop Backtracking Pattern

This is used when, instead of only two choices, you can choose from many possible next elements.

Common problems:

```text
combinations
combination sum
subsets with duplicates
choose k numbers
generate all groups
```

### Basic combination pseudocode

```text
function backtrack(start, path):
    result.add(copy(path))

    for i from start to n - 1:
        path.add(arr[i])

        backtrack(i + 1, path)

        path.removeLast()
```

Here, `start` prevents going backward, so you do not create duplicate orderings.

For example:

```text
[1, 2] and [2, 1]
```

are treated as the same combination.

### Combination of size k

```text
function combine(start, path):
    if length(path) == k:
        result.add(copy(path))
        return

    for i from start to n - 1:
        path.add(arr[i])
        combine(i + 1, path)
        path.removeLast()
```

### Combination Sum, reuse allowed

```text
function combinationSum(start, target, path):
    if target == 0:
        result.add(copy(path))
        return

    if target < 0:
        return

    for i from start to n - 1:
        path.add(arr[i])

        // i, not i + 1, because reuse is allowed
        combinationSum(i, target - arr[i], path)

        path.removeLast()
```

### Combination Sum, reuse not allowed

```text
function combinationSum(start, target, path):
    if target == 0:
        result.add(copy(path))
        return

    if target < 0:
        return

    for i from start to n - 1:
        path.add(arr[i])

        // i + 1 because reuse is not allowed
        combinationSum(i + 1, target - arr[i], path)

        path.removeLast()
```

### Handling duplicates

Sort first, then skip duplicates at the same recursion level.

```text
sort(arr)

function backtrack(start, path):
    result.add(copy(path))

    for i from start to n - 1:
        if i > start and arr[i] == arr[i - 1]:
            continue

        path.add(arr[i])
        backtrack(i + 1, path)
        path.removeLast()
```

### How to identify

Use for-loop backtracking when:

```text
At each step, you can choose from many candidates.
You are generating combinations.
You need a start index.
Order should not matter.
You need to avoid duplicates.
```

Typical keywords:

```text
combinations
choose k
all possible groups
combination sum
unique subsets
```

---

## 4. Permutation Recursion

Use this when order matters.

Example:

```text
[1, 2] and [2, 1]
```

are different permutations.

Common problems:

```text
permutations
arrangements
anagrams
all orderings
```

### Permutation using visited array

```text
function permute(path, used):
    if length(path) == n:
        result.add(copy(path))
        return

    for i from 0 to n - 1:
        if used[i] == true:
            continue

        used[i] = true
        path.add(arr[i])

        permute(path, used)

        path.removeLast()
        used[i] = false
```

### Unique permutations with duplicates

```text
sort(arr)

function permute(path, used):
    if length(path) == n:
        result.add(copy(path))
        return

    for i from 0 to n - 1:
        if used[i] == true:
            continue

        if i > 0 and arr[i] == arr[i - 1] and used[i - 1] == false:
            continue

        used[i] = true
        path.add(arr[i])

        permute(path, used)

        path.removeLast()
        used[i] = false
```

### Permutation using swapping

```text
function permute(index):
    if index == n:
        result.add(copy(arr))
        return

    for i from index to n - 1:
        swap(arr[index], arr[i])

        permute(index + 1)

        swap(arr[index], arr[i])
```

### How to identify

Use permutation recursion when:

```text
Order matters.
You need all arrangements.
You can place any unused element at the current position.
```

Typical keywords:

```text
permutation
arrangement
rearrange
all possible orders
anagram
```

---

## 5. General Backtracking Pattern

Backtracking means:

```text
choose
explore
undo
```

It is used when you are building a solution step by step and may need to undo choices.

Common problems:

```text
N-Queens
Sudoku
Rat in a maze
Word Search
M-coloring
Hamiltonian path
generate valid parentheses
```

### Generic backtracking pseudocode

```text
function backtrack(state):
    if solution_is_complete(state):
        result.add(copy(state))
        return

    for choice in possible_choices(state):
        if choice_is_not_valid:
            continue

        apply(choice)

        backtrack(new_state)

        undo(choice)
```

### Example: Generate valid parentheses

```text
function generate(open, close, path):
    if length(path) == 2 * n:
        result.add(path)
        return

    if open < n:
        generate(open + 1, close, path + "(")

    if close < open:
        generate(open, close + 1, path + ")")
```

### How to identify

Use backtracking when:

```text
You are asked to generate all valid solutions.
There are constraints.
You need to try choices and undo them.
A wrong choice should be abandoned early.
```

Typical keywords:

```text
all valid
place
arrange
solve board
maze
path
constraint
```

---

## 6. Grid / Matrix DFS Recursion

Use this when recursion moves in directions.

Common problems:

```text
number of islands
flood fill
word search
rat in a maze
unique paths with obstacles
connected components in grid
```

### Basic grid DFS

```text
function dfs(r, c):
    if r < 0 or c < 0 or r >= rows or c >= cols:
        return

    if grid[r][c] is blocked:
        return

    if visited[r][c] == true:
        return

    visited[r][c] = true

    dfs(r + 1, c)
    dfs(r - 1, c)
    dfs(r, c + 1)
    dfs(r, c - 1)
```

### Grid backtracking for all paths

```text
function dfs(r, c, path):
    if outside_grid(r, c):
        return

    if blocked(r, c) or visited[r][c]:
        return

    if r == targetRow and c == targetCol:
        result.add(copy(path))
        return

    visited[r][c] = true

    for each direction in directions:
        path.add(direction)
        dfs(newRow, newCol, path)
        path.removeLast()

    visited[r][c] = false
```

### Important difference

For normal DFS, such as counting islands:

```text
mark visited and do not unmark
```

For backtracking paths:

```text
mark visited, explore, then unmark
```

### How to identify

Use grid DFS when:

```text
You move up, down, left, right, or diagonally.
You need to explore connected cells.
The input is a matrix.
```

Typical keywords:

```text
grid
matrix
island
maze
path
connected cells
word search
```

---

## 7. Tree Recursion

Trees are naturally recursive because every subtree is a smaller tree.

Common problems:

```text
tree height
diameter
balanced tree
path sum
lowest common ancestor
serialize tree
tree DP
```

### Basic tree recursion

```text
function dfs(node):
    if node == null:
        return base_value

    leftAnswer = dfs(node.left)
    rightAnswer = dfs(node.right)

    return combine(node, leftAnswer, rightAnswer)
```

### Example: height of binary tree

```text
function height(node):
    if node == null:
        return 0

    leftHeight = height(node.left)
    rightHeight = height(node.right)

    return 1 + max(leftHeight, rightHeight)
```

### Example: path sum

```text
function hasPathSum(node, target):
    if node == null:
        return false

    if node.left == null and node.right == null:
        return target == node.value

    remaining = target - node.value

    return hasPathSum(node.left, remaining) 
           or hasPathSum(node.right, remaining)
```

### How to identify

Use tree recursion when:

```text
The input is a tree.
The answer for a node depends on its children.
The same logic applies to left and right subtree.
```

Typical keywords:

```text
binary tree
subtree
root
leaf
ancestor
height
diameter
path in tree
```

---

## 8. Graph DFS Recursion

Use graph recursion when nodes are connected by edges.

Common problems:

```text
connected components
cycle detection
topological sort
DFS traversal
clone graph
all paths in graph
```

### Basic DFS

```text
function dfs(node):
    visited[node] = true

    for neighbor in graph[node]:
        if visited[neighbor] == false:
            dfs(neighbor)
```

### All paths in graph

```text
function dfs(node, path):
    if node == target:
        result.add(copy(path))
        return

    visited[node] = true

    for neighbor in graph[node]:
        if visited[neighbor] == false:
            path.add(neighbor)
            dfs(neighbor, path)
            path.removeLast()

    visited[node] = false
```

### Important difference

For normal traversal:

```text
visited[node] = true
do not undo
```

For finding all paths:

```text
visited[node] = true
explore
visited[node] = false
```

### How to identify

Use graph DFS when:

```text
The input has nodes and edges.
You need to explore reachability.
You need components, paths, cycles, or traversal.
```

Typical keywords:

```text
graph
nodes
edges
connected
cycle
path
component
reachable
```

---

## 9. Divide and Conquer Recursion

Use this when the problem can be split into independent smaller parts.

Common problems:

```text
merge sort
quick sort
binary search
maximum subarray divide and conquer
segment tree
fast exponentiation
```

### Generic divide and conquer

```text
function solve(left, right):
    if left == right:
        return base_answer

    mid = (left + right) / 2

    leftAnswer = solve(left, mid)
    rightAnswer = solve(mid + 1, right)

    return merge(leftAnswer, rightAnswer)
```

### Binary search recursion

```text
function binarySearch(left, right, target):
    if left > right:
        return -1

    mid = (left + right) / 2

    if arr[mid] == target:
        return mid

    if target < arr[mid]:
        return binarySearch(left, mid - 1, target)
    else:
        return binarySearch(mid + 1, right, target)
```

### Fast power recursion

```text
function power(x, n):
    if n == 0:
        return 1

    half = power(x, n / 2)

    if n is even:
        return half * half
    else:
        return x * half * half
```

### How to identify

Use divide and conquer when:

```text
The problem can be split into halves or ranges.
The answer comes from merging smaller answers.
The subproblems are mostly independent.
```

Typical keywords:

```text
sorted array
range
split
merge
divide
left half
right half
```

---

## 10. Boolean Recursion / Early Stopping

Use this when you only need to know whether a solution exists.

Common problems:

```text
does a path exist
can target be formed
can board be solved
does subset exist
```

### Pseudocode

```text
function exists(state):
    if goal_reached:
        return true

    if invalid_state:
        return false

    for choice in possible_choices:
        apply(choice)

        if exists(next_state) == true:
            return true

        undo(choice)

    return false
```

### How to identify

Use this when the problem asks:

```text
return true or false
find any valid solution
determine if possible
```

Typical keywords:

```text
exists
possible
can we
is there a way
valid or invalid
```

---

## 11. Count Recursion

Use this when you need the number of ways.

Common problems:

```text
count paths
count subsets
count ways to climb stairs
count coin change combinations
count partitions
```

### Generic count recursion

```text
function count(state):
    if success:
        return 1

    if failure:
        return 0

    total = 0

    for choice in possible_choices:
        total += count(next_state)

    return total
```

### Example: climbing stairs

```text
function ways(n):
    if n == 0:
        return 1

    if n < 0:
        return 0

    return ways(n - 1) + ways(n - 2)
```

### How to identify

Use count recursion when:

```text
The problem asks how many ways.
Every valid complete path contributes 1.
You need to add answers from choices.
```

Typical keywords:

```text
count
number of ways
total ways
how many
```

---

## 12. Min / Max Recursion

Use this when you need the best possible answer.

Common problems:

```text
minimum cost
maximum profit
longest subsequence
shortest path in special state-space
minimum coins
maximum points
```

### Generic min recursion

```text
function minCost(state):
    if goal_reached:
        return 0

    if invalid_state:
        return INF

    answer = INF

    for choice in possible_choices:
        cost = cost_of_choice + minCost(next_state)
        answer = min(answer, cost)

    return answer
```

### Generic max recursion

```text
function maxProfit(state):
    if finished:
        return 0

    answer = -INF

    for choice in possible_choices:
        value = gain_from_choice + maxProfit(next_state)
        answer = max(answer, value)

    return answer
```

### How to identify

Use min/max recursion when:

```text
The problem asks for best, minimum, maximum, longest, shortest, or optimal.
```

Typical keywords:

```text
minimum
maximum
largest
smallest
longest
shortest
optimal
best
```

---

## 13. DP-Based Recursion / Memoization

DP-based recursion is also called top-down DP.

It is recursion plus memoization.

Use it when the same state repeats multiple times.

### Generic memoized recursion

```text
memo = map

function dp(state):
    if base_case:
        return base_answer

    if state exists in memo:
        return memo[state]

    answer = initial_value

    for choice in possible_choices:
        answer = combine(answer, dp(next_state))

    memo[state] = answer

    return answer
```

### How to identify DP recursion

Use DP when:

```text
You are returning count, min, max, or true/false.
The same recursive state appears again and again.
The constraints are too large for brute force.
The problem has overlapping subproblems.
```

Typical keywords:

```text
number of ways
minimum
maximum
longest
shortest
can we
optimal
count
```

---

## 14. 1D DP Recursion

Use when the state depends on one variable.

Common problems:

```text
climbing stairs
frog jump
house robber
minimum cost climbing stairs
decode ways
```

### Pseudocode

```text
function dp(i):
    if i reaches base:
        return base_answer

    if memo[i] exists:
        return memo[i]

    answer = combine(
        dp(next_state_1),
        dp(next_state_2),
        ...
    )

    memo[i] = answer
    return answer
```

### Example: climbing stairs

```text
function ways(n):
    if n == 0:
        return 1

    if n < 0:
        return 0

    if memo[n] exists:
        return memo[n]

    memo[n] = ways(n - 1) + ways(n - 2)

    return memo[n]
```

### How to identify

Use 1D DP when:

```text
Only one changing variable matters.
Usually index, position, day, step, or amount.
```

State examples:

```text
dp(i)
dp(amount)
dp(day)
dp(index)
```

---

## 15. 2D DP Recursion

Use when two variables define the state.

Common problems:

```text
LCS
edit distance
grid paths
0/1 knapsack
subset sum
coin change
minimum path sum
```

### Pseudocode

```text
function dp(i, j):
    if base_case:
        return base_answer

    if memo[i][j] exists:
        return memo[i][j]

    answer = combine(
        dp(next_i_1, next_j_1),
        dp(next_i_2, next_j_2)
    )

    memo[i][j] = answer
    return answer
```

### Example: LCS

```text
function lcs(i, j):
    if i == length(s1) or j == length(s2):
        return 0

    if memo[i][j] exists:
        return memo[i][j]

    if s1[i] == s2[j]:
        memo[i][j] = 1 + lcs(i + 1, j + 1)
    else:
        memo[i][j] = max(
            lcs(i + 1, j),
            lcs(i, j + 1)
        )

    return memo[i][j]
```

### How to identify

Use 2D DP when:

```text
Two indices are moving.
Two strings/arrays are involved.
One index and one target/capacity are involved.
```

State examples:

```text
dp(i, j)
dp(index, target)
dp(index, capacity)
dp(row, col)
```

---

## 16. Knapsack-Style DP Recursion

Use when each item can be picked or skipped and there is some capacity or target.

Common problems:

```text
0/1 knapsack
subset sum
partition equal subset sum
target sum
minimum subset difference
```

### Pseudocode

```text
function dp(i, capacity):
    if i == n:
        return 0

    if memo[i][capacity] exists:
        return memo[i][capacity]

    notTake = dp(i + 1, capacity)

    take = -INF
    if weight[i] <= capacity:
        take = value[i] + dp(i + 1, capacity - weight[i])

    memo[i][capacity] = max(take, notTake)

    return memo[i][capacity]
```

### For subset sum

```text
function possible(i, target):
    if target == 0:
        return true

    if i == n:
        return false

    if memo[i][target] exists:
        return memo[i][target]

    notTake = possible(i + 1, target)

    take = false
    if arr[i] <= target:
        take = possible(i + 1, target - arr[i])

    memo[i][target] = take or notTake

    return memo[i][target]
```

### How to identify

Use knapsack DP when:

```text
You have items.
Each item can be taken or skipped.
There is a capacity, sum, or target.
```

Typical keywords:

```text
subset sum
target
capacity
partition
0/1
pick or not pick
```

---

## 17. Unbounded Knapsack / Coin Change Recursion

Use when items can be reused multiple times.

Common problems:

```text
coin change
combination sum
minimum coins
rod cutting
unbounded knapsack
```

### Count ways

```text
function count(i, target):
    if target == 0:
        return 1

    if i == n:
        return 0

    if memo[i][target] exists:
        return memo[i][target]

    notTake = count(i + 1, target)

    take = 0
    if coin[i] <= target:
        // i stays same because coin can be reused
        take = count(i, target - coin[i])

    memo[i][target] = take + notTake

    return memo[i][target]
```

### Minimum coins

```text
function minCoins(i, target):
    if target == 0:
        return 0

    if i == n:
        return INF

    if memo[i][target] exists:
        return memo[i][target]

    notTake = minCoins(i + 1, target)

    take = INF
    if coin[i] <= target:
        take = 1 + minCoins(i, target - coin[i])

    memo[i][target] = min(take, notTake)

    return memo[i][target]
```

### How to identify

Use unbounded knapsack when:

```text
You can use the same item multiple times.
There is usually a target or capacity.
```

Typical keywords:

```text
infinite supply
unlimited coins
reuse allowed
same item multiple times
```

---

## 18. Grid DP Recursion

Use when the answer depends on nearby cells.

Common problems:

```text
unique paths
minimum path sum
cherry pickup
falling path sum
dungeon game
```

### Count paths

```text
function dp(r, c):
    if r >= rows or c >= cols:
        return 0

    if grid[r][c] is blocked:
        return 0

    if r == rows - 1 and c == cols - 1:
        return 1

    if memo[r][c] exists:
        return memo[r][c]

    down = dp(r + 1, c)
    right = dp(r, c + 1)

    memo[r][c] = down + right

    return memo[r][c]
```

### Minimum path sum

```text
function dp(r, c):
    if r >= rows or c >= cols:
        return INF

    if r == rows - 1 and c == cols - 1:
        return grid[r][c]

    if memo[r][c] exists:
        return memo[r][c]

    memo[r][c] = grid[r][c] + min(
        dp(r + 1, c),
        dp(r, c + 1)
    )

    return memo[r][c]
```

### How to identify

Use grid DP when:

```text
You are counting paths or finding min/max path cost.
Movement is restricted, usually right/down/up/down.
Same cell state is reached repeatedly.
```

Important distinction:

```text
Generate all paths -> backtracking
Count/min/max paths -> DP
```

---

## 19. Interval DP Recursion

Use when the problem is about a range `[left, right]`.

Common problems:

```text
matrix chain multiplication
burst balloons
palindrome partitioning
minimum cost to cut stick
strange printer
optimal BST
```

### Generic interval DP

```text
function dp(left, right):
    if left > right:
        return base_answer

    if memo[left][right] exists:
        return memo[left][right]

    answer = initial_value

    for k from left to right:
        candidate = cost(left, k, right)
                    + dp(left, k - 1)
                    + dp(k + 1, right)

        answer = best(answer, candidate)

    memo[left][right] = answer

    return answer
```

### Matrix chain style

```text
function dp(left, right):
    if left == right:
        return 0

    if memo[left][right] exists:
        return memo[left][right]

    answer = INF

    for k from left to right - 1:
        cost = dp(left, k)
             + dp(k + 1, right)
             + mergeCost(left, k, right)

        answer = min(answer, cost)

    memo[left][right] = answer

    return answer
```

### How to identify

Use interval DP when:

```text
The problem asks about a substring, subarray, or interval.
You choose a split point k.
The answer for [left, right] depends on smaller intervals.
```

Typical keywords:

```text
substring
subarray
range
partition
burst
cut
merge
palindrome
```

---

## 20. Bitmask DP Recursion

Use when you need to track a subset of used items.

Common problems:

```text
traveling salesman problem
assignment problem
minimum cost matching
visit all nodes
Hamiltonian path
```

### Pseudocode

```text
function dp(mask, last):
    if mask == allVisited:
        return base_answer

    if memo[mask][last] exists:
        return memo[mask][last]

    answer = INF

    for next from 0 to n - 1:
        if mask does not contain next:
            newMask = mask with next added

            candidate = cost[last][next] + dp(newMask, next)

            answer = min(answer, candidate)

    memo[mask][last] = answer

    return answer
```

### How to identify

Use bitmask DP when:

```text
n is small, usually <= 20.
You need to know which elements have already been used.
Order or assignment matters.
```

Typical keywords:

```text
visit all
assign
used elements
minimum cost arrangement
small n
subset state
```

---

## 21. Tree DP Recursion

Use when the answer for a tree node depends on answers from its children.

Common problems:

```text
house robber on tree
maximum path sum
diameter of tree
maximum independent set
tree matching
```

### Generic tree DP

```text
function dfs(node):
    if node == null:
        return base_answer

    answersFromChildren = []

    for child in node.children:
        answersFromChildren.add(dfs(child))

    return combine(node, answersFromChildren)
```

### Example: House Robber on Tree

For each node, return two values:

```text
takeNode
skipNode
```

```text
function dfs(node):
    if node == null:
        return (0, 0)

    leftTake, leftSkip = dfs(node.left)
    rightTake, rightSkip = dfs(node.right)

    takeNode = node.value + leftSkip + rightSkip

    skipNode = max(leftTake, leftSkip) 
             + max(rightTake, rightSkip)

    return (takeNode, skipNode)
```

Final answer:

```text
takeRoot, skipRoot = dfs(root)
answer = max(takeRoot, skipRoot)
```

### How to identify

Use tree DP when:

```text
Input is a tree.
For every node, you need multiple possible states.
Parent choice affects child choices.
```

Typical keywords:

```text
tree
choose nodes
rob tree
include/exclude node
maximum path
subtree answer
```

---

## 22. Digit DP Recursion

This is an advanced DP recursion pattern.

Use it when counting numbers within a range with digit constraints.

Common problems:

```text
count numbers <= N
numbers with no repeated digits
count numbers with digit sum K
count beautiful numbers
```

### Pseudocode

```text
digits = digits of N

function dp(pos, tight, started, extraState):
    if pos == length(digits):
        if valid(extraState):
            return 1
        else:
            return 0

    if memo[pos][tight][started][extraState] exists:
        return memo value

    limit = digits[pos] if tight == true else 9

    answer = 0

    for digit from 0 to limit:
        newTight = tight and digit == limit
        newStarted = started or digit != 0
        newExtraState = update(extraState, digit)

        answer += dp(pos + 1, newTight, newStarted, newExtraState)

    memo[state] = answer
    return answer
```

### How to identify

Use digit DP when:

```text
The problem asks to count numbers in a range.
There are constraints on digits.
N can be very large.
```

Typical keywords:

```text
count numbers from L to R
digits
digit sum
no repeated digits
number <= N
```

---

