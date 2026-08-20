# Greedy Algorithm Pattern Cheat Sheet for DSA

Greedy algorithms make the best local choice at each step, hoping it leads to the best global answer.

The key skill is not memorizing problems. The key skill is knowing **when a local best choice is safe**.

---

# What Is Greedy?

A greedy algorithm repeatedly chooses the option that looks best right now.

```text
while problem is not finished:
    choose the best available option
    apply that choice

return answer
```

A greedy solution usually works when the problem has these properties:

```text
1. Greedy choice property
   A locally optimal choice can be part of a globally optimal answer.

2. Optimal substructure
   After making the greedy choice, the remaining problem is still the same type of problem.

3. Irreversibility
   Once a choice is made, you do not need to revisit it.
```

---

# Universal Greedy Template

```text
function greedy(input):
    sort or organize input if needed

    answer = initial value
    state = initial state

    for item in input:
        if item can be safely chosen:
            choose item
            update answer
            update state
        else:
            skip item or fix conflict

    return answer
```

---

# How to Identify Greedy Problems

Greedy is likely when the problem asks for:

```text
minimum number of something
maximum number of something
minimum cost
maximum profit
minimum operations
maximum activities/tasks/events
can we reach the end
choose items under constraints
assign limited resources optimally
```

Common keywords:

```text
minimum
maximum
optimal
at most
at least
non-overlapping
earliest
latest
smallest
largest
fewest
most
schedule
intervals
resources
coins
jumps
profit
```

But keywords alone are not enough. You must check whether a local decision is safe.

---

# Greedy vs DP vs Backtracking

| Situation | Use |
|---|---|
| Need all possible solutions | Backtracking |
| Need count/min/max/possible and choices overlap | DP |
| Local best decision is always safe | Greedy |
| Need to try every combination | Backtracking / DP |
| Need to optimize with repeated states | DP |
| Need to sort and pick based on a rule | Greedy |

---

# The Greedy Identification Checklist

Ask these questions:

```text
1. Can I sort the input to make choices easier?
2. Is there an obvious local best choice?
3. After choosing it, does the remaining problem stay similar?
4. Can I prove that choosing it never hurts?
5. Is there no need to go back and change previous choices?
```

If the answer is mostly yes, try greedy.

If you are unsure, test greedy against small brute force cases.

---

# 1. Sorting-Based Greedy

This is the most common greedy pattern.

Use it when choosing items in a specific order makes the decision easy.

Common sorting rules:

```text
sort by start time
sort by end time
sort by profit
sort by cost
sort by ratio
sort by deadline
sort by length
sort ascending
sort descending
```

### Generic Pseudocode

```text
function greedy(items):
    sort(items, by greedy_rule)

    answer = 0
    state = initial_state

    for item in items:
        if can_take(item, state):
            take item
            answer = update(answer, item)
            state = update_state(state, item)

    return answer
```

### How to identify

Use sorting-based greedy when:

```text
The problem asks to choose the best subset/order.
The input has values like start, end, cost, profit, deadline, weight.
Sorting by one property makes conflicts easier to handle.
```

---

# 2. Activity Selection / Maximum Non-Overlapping Intervals

Use when you need to select the maximum number of non-overlapping intervals.

Greedy rule:

```text
Pick the interval that ends earliest.
```

Why?

```text
The earlier an interval ends, the more room remains for future intervals.
```

### Pseudocode

```text
function maxActivities(intervals):
    sort intervals by end time ascending

    count = 0
    lastEnd = -infinity

    for interval in intervals:
        if interval.start >= lastEnd:
            count += 1
            lastEnd = interval.end

    return count
```

### How to identify

Use this when the problem says:

```text
maximum number of meetings
maximum activities
non-overlapping intervals
select maximum events
one room/person/resource
```

Typical examples:

```text
Activity Selection
Maximum Meetings in One Room
Non-overlapping Intervals
Erase Minimum Overlapping Intervals
```

---

# 3. Minimum Intervals to Remove

This is the opposite of selecting maximum non-overlapping intervals.

Idea:

```text
minimum removals = total intervals - maximum non-overlapping intervals
```

### Pseudocode

```text
function minRemove(intervals):
    sort intervals by end time ascending

    keep = 0
    lastEnd = -infinity

    for interval in intervals:
        if interval.start >= lastEnd:
            keep += 1
            lastEnd = interval.end

    return length(intervals) - keep
```

### How to identify

Use this when the problem says:

```text
remove minimum intervals so that remaining intervals do not overlap
make intervals non-overlapping
```

---

# 4. Merge Intervals Pattern

This is not always called greedy, but it uses a greedy idea after sorting.

Greedy rule:

```text
Sort by start time, then keep extending the current interval while overlap exists.
```

### Pseudocode

```text
function mergeIntervals(intervals):
    sort intervals by start time ascending

    result = empty list

    for interval in intervals:
        if result is empty or interval.start > result.last.end:
            result.add(interval)
        else:
            result.last.end = max(result.last.end, interval.end)

    return result
```

### How to identify

Use this when the problem says:

```text
merge overlapping intervals
combine ranges
insert interval
union of intervals
```

---

# 5. Interval Scheduling With Minimum Rooms

Use when intervals overlap and you need the minimum number of rooms/resources.

Greedy rule:

```text
Reuse the room that becomes free earliest.
```

Usually implemented with a min-heap of end times.

### Pseudocode

```text
function minRooms(intervals):
    sort intervals by start time ascending

    minHeap = empty heap    // stores end times

    for interval in intervals:
        if minHeap is not empty and minHeap.top <= interval.start:
            heapPop(minHeap)     // reuse room

        heapPush(minHeap, interval.end)

    return size(minHeap)
```

### How to identify

Use this when the problem says:

```text
minimum meeting rooms
minimum platforms
minimum resources needed
maximum simultaneous intervals
```

---

# 6. Two-Pointer Greedy

Use when the array is sorted and you choose from both ends.

Common problems:

```text
boats to save people
pairing problems
assign cookies
minimum pair operations
maximize pairs
```

### Generic Pseudocode

