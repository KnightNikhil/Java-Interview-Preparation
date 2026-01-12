The 15 core DSA patterns from Grokking-style resources cover most interview problems, each with a template to recognize and apply quickly for optimal solutions. These build on arrays, trees, graphs, and DP, helping achieve O(n log n) or better complexity. Use them to classify LeetCode hards as mediums

## Sliding Window
Maintains a window [left, right] for subarray/substring problems, adding/removing elements to track min/max/valid states. Optimizes O(n²) scans to O(n) by incremental updates.

Dynamic template:
```java
int left=0; for(int right=0; right<n; right++) {
  add arr[right];
  while(invalid) remove arr[left++];
  update maxLen = Math.max(maxLen, right-left+1);
}
```
Examples: Longest substring no repeat, permutation in string.

## Two Pointers
Two indices converge/diverge on sorted arrays for pairs or partitions. Avoids nested loops for O(n) pair sums or sorts.

Template:
```java
int l=0, r=n-1;
while(l<r){
  if(arr[l]+arr[r]==target) return true;
  else if(arr[l]+arr[r]<target) l++;
  else r--;
}
```
Examples: Pair target sum, container most water.

## Fast & Slow Pointers
Slow advances 1, fast 2 steps to detect cycles or middles in lists/arrays. O(n) cycle detection without extra space.

Template (cycle detect):
```java
while(slow != fast && fast && fast.next){
  slow = slow.next;
  fast = fast.next.next;
}
```
Examples: LinkedList cycle, happy number.

## Merge Intervals
Sort intervals by start, merge overlaps for scheduling/conflicts. Handles O(n log n) sorting then linear merge.

Template:
```java
intervals.sort((a,b)->a[0]-b[0]);
List<int[]> res = new ArrayList<>();
for(int[] i: intervals){
  if(res.isEmpty() || res.getLast()[1] < i[0])
    res.add(i);
  else res.getLast()[1] = Math.max(res.getLast()[1], i[1]);
}
```
Examples: Merge intervals, insert interval.

## Cyclic Sort
For 1..n unique arrays, swap i to arr[arr[i]-1] position. O(n) + O(1) space for finding duplicates/missing.

Template:
```java
for(int i=0; i<n; i++)
  while(arr[i] != i+1)
    swap(arr[i], arr[arr[i]-1]);
```
Examples: Find duplicate, missing number.

## In-place Reversal LinkedList
Reverse nodes by prev/next pointers without extra space. Builds reversal for palindromes/rotations

Template:
```java
Node prev=null, curr=head;
while(curr!=null){
  Node next=curr.next;
  curr.next=prev;
  prev=curr; curr=next;
}
```
Examples: Reverse linkedlist, rotate list

## Tree BFS
Level-order queue traversal for shortest path/heights. O(n) visits each node once.

Template:
```java
Queue<Node> q = new LinkedList<>(); q.add(root);
while(!q.isEmpty()){
  int size=q.size();
  for(int i=0; i<size; i++){
    Node node=q.poll();
    // process
    if(node.left) q.add(node.left);
    if(node.right) q.add(node.right);
  }
}
```
Examples: Level order, right side view.

## Tree DFS
Recursive inorder/pre/postorder for paths/sums. Stack simulates for iterative.

Template:
```java
void dfs(Node node){
  if(node==null) return;
  dfs(node.left);
  // process
  dfs(node.right);
}
```
Examples: Path sum, max depth.

## Two Heaps
Max-heap for smaller half, min-heap for larger; balance for median. O(log n) inserts.

Template: PriorityQueue maxH = new PriorityQueue<>((a,b)->b-a); minH for larger.
Balance: if(maxH.size()>minH.size()+1) minH.add(maxH.poll());
Examples: Find median data stream.

## Subsets (Backtracking)
Recurse choose/not choose for combinations/permutations. Prunes with used array.

Template:
```java
void backtrack(int i, List<Integer> track){
  if(i==n){ res.add(new ArrayList(track)); return;}
  track.add(nums[i]); backtrack(i+1,track); track.remove(track.size()-1);
  backtrack(i+1,track);
}
```
Examples: Subsets, permutations.

## Modified Binary Search
Adapt for rotated/ceiling/floor in sorted arrays. Halves search space.

Template:
```java
while(l<=r){
  int m=(l+r)/2;
  if(arr[m]==target) return m;
  else if(arr[m]>target) r=m-1;
  else l=m+1;
}
```
Examples: Search rotated, ceiling number.

## Top K Elements
Heap or quickselect for kth largest/smallest. O(n log k) heap efficient.

Template: PriorityQueue pq = new PriorityQueue<>(k, Comparator.reverseOrder()); pq.add/addAll/poll.
Examples: K closest points, top k frequent.

## K-way Merge
PriorityQueue of heads for merging sorted lists/arrays. O(n log k)

Template: 
```java
pq.add(new int[]{0,0}); // {sum, idx}
while(!pq.isEmpty()){
    int[] top = pq.poll();
    // add
    if(top+1 < lists[top].size)
        pq.add(new int[]{top, top+1});
}
```
Examples: Merge k sorted lists

## 0/1 Knapsack DP
Table dp[i][w] = max(dp[i-1][w], dp[i-1][w-weight[i]] + val[i]). O(nW) space optimizable

Template:
```java
for(int i=1; i<=n; i++)
  for(int w=1; w<=W; w++)
    if(weight[i-1]<=w)
      dp[i][w] = max(dp[i-1][w], dp[i-1][w-weight[i-1]]+val[i-1]);
```
Examples: 0/1 knapsack