```text
function twoPointerGreedy(arr):
    sort(arr)

    left = 0
    right = n - 1
    answer = 0

    while left <= right:
        if arr[left] and arr[right] can be paired:
            left += 1
            right -= 1
        else:
            right -= 1

        answer += 1

    return answer
```

---

## Boats to Save People

Greedy rule:

```text
Put the heaviest remaining person on a boat.
If possible, pair them with the lightest remaining person.
```

### Pseudocode

```text
function numBoats(people, limit):
    sort(people)

    left = 0
    right = n - 1
    boats = 0

    while left <= right:
        if people[left] + people[right] <= limit:
            left += 1

        right -= 1
        boats += 1

    return boats
```

### How to identify

Use this when:

```text
You need to pair small with large.
There is a capacity or limit.
Sorting allows best/worst elements to be handled first.
```

---

# 7. Assign Resources Greedy

Use when you need to assign limited resources to satisfy maximum requests.

Common problems:

```text
assign cookies
assign workers to jobs
maximize satisfied children
minimum arrows to burst balloons
```

Greedy rule:

```text
Give the smallest sufficient resource to the smallest requirement.
```

### Pseudocode

```text
function assign(requirements, resources):
    sort(requirements)
    sort(resources)

    i = 0    // requirement pointer
    j = 0    // resource pointer
    count = 0

    while i < length(requirements) and j < length(resources):
        if resources[j] >= requirements[i]:
            count += 1
            i += 1
            j += 1
        else:
            j += 1

    return count
```

### How to identify

Use this when:

```text
You have people/jobs/tasks with requirements.
You have resources with capacities.
You want to maximize assignments.
Each resource can be used once.
```

---

# 8. Fractional Knapsack

Use when items can be broken into fractions.

Greedy rule:

```text
Pick the item with highest value / weight ratio first.
```

### Pseudocode

```text
function fractionalKnapsack(items, capacity):
    for item in items:
        item.ratio = item.value / item.weight

    sort items by ratio descending

    totalValue = 0

    for item in items:
        if capacity == 0:
            break

        if item.weight <= capacity:
            totalValue += item.value
            capacity -= item.weight
        else:
            fraction = capacity / item.weight
            totalValue += item.value * fraction
            capacity = 0

    return totalValue
```

### How to identify

Use this when:

```text
Items can be divided.
You can take a fraction of an item.
Need maximum value under capacity.
```

Important:

```text
0/1 Knapsack is not greedy.
0/1 Knapsack usually needs DP.
```

---

# 9. Coin Change Greedy

Use only when the coin system supports greedy choices.

For standard coin systems like Indian or US currency, choosing the largest coin first usually works.

But for arbitrary coin systems, greedy can fail.

Example where greedy fails:

```text
coins = [1, 3, 4]
target = 6

Greedy: 4 + 1 + 1 = 3 coins
Optimal: 3 + 3 = 2 coins
```

### Pseudocode

```text
function coinChangeGreedy(coins, amount):
    sort coins descending

    count = 0

    for coin in coins:
        while amount >= coin:
            amount -= coin
            count += 1

    if amount == 0:
        return count
    else:
        return impossible
```

### How to identify

Use greedy only when:

```text
The coin system is canonical or standard.
The problem specifically allows greedy.
```

Use DP when:

```text
Coin values are arbitrary.
You need the minimum number of coins for any input.
```

---

# 10. Jump Game Greedy

Use when you need to know if you can reach the end or minimum jumps.

---

## Jump Game I: Can Reach End

Greedy rule:

```text
Track the farthest index reachable so far.
```

### Pseudocode

```text
function canJump(nums):
    farthest = 0

    for i from 0 to n - 1:
        if i > farthest:
            return false

        farthest = max(farthest, i + nums[i])

        if farthest >= n - 1:
            return true

    return true
```

### How to identify

Use this when:

```text
Each index tells maximum jump length.
You only need possible or impossible.
```

---

## Jump Game II: Minimum Jumps

Greedy rule:

```text
Use the current jump range.
When the range ends, make another jump and extend the range as far as possible.
```

### Pseudocode

```text
function minJumps(nums):
    jumps = 0
    currentEnd = 0
    farthest = 0

    for i from 0 to n - 2:
        farthest = max(farthest, i + nums[i])

        if i == currentEnd:
            jumps += 1
            currentEnd = farthest

    return jumps
```

### How to identify

Use this when:

```text
Need minimum jumps.
At each step, you want to extend reach as far as possible.
```

---

# 11. Gas Station Greedy

Use when you need to find a valid starting point in a circular route.

Key idea:

```text
If total gas >= total cost, a solution exists.
If starting from station start fails at i, then none of the stations between start and i can be valid starts.
```

### Pseudocode

```text
function canCompleteCircuit(gas, cost):
    totalTank = 0
    currentTank = 0
    start = 0

    for i from 0 to n - 1:
        gain = gas[i] - cost[i]

        totalTank += gain
        currentTank += gain

        if currentTank < 0:
            start = i + 1
            currentTank = 0

    if totalTank >= 0:
        return start
    else:
        return -1
```

### How to identify

Use this when:

```text
Circular route.
Need starting point.
There is gain and cost at each station.
```

---

# 12. Candy Distribution Greedy

Use when each element must satisfy constraints relative to neighbors.

Problem type:

```text
Each child must get at least one candy.
Higher rating child must get more candy than adjacent lower rating child.
```

Greedy rule:

```text
Do one left-to-right pass and one right-to-left pass.
```

### Pseudocode

```text
function candy(ratings):
    n = length(ratings)
    candies = array of size n filled with 1

    for i from 1 to n - 1:
        if ratings[i] > ratings[i - 1]:
            candies[i] = candies[i - 1] + 1

    for i from n - 2 down to 0:
        if ratings[i] > ratings[i + 1]:
            candies[i] = max(candies[i], candies[i + 1] + 1)

    return sum(candies)
```

### How to identify

Use this when:

```text
Local neighbor constraints exist.
Left side and right side both matter.
One pass is not enough.
```

---

# 13. Prefix/Suffix Greedy

Use when the decision depends on information from the left and right sides.

Common problems:

```text
candy
trapping rain water
minimum removals
valid mountain-like constraints
```

### Generic Pseudocode

```text
function prefixSuffixGreedy(arr):
    left = array
    right = array

    left[0] = base
    for i from 1 to n - 1:
        left[i] = update_using(left[i - 1], arr[i])

    right[n - 1] = base
    for i from n - 2 down to 0:
        right[i] = update_using(right[i + 1], arr[i])

    answer = combine(left, right)

    return answer
```

### How to identify

Use this when:

```text
A position must satisfy both left and right conditions.
The answer at index i depends on best information before and after it.
```

---

# 14. Greedy With Heap / Priority Queue

Use when you repeatedly need the smallest or largest available item.

Common problems:

```text
meeting rooms
minimum platforms
task scheduler
reorganize string
IPO
course schedule III
minimum cost to connect ropes
```

### Generic Max-Heap Greedy

```text
function greedyMaxHeap(items):
    maxHeap = empty heap

    sort items if needed

    for item in items:
        add available items to maxHeap

        best = heapPop(maxHeap)
        use best
        update answer

    return answer
```

### Generic Min-Heap Greedy

```text
function greedyMinHeap(items):
    minHeap = empty heap

    for item in items:
        heapPush(minHeap, item)

        if need to remove smallest:
            heapPop(minHeap)

    return answer
```

---

## Minimum Cost to Connect Ropes

Greedy rule:

```text
Always connect the two smallest ropes first.
```

### Pseudocode

```text
function minCostRopes(ropes):
    minHeap = ropes
    buildHeap(minHeap)

    cost = 0

    while size(minHeap) > 1:
        first = heapPop(minHeap)
        second = heapPop(minHeap)

        merged = first + second
        cost += merged

        heapPush(minHeap, merged)

    return cost
```

### How to identify

Use this when:

```text
Combining items has cost equal to sum.
Need minimum total merge cost.
```

Similar to:

```text
Huffman coding
optimal merge pattern
```

---

# 15. Huffman Coding Greedy

Use when building an optimal prefix code.

Greedy rule:

```text
Repeatedly merge the two lowest-frequency nodes.
```

### Pseudocode

```text
function huffman(frequencies):
    minHeap = empty heap

    for each character and frequency:
        heapPush(minHeap, newNode(character, frequency))

    while size(minHeap) > 1:
        left = heapPop(minHeap)
        right = heapPop(minHeap)

        parent = newNode(null, left.freq + right.freq)
        parent.left = left
        parent.right = right

        heapPush(minHeap, parent)

    return heapPop(minHeap)    // root of Huffman tree
```

### How to identify

Use this when:

```text
Need optimal prefix encoding.
Frequent items should have shorter codes.
Repeatedly combining smallest weights makes sense.
```

---

# 16. Job Sequencing With Deadlines

Use when jobs have deadlines and profits, and each job takes one unit of time.

Greedy rule:

```text
Do the highest-profit job as late as possible before its deadline.
```

### Simple Pseudocode

```text
function jobSequencing(jobs):
    sort jobs by profit descending

    maxDeadline = maximum deadline among jobs
    slots = array of size maxDeadline + 1 filled with empty

    totalProfit = 0
    jobsDone = 0

    for job in jobs:
        for t from job.deadline down to 1:
            if slots[t] is empty:
                slots[t] = job
                totalProfit += job.profit
                jobsDone += 1
                break

    return (jobsDone, totalProfit)
```

### Optimized DSU Pseudocode

```text
function find(x):
    if parent[x] == x:
        return x
    parent[x] = find(parent[x])
    return parent[x]

function jobSequencing(jobs):
    sort jobs by profit descending

    maxDeadline = maximum deadline

    for i from 0 to maxDeadline:
        parent[i] = i

    totalProfit = 0
    jobsDone = 0

    for job in jobs:
        availableSlot = find(job.deadline)

        if availableSlot > 0:
            totalProfit += job.profit
            jobsDone += 1

            parent[availableSlot] = find(availableSlot - 1)

    return (jobsDone, totalProfit)
```

### How to identify

Use this when:

```text
Each job takes one unit time.
Each job has deadline and profit.
Need maximum profit.
Only one job can be done at a time.
```

---

# 17. Scheduling With Deadlines and Durations

Use when tasks have durations and deadlines, and you want to maximize how many can be completed.

Greedy rule:

```text
Sort by deadline.
Keep selected durations in a max-heap.
If total time exceeds deadline, remove the longest selected task.
```

### Pseudocode

```text
function scheduleCourse(courses):
    sort courses by deadline ascending

    maxHeap = empty heap     // stores durations
    totalTime = 0

    for course in courses:
        duration = course.duration
        deadline = course.deadline

        heapPush(maxHeap, duration)
        totalTime += duration

        if totalTime > deadline:
            longest = heapPopMax(maxHeap)
            totalTime -= longest

    return size(maxHeap)
```

### How to identify

Use this when:

```text
Tasks have duration and deadline.
Need maximum number of tasks.
Can drop previously chosen task.
Dropping the longest task is best.
```

---

# 18. Minimum Arrows to Burst Balloons

This is an interval greedy problem.

Greedy rule:

```text
Shoot arrow at the earliest ending balloon.
This bursts all balloons that start before or at that end.
```

### Pseudocode

```text
function findMinArrows(points):
    sort points by end ascending

    arrows = 0
    arrowPosition = -infinity

    for balloon in points:
        if balloon.start > arrowPosition:
            arrows += 1
            arrowPosition = balloon.end

    return arrows
```

### How to identify

Use this when:

```text
One action can cover multiple overlapping intervals.
Need minimum actions to cover all intervals.
```

---

# 19. Partition Labels Greedy

Use when you need to split a string so that each character appears in at most one part.

Greedy rule:

```text
A partition must extend until the last occurrence of every character seen so far.
```

### Pseudocode

```text
function partitionLabels(s):
    last = map from character to last index

    for i from 0 to length(s) - 1:
        last[s[i]] = i

    result = empty list
    start = 0
    end = 0

    for i from 0 to length(s) - 1:
        end = max(end, last[s[i]])

        if i == end:
            result.add(end - start + 1)
            start = i + 1

    return result
```

### How to identify

Use this when:

```text
Need to partition string/array.
Each value should belong to only one partition.
Need smallest/maximum valid chunks.
```

---

# 20. Valid Parentheses With Wildcards

Example problem:

```text
String contains '(', ')' and '*'.
'*' can be '(', ')' or empty.
Check if string can be valid.
```

Greedy rule:

```text
Track the possible range of open brackets.
```

### Pseudocode

```text
function checkValidString(s):
    low = 0     // minimum possible open brackets
    high = 0    // maximum possible open brackets

    for ch in s:
        if ch == '(':
            low += 1
            high += 1
        else if ch == ')':
            low -= 1
            high -= 1
        else:
            low -= 1      // use '*' as ')'
            high += 1     // use '*' as '('

        if high < 0:
            return false

        if low < 0:
            low = 0

    return low == 0
```

### How to identify

Use this when:

```text
A wildcard has multiple possible meanings.
You do not need exact choices, only a valid range.
```

---

# 21. Monotonic Greedy

Use when maintaining increasing or decreasing order helps remove bad choices.

Common problems:

```text
remove k digits
create maximum number
smallest subsequence of distinct characters
lexicographically smallest result
```

Greedy rule:

```text
While the previous choice is worse than the current choice, remove it if allowed.
```

Usually implemented with a stack.

### Generic Pseudocode

```text
function monotonicGreedy(arr, k):
    stack = empty stack

    for x in arr:
        while stack is not empty and k > 0 and stack.top is worse than x:
            stack.pop()
            k -= 1

        stack.push(x)

    while k > 0:
        stack.pop()
        k -= 1

    return stack as answer
```

---

## Remove K Digits

Goal:

```text
Remove k digits to make the smallest possible number.
```

Greedy rule:

```text
If previous digit is bigger than current digit, remove previous digit.
```

### Pseudocode

```text
function removeKdigits(num, k):
    stack = empty stack

    for digit in num:
        while stack is not empty and k > 0 and stack.top > digit:
            stack.pop()
            k -= 1

        stack.push(digit)

    while k > 0:
        stack.pop()
        k -= 1

    answer = stack joined as string
    remove leading zeroes from answer

    if answer is empty:
        return "0"
    else:
        return answer
```

### How to identify

Use this when:

```text
Need lexicographically smallest/largest result.
Can remove limited elements.
Order of remaining elements must stay same.
```

---

# 22. Greedy With Frequencies

Use when choices depend on counts.

Common problems:

```text
reorganize string
task scheduler
hand of straights
minimum deletions for unique frequencies
```

### Generic Pseudocode

```text
function frequencyGreedy(items):
    freq = count frequencies of items

    organize frequencies using heap or sorting

    while choices remain:
        choose item with best frequency condition
        update frequency
        update answer

    return answer
```

---

## Reorganize String

Goal:

```text
Rearrange string so no two adjacent characters are equal.
```

Greedy rule:

```text
Always place the character with highest remaining frequency that is not equal to previous character.
```

### Pseudocode

```text
function reorganizeString(s):
    freq = frequency map of characters
    maxHeap = characters by frequency

    result = empty string
    previous = null

    while maxHeap is not empty:
        current = heapPop(maxHeap)
        result.add(current.char)
        current.freq -= 1

        if previous is not null and previous.freq > 0:
            heapPush(maxHeap, previous)

        previous = current

    if length(result) != length(s):
        return ""

    return result
```

### How to identify

Use this when:

```text
Need to rearrange items with frequency constraints.
Avoid adjacent duplicates.
Most frequent item should be handled first.
```

---

# 23. Task Scheduler Greedy

Use when tasks have cooldowns.

Core idea:

```text
The most frequent task determines the minimum possible gaps.
```

### Formula-Based Pseudocode

```text
function leastInterval(tasks, cooldown):
    freq = frequency of each task
    maxFreq = maximum frequency
    maxCount = number of tasks with frequency == maxFreq

    partCount = maxFreq - 1
    partLength = cooldown + 1

    minimumSlots = partCount * partLength + maxCount

    return max(length(tasks), minimumSlots)
```

### Heap Simulation Pseudocode

```text
function leastInterval(tasks, cooldown):
    freq = frequency map
    maxHeap = frequencies
    queue = empty queue    // stores (frequencyLeft, availableTime)

    time = 0

    while maxHeap not empty or queue not empty:
        time += 1

        if maxHeap not empty:
            f = heapPopMax(maxHeap)
            f -= 1

            if f > 0:
                queue.push((f, time + cooldown))

        if queue not empty and queue.front.availableTime == time:
            heapPush(maxHeap, queue.pop().frequencyLeft)

    return time
```

### How to identify

Use this when:

```text
Tasks repeat.
Same task needs cooldown before reuse.
Need minimum total time.
```

---

# 24. Greedy for Buying/Selling Stock

Use when profit can be collected from every upward movement.

---

## Best Time to Buy and Sell Stock II

Greedy rule:

```text
Take every positive price difference.
```

### Pseudocode

```text
function maxProfit(prices):
    profit = 0

    for i from 1 to n - 1:
        if prices[i] > prices[i - 1]:
            profit += prices[i] - prices[i - 1]

    return profit
```

### How to identify

Use this when:

```text
Unlimited transactions are allowed.
You can buy and sell multiple times.
No cooldown or fee unless handled separately.
```

Important:

```text
With cooldown, transaction fee, or limited transactions, stock problems usually need DP.
```

---

# 25. Greedy for Maximum Subarray-Like Problems

Kadane's algorithm is greedy-like and DP-like.

Greedy rule:

```text
If the current sum becomes negative, discard it.
```

### Pseudocode

```text
function maxSubarray(nums):
    current = nums[0]
    best = nums[0]

    for i from 1 to n - 1:
        current = max(nums[i], current + nums[i])
        best = max(best, current)

    return best
```

### How to identify

Use this when:

```text
Need maximum sum contiguous subarray.
A bad prefix should be discarded.
```

---

# 26. Greedy Graph Algorithms

Some graph algorithms are greedy.

Common examples:

```text
Dijkstra's algorithm
Prim's algorithm
Kruskal's algorithm
```

---

# 27. Dijkstra's Algorithm

Use for shortest path from one source when edge weights are non-negative.

Greedy rule:

```text
Always finalize the unvisited node with the smallest known distance.
```

### Pseudocode

```text
function dijkstra(graph, source):
    dist = array filled with infinity
    dist[source] = 0

    minHeap = empty heap
    heapPush(minHeap, (0, source))

    while minHeap is not empty:
        currentDist, node = heapPop(minHeap)

        if currentDist > dist[node]:
            continue

        for neighbor, weight in graph[node]:
            newDist = currentDist + weight

            if newDist < dist[neighbor]:
                dist[neighbor] = newDist
                heapPush(minHeap, (newDist, neighbor))

    return dist
```

### How to identify

Use Dijkstra when:

```text
Need shortest path.
Graph has non-negative edge weights.
Source node is fixed.
```

Do not use Dijkstra when:

```text
There are negative edge weights.
```

For negative weights, consider Bellman-Ford.

---

# 28. Prim's Algorithm

Use for minimum spanning tree.

Greedy rule:

```text
Always add the smallest edge that connects the current tree to a new node.
```

### Pseudocode

```text
function prim(graph):
    visited = set
    minHeap = empty heap

    start = any node
    heapPush(minHeap, (0, start))

    totalCost = 0

    while minHeap is not empty:
        cost, node = heapPop(minHeap)

        if node in visited:
            continue

        visited.add(node)
        totalCost += cost

        for neighbor, edgeCost in graph[node]:
            if neighbor not in visited:
                heapPush(minHeap, (edgeCost, neighbor))

    return totalCost
```

### How to identify

Use Prim when:

```text
Need to connect all nodes with minimum total edge cost.
Graph is weighted and undirected.
You want MST.
```

---

# 29. Kruskal's Algorithm

Use for minimum spanning tree.

Greedy rule:

```text
Sort all edges by weight.
Pick the smallest edge that does not create a cycle.
```

Uses DSU / Union Find.

### Pseudocode

```text
function kruskal(n, edges):
    sort edges by weight ascending

    parent = array where parent[i] = i
    rank = array filled with 0

    totalCost = 0
    edgesUsed = 0

    for edge in edges:
        u = edge.u
        v = edge.v
        w = edge.weight

        if find(u) != find(v):
            union(u, v)
            totalCost += w
            edgesUsed += 1

            if edgesUsed == n - 1:
                break

    return totalCost
```

### DSU Pseudocode

```text
function find(x):
    if parent[x] == x:
        return x

    parent[x] = find(parent[x])
    return parent[x]

function union(a, b):
    rootA = find(a)
    rootB = find(b)

    if rootA == rootB:
        return

    if rank[rootA] < rank[rootB]:
        parent[rootA] = rootB
    else if rank[rootA] > rank[rootB]:
        parent[rootB] = rootA
    else:
        parent[rootB] = rootA
        rank[rootA] += 1
```

### How to identify

Use Kruskal when:

```text
Need minimum spanning tree.
Edges are easier to sort than traversing from a node.
Need to avoid cycles.
```

---

# 30. Greedy With Sweep Line

Use when events happen over time or positions.

Common problems:

```text
minimum platforms
maximum population year
car pooling
meeting rooms
skyline-like problems
```

Greedy idea:

```text
Process events in sorted order.
Maintain current active count/state.
```

### Pseudocode

```text
function sweepLine(events):
    points = empty list

    for event in events:
        points.add((event.start, +1))
        points.add((event.end, -1))

    sort points by position

    current = 0
    answer = 0

    for point in points:
        current += point.delta
        answer = max(answer, current)

    return answer
```

### How to identify

Use this when:

```text
You need maximum overlap.
Events have start and end.
Need active count at any time.
```

---

# 31. Greedy for Minimum Platforms

Greedy rule:

```text
Sort arrivals and departures separately.
If next train arrives before the earliest departure, need one more platform.
Otherwise, free one platform.
```

### Pseudocode

```text
function minPlatforms(arrivals, departures):
    sort arrivals
    sort departures

    i = 0
    j = 0
    platforms = 0
    answer = 0

    while i < n:
        if arrivals[i] <= departures[j]:
            platforms += 1
            answer = max(answer, platforms)
            i += 1
        else:
            platforms -= 1
            j += 1

    return answer
```

### How to identify

Use this when:

```text
Need minimum resources for overlapping time intervals.
Start and end times can be processed separately.
```

---

# 32. Greedy for Lexicographical Order

Use when you need the smallest or largest string/sequence after operations.

Common problems:

```text
remove duplicate letters
smallest subsequence
remove k digits
construct smallest number
```

Greedy rule:

```text
Build the answer from left to right.
Remove previous characters if a better current character appears and removal is allowed.
```

### Remove Duplicate Letters Pseudocode

```text
function removeDuplicateLetters(s):
    last = map character to last occurrence index
    stack = empty stack
    used = empty set

    for i from 0 to length(s) - 1:
        ch = s[i]

        if ch in used:
            continue

        while stack not empty and stack.top > ch and last[stack.top] > i:
            removed = stack.pop()
            used.remove(removed)

        stack.push(ch)
        used.add(ch)

    return stack as string
```

### How to identify

Use this when:

```text
Need lexicographically smallest/largest valid sequence.
Characters must remain in relative order.
You can remove duplicates or limited items.
```

---

# 33. Greedy for Range Coverage

Use when you need minimum intervals/jumps/taps to cover a target range.

Common problems:

```text
minimum taps to water garden
video stitching
jump game II
minimum intervals to cover range
```

Greedy rule:

```text
Among all intervals starting before or at current coverage, choose the one that extends coverage farthest.
```

### Pseudocode

```text
function minIntervalsToCover(intervals, targetStart, targetEnd):
    sort intervals by start ascending

    i = 0
    currentEnd = targetStart
    farthest = targetStart
    count = 0

    while currentEnd < targetEnd:
        while i < n and intervals[i].start <= currentEnd:
            farthest = max(farthest, intervals[i].end)
            i += 1

        if farthest == currentEnd:
            return impossible

        count += 1
        currentEnd = farthest

    return count
```

### How to identify

Use this when:

```text
Need to cover [0, target] or some range.
Each interval covers a segment.
Need minimum number of intervals.
```

---

# 34. Greedy for Advantage / Rearrangement

Use when you need to match one array against another to maximize wins.

Common problem:

```text
Advantage Shuffle
```

Greedy rule:

```text
Use the smallest number that can beat the opponent's current number.
If you cannot beat it, sacrifice the smallest number.
```

### Pseudocode

```text
function advantageShuffle(A, B):
    sort A ascending

    pairs = B with original indices
    sort pairs by value descending

    left = 0
    right = length(A) - 1
    result = array

    for value, index in pairs:
        if A[right] > value:
            result[index] = A[right]
            right -= 1
        else:
            result[index] = A[left]
            left += 1

    return result
```

### How to identify

Use this when:

```text
Need to maximize pairwise wins.
A larger value beats a smaller value.
Sacrificing weak values can save strong values.
```

---

# 35. Greedy for Majority / Voting

Boyer-Moore Voting Algorithm uses a greedy cancellation idea.

Use when an element appears more than half the time.

Greedy rule:

```text
Different elements cancel each other.
The majority element survives.
```

### Pseudocode

```text
function majorityElement(nums):
    candidate = null
    count = 0

    for x in nums:
        if count == 0:
            candidate = x
            count = 1
        else if x == candidate:
            count += 1
        else:
            count -= 1

    return candidate
```

### How to identify

Use this when:

```text
There is guaranteed majority element.
Need O(1) space.
Majority means frequency > n / 2.
```

---

# 36. Greedy for Increasing Triplet / Subsequence

Use when you track the smallest possible values for positions.

Greedy rule:

```text
Keep the smallest possible first and second values.
```

### Pseudocode

```text
function increasingTriplet(nums):
    first = infinity
    second = infinity

    for x in nums:
        if x <= first:
            first = x
        else if x <= second:
            second = x
        else:
            return true

    return false
```

### How to identify

Use this when:

```text
Need existence of increasing subsequence of fixed small length.
You only need to track best candidates, not all subsequences.
```

For general LIS length, use DP or binary search greedy.

---

# 37. Greedy + Binary Search: LIS

Longest Increasing Subsequence can be solved using greedy with binary search.

Greedy rule:

```text
For every length, keep the smallest possible tail value.
```

### Pseudocode

```text
function lengthOfLIS(nums):
    tails = empty list

    for x in nums:
        pos = lowerBound(tails, x)

        if pos == length(tails):
            tails.add(x)
        else:
            tails[pos] = x

    return length(tails)
```

### How to identify

Use this when:

```text
Need LIS length only.
Need O(n log n).
```

Use DP when:

```text
Need to reconstruct all subsequences.
Need count of LIS.
```

---

# 38. Greedy for Smallest Number From Pattern

Example:

```text
Pattern contains I and D.
Construct smallest number that follows the pattern.
```

Greedy rule:

```text
For every decreasing block, reverse that block.
```

### Stack Pseudocode

```text
function smallestNumber(pattern):
    result = empty string
    stack = empty stack

    for i from 0 to length(pattern):
        stack.push(i + 1)

        if i == length(pattern) or pattern[i] == 'I':
            while stack not empty:
                result += stack.pop()

    return result
```

### How to identify

Use this when:

```text
Need smallest lexicographic/number arrangement.
Pattern has increasing and decreasing constraints.
```

---

# 39. Greedy for Balancing / Minimum Swaps

Use when you need to balance brackets or groups with minimum operations.

Greedy idea:

```text
Scan left to right.
Fix imbalance only when necessary.
```

### Generic Pseudocode

```text
function minFixes(sequence):
    balance = 0
    answer = 0

    for x in sequence:
        update balance based on x

        if balance becomes invalid:
            answer += 1
            fix balance greedily

    return answer
```

### How to identify

Use this when:

```text
Need minimum swaps/reversals/fixes.
There is a running balance.
Only fix when the prefix becomes invalid.
```

---

# 40. Greedy for Array Transformation

Use when you need to transform an array with minimum operations.

Common examples:

```text
make array non-decreasing
minimum replacements
minimum increments
```

Greedy rule depends on direction:

```text
Left to right: fix current based on previous.
Right to left: fix current based on next.
```

### Generic Pseudocode

```text
function transform(arr):
    operations = 0

    for i from 1 to n - 1:
        if arr[i] violates condition with arr[i - 1]:
            change arr[i] minimally
            operations += needed changes

    return operations
```

### How to identify

Use this when:

```text
Need minimum changes.
Each position only needs to satisfy local condition with neighbor.
Changing minimally preserves future flexibility.
```

---

# 41. Greedy for Maximum Product / Sign Handling

Use when signs matter and sorting helps.

Example:

```text
maximum product of three numbers
```

Greedy rule:

```text
The answer is either:
1. three largest numbers
2. two smallest negative numbers and the largest positive number
```

### Pseudocode

```text
function maximumProduct(nums):
    sort(nums)

    n = length(nums)

    option1 = nums[n - 1] * nums[n - 2] * nums[n - 3]
    option2 = nums[0] * nums[1] * nums[n - 1]

    return max(option1, option2)
```

### How to identify

Use this when:

```text
Need maximum/minimum product.
Negative numbers can turn positive when paired.
Extreme values matter.
```

---

# 42. Greedy for Loading / Capacity

Use when items must be loaded into minimum containers under a capacity limit.

Common examples:

```text
boats rescue people
bin-packing-like simplified problems
pairing weights
```

Greedy rule:

```text
Handle the largest item first.
Pair it with the smallest possible item if allowed.
```

### Pseudocode

```text
function minContainers(weights, limit):
    sort(weights)

    left = 0
    right = n - 1
    containers = 0

    while left <= right:
        if weights[left] + weights[right] <= limit:
            left += 1

        right -= 1
        containers += 1

    return containers
```

### How to identify

Use this when:

```text
Each container can hold at most two items.
Need minimum containers.
Sorting and pairing extremes is safe.
```

Important:

```text
General bin packing is not solved optimally by simple greedy.
```

---

# 43. Greedy for Minimum Operations to Reach Target

Sometimes working backwards is greedy.

Example:

```text
Start from x, reach y.
Allowed operations: multiply by 2 or subtract 1.
```

Greedy rule:

```text
Work backward from y to x.
If y is odd, increment it.
If y is even, divide by 2.
```

### Pseudocode

```text
function brokenCalculator(x, y):
    operations = 0

    while y > x:
        if y is odd:
            y += 1
        else:
            y = y / 2

        operations += 1

    return operations + (x - y)
```

### How to identify

Use this when:

```text
Forward choices branch too much.
Reverse operation has an obvious best choice.
Target can be reduced greedily.
```

---

# 44. Greedy for String Construction

Use when building a string under constraints.

Common problems:

```text
longest happy string
reorganize string
construct string with repeat limit
```

Greedy rule:

```text
Use the most frequent/best character unless it violates the constraint.
If it violates, use the next best character.
```

### Pseudocode

```text
function buildString(freq):
    maxHeap = characters by frequency
    result = empty string

    while maxHeap is not empty:
        first = heapPop(maxHeap)

        if adding first violates condition:
            if maxHeap is empty:
                break

            second = heapPop(maxHeap)
            add second to result
            decrease second frequency

            if second still available:
                heapPush(maxHeap, second)

            heapPush(maxHeap, first)
        else:
            add first to result
            decrease first frequency

            if first still available:
                heapPush(maxHeap, first)

    return result
```

### How to identify

Use this when:

```text
Need longest or valid string.
Characters have frequency constraints.
Need avoid repeated adjacent characters or repeat limits.
```

---

# 45. Greedy Proof Techniques

You should be able to justify why greedy works.

The most common proof techniques are:

```text
1. Exchange argument
2. Staying ahead argument
3. Cut property
4. Contradiction
```

---

## Exchange Argument

Used for:

```text
activity selection
interval scheduling
job scheduling
sorting-based greedy
```

Idea:

```text
Take any optimal solution.
If it does not use the greedy choice, replace one of its choices with the greedy choice.
Show the solution is still valid and not worse.
```

### Proof Template

```text
Assume there is an optimal solution OPT.
Let G be the greedy choice.
If OPT already contains G, we are done.
Otherwise, replace the corresponding choice in OPT with G.
This replacement does not make the answer worse.
Therefore, there exists an optimal solution containing G.
Now solve the remaining subproblem greedily.
```

---

## Staying Ahead Argument

Used for:

```text
jump game
interval coverage
minimum jumps
scheduling
```

Idea:

```text
After each step, greedy is at least as good as any other solution.
```

### Proof Template

```text
After k choices, greedy reaches at least as far as any other method using k choices.
Therefore, when greedy finishes, no other method can use fewer choices.
```

---

## Cut Property

Used for:

```text
minimum spanning tree
Kruskal
Prim
```

Idea:

```text
The minimum edge crossing a cut is always safe to choose.
```

### Proof Template

```text
Consider a cut separating chosen nodes from unchosen nodes.
The lightest edge crossing this cut must be part of some MST.
Choosing it is safe.
```

---

## Contradiction

Used broadly.

Idea:

```text
Assume greedy is not optimal.
Then show this leads to a contradiction with the greedy choice being locally best.
```

---

# 46. Greedy Failure Signs

Greedy may not work when:

```text
A local best choice can block a better future choice.
Choices interact in complex ways.
You need to consider many combinations.
The problem has arbitrary weights and constraints.
The problem asks for exact count of ways.
There are overlapping subproblems.
```

Examples where greedy often fails:

```text
0/1 knapsack
minimum coins with arbitrary denominations
longest common subsequence
edit distance
subset sum
partition equal subset sum
matrix chain multiplication
```

For these, use DP.

---

# 47. Greedy vs DP Identification

| Question | If yes, likely |
|---|---|
| Can a local best choice be proven safe? | Greedy |
| Do I need to compare many future possibilities? | DP |
| Does the same state repeat? | DP |
| Does sorting create a simple rule? | Greedy |
| Is the problem asking for exact count of ways? | DP |
| Can I solve by always taking earliest/latest/smallest/largest? | Greedy |
| Does arbitrary input break local choices? | DP |

---

# 48. Common Greedy Rules

| Greedy Rule | Used In |
|---|---|
| Pick earliest ending interval | Activity selection |
| Pick interval that extends coverage farthest | Video stitching, minimum taps |
| Pick highest value/weight ratio | Fractional knapsack |
| Pick two smallest items | Connect ropes, Huffman |
| Pick highest profit first | Job sequencing |
| Pick smallest sufficient resource | Assign cookies |
| Pick heaviest with lightest | Boats to save people |
| Track farthest reachable | Jump game |
| Track current balance | Gas station, brackets |
| Remove previous worse element | Monotonic stack greedy |
| Process smallest edge first | Kruskal |
| Process closest node first | Dijkstra |
| Add cheapest edge to tree | Prim |

---

# 49. Pattern Selection Table

| Problem Type | Greedy Pattern |
|---|---|
| Maximum non-overlapping meetings | Sort by end time |
| Minimum intervals to remove | Sort by end time |
| Merge intervals | Sort by start time |
| Minimum meeting rooms | Sort + min-heap |
| Minimum platforms | Two arrays / sweep line |
| Assign cookies | Sort both arrays |
| Boats to save people | Two pointers |
| Fractional knapsack | Sort by value/weight ratio |
| Job sequencing | Sort by profit + latest slot |
| Connect ropes | Min-heap |
| Huffman coding | Min-heap |
| Jump game possible | Track farthest reach |
| Minimum jumps | Current range + farthest |
| Gas station | Reset start when tank negative |
| Candy | Left pass + right pass |
| Remove k digits | Monotonic increasing stack |
| Reorganize string | Max-heap by frequency |
| Task scheduler | Frequency greedy / heap |
| Partition labels | Last occurrence boundary |
| Dijkstra | Min distance heap |
| Prim | Minimum outgoing edge |
| Kruskal | Sort edges + DSU |
| Cover target range | Extend farthest coverage |

---

# 50. The Greedy Decision Flow

Use this flow in interviews:

```text
1. Is the problem asking for all solutions?
      Yes -> Not greedy. Use backtracking.

2. Is the problem asking for count/min/max/possible?
      Yes -> Continue.

3. Can I sort the input to expose a simple choice?
      Yes -> Try sorting greedy.

4. Is there an interval conflict?
      Yes -> Usually sort by end time or use heap.

5. Are resources being assigned to requirements?
      Yes -> Sort both and use two pointers.

6. Are we repeatedly taking smallest/largest available?
      Yes -> Use heap.

7. Is it a graph shortest path with non-negative weights?
      Yes -> Dijkstra.

8. Is it MST?
      Yes -> Prim or Kruskal.

9. Is it about reaching farthest/min jumps/covering range?
      Yes -> Track farthest reach.

10. Can a local choice hurt the future?
      Yes or unsure -> Think DP or backtracking.

11. Can I prove greedy choice is safe?
      Yes -> Greedy.
```

---

# 51. How to Design a Greedy Solution

Use this process:

```text
1. Understand the objective.
   Are you minimizing or maximizing?

2. Identify the decision.
   What do you choose at each step?

3. Find the greedy rule.
   Earliest? Latest? Smallest? Largest? Highest ratio? Farthest reach?

4. Sort or use a heap.
   Organize data so the best choice is easy to access.

5. Simulate choices.
   Take the choice if it keeps the solution valid.

6. Prove safety.
   Use exchange argument, staying ahead, or contradiction.

7. Check edge cases.
   Empty input, single item, ties, impossible cases.
```

---

# 52. Common Edge Cases

Always check:

```text
empty array
one element
all intervals overlap
no intervals overlap
same start/end times
duplicate values
impossible case
zero capacity
negative numbers
large values causing overflow
already sorted input
reverse sorted input
```

---

# 53. Common Mistakes in Greedy

```text
1. Sorting by the wrong key.
2. Assuming greedy works without proof.
3. Using greedy for 0/1 knapsack.
4. Using greedy for arbitrary coin change.
5. Forgetting tie handling.
6. Not considering impossible cases.
7. Confusing maximum selected intervals with minimum rooms.
8. Forgetting to update heap/state correctly.
9. Using local max when local min is needed.
10. Not testing against brute force for small cases.
```

---

# 54. Quick Comparison: Interval Patterns

| Problem | Sort By | Data Structure | Greedy Choice |
|---|---|---|---|
| Max non-overlapping intervals | End time | None | Pick earliest ending |
| Min remove overlapping | End time | None | Keep earliest ending |
| Merge intervals | Start time | Result list | Extend current interval |
| Meeting rooms | Start time | Min-heap of end times | Reuse earliest ending room |
| Min arrows | End time | None | Shoot at earliest end |
| Cover range | Start time | None | Extend farthest |

---

# 55. Quick Comparison: Heap Greedy Patterns

| Problem | Heap Type | Greedy Choice |
|---|---|---|
| Connect ropes | Min-heap | Merge two smallest |
| Huffman coding | Min-heap | Merge two lowest frequencies |
| Meeting rooms | Min-heap | Reuse earliest ending room |
| Dijkstra | Min-heap | Visit smallest distance node |
| Prim | Min-heap | Add smallest edge |
| Reorganize string | Max-heap | Use most frequent valid char |
| Task scheduler | Max-heap + queue | Use most frequent available task |
| Course schedule III | Max-heap | Remove longest duration if late |

---

# 56. Quick Comparison: Sorting Rules

| Sorting Rule | Use When |
|---|---|
| Sort by end ascending | Want maximum non-overlapping intervals |
| Sort by start ascending | Want merge/scan intervals |
| Sort by profit descending | Want highest profit jobs first |
| Sort by ratio descending | Fractional knapsack |
| Sort by deadline ascending | Scheduling tasks by deadlines |
| Sort both arrays ascending | Assign resources to requirements |
| Sort edges by weight | Kruskal MST |
| Sort weights ascending | Two-pointer pairing |

---

# 57. Master Templates

## Sorting Greedy

```text
function solve(items):
    sort(items, by greedy_rule)

    answer = 0
    state = initial_state

    for item in items:
        if valid(item, state):
            answer = update_answer(answer, item)
            state = update_state(state, item)

    return answer
```

---

## Heap Greedy

```text
function solve(items):
    heap = empty heap
    answer = 0

    for item in items:
        add eligible items to heap

        best = heapPop(heap)
        use best
        update answer

    return answer
```

---

## Two-Pointer Greedy

```text
function solve(arr):
    sort(arr)

    left = 0
    right = n - 1
    answer = 0

    while left <= right:
        if can_pair(arr[left], arr[right]):
            left += 1
            right -= 1
        else:
            right -= 1

        answer += 1

    return answer
```

---

## Farthest Reach Greedy

```text
function solve(arr):
    farthest = 0

    for i from 0 to n - 1:
        if i > farthest:
            return impossible

        farthest = max(farthest, reach_from(i))

    return answer
```

---

## Interval Coverage Greedy

```text
function cover(intervals, targetEnd):
    sort intervals by start

    currentEnd = 0
    farthest = 0
    count = 0
    i = 0

    while currentEnd < targetEnd:
        while i < n and intervals[i].start <= currentEnd:
            farthest = max(farthest, intervals[i].end)
            i += 1

        if farthest == currentEnd:
            return impossible

        count += 1
        currentEnd = farthest

    return count
```

---

## Monotonic Stack Greedy

```text
function solve(sequence, k):
    stack = empty stack

    for x in sequence:
        while stack not empty and k > 0 and stack.top is worse than x:
            stack.pop()
            k -= 1

        stack.push(x)

    while k > 0:
        stack.pop()
        k -= 1

    return stack
```

---

# 58. Final Rule of Thumb

Think like this:

```text
Need all solutions?
    -> Backtracking

Need count/min/max with repeated states?
    -> DP

Need local best and no revisiting?
    -> Greedy

Intervals?
    -> Sort by end/start or use heap

Resources and requirements?
    -> Sort both, two pointers

Repeated smallest/largest choice?
    -> Heap

Reachability or minimum jumps?
    -> Track farthest reach

String with lexicographic removal?
    -> Monotonic stack

Graph shortest path with non-negative weights?
    -> Dijkstra

Minimum spanning tree?
    -> Prim or Kruskal
```

The most important question is:

```text
Can I prove that this local choice will never make the final answer worse?
```

If yes, greedy is probably correct.
If no, think DP, backtracking, or binary search on answer.
