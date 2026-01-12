1. Your Spring Boot application is experiencing performance issues under high load. What are the steps you would take to identify and address the performance?
   

2. What strategies would you use to optimize the performance of a Spring Boot application?
   Let’s say my Spring Boot application is taking too long to respond to user requests. I could:
   • Implement caching for frequently accessed data.
   • Optimize database queries to reduce the load on the database.
   • Use asynchronous methods for operations like sending emails.
   • Load Balancer if traffic is high
   • Optimize the time complexity of the code
   • Use webFlux to handle a large number of concurrent connections.

3. scheduled jobs in your spring boot application is running twice in production even though you deployed only once. What might be happening?

4. So let's say your rest API allows file uploads but users complain about large files failing silently. How would you debug and fix this?

5. So uh let's say your API endpoints returns an empty JSON response even though that DB has data.

6. So in your system you have 10 spring boot microservices. Okay. Managing configuration separately has been painful. Right. Managing configurations separately has been painful. How would you handle centralized config management?

7. so how are static variables stored in memory compared to instance variable?


8. if I am using cache in my application, there will be a time cache expires, and at the same time if 100s of request tries access the data, all 100s o request will go to db?
This is an excellent and very real-world question — and yes, what you’re describing is a classic production problem.

Short answer first, then we’ll go deep.

⸻

🔴 Short Answer

Yes — if you do nothing special, when cache expires and 100 requests arrive at the same time, all 100 can hit the DB.

This is called Cache Stampede (or Thundering Herd).

⸻

1️⃣ What Exactly Is Happening (Timeline)

Assume:
•	Cache TTL = 5 minutes
•	DB query = expensive

Timeline

T0: Cache entry expires
T1: 100 requests arrive at same time

Each request:
1.	Checks cache → MISS
2.	Goes to DB
3.	Fetches same data
4.	Writes back to cache

➡️ 100 identical DB queries
➡️ DB gets hammered
➡️ Latency spikes

⸻

2️⃣ Why Cache Alone Does NOT Prevent This

Cache guarantees:
•	Fast reads on hit
•	Nothing on miss

Cache does not:
•	Coordinate concurrent misses
•	Serialize DB access
•	Deduplicate requests

⸻

3️⃣ This Is NOT a Multithreading Bug

Important clarification:
•	Each request is on a different thread
•	Threads are doing the correct thing
•	Problem is coordination, not threads

⸻

4️⃣ Common Real-World Solutions (Industry Standard)

✅ Solution 1: Cache Lock / Single Flight (Most Common)

Only ONE request goes to DB.

Others wait.

Pseudocode

if (cache.get(key) == null) {
synchronized(lockForKey(key)) {
if (cache.get(key) == null) {
value = db.load(key);
cache.put(key, value);
}
}
}
return cache.get(key);

✔ Prevents stampede
❌ Can block threads briefly

⸻

✅ Solution 2: Distributed Lock (Redis)

Used in multi-instance systems.

SETNX lock:key

Only one service instance:
•	Acquires lock
•	Loads DB
•	Updates cache

Others:
•	Spin / wait / retry

⸻

✅ Solution 3: Request Coalescing (Best)
•	First request triggers DB call
•	Others wait on same future
•	When DB returns, all get result

Used by:
•	Guava Cache
•	Caffeine
•	Netflix Hystrix
•	Resilience4j

Example (Caffeine):

cache.get(key, k -> db.load(k));

✔ Only one DB call
✔ No explicit locking
✔ Highly scalable

⸻

✅ Solution 4: Stale-While-Revalidate (Very Popular)

Cache never fully expires.
•	Serve stale data
•	Refresh in background

User → gets old data (fast)
Background thread → refreshes cache

Used by:
•	CDNs
•	Payment systems
•	Social media

⸻

✅ Solution 5: TTL Jitter (Simple & Effective)

Instead of fixed TTL:

TTL = 5 min ± random(30 sec)

Prevents many keys expiring at once.

⸻

5️⃣ What Payment Gateways Usually Do

Payment systems never rely only on TTL.

They combine:
•	Request coalescing
•	Short TTL
•	Background refresh
•	Idempotency keys

Because DB hits during expiry = outage risk.

⸻

6️⃣ Important Distinction

Concept	Meaning
Cache miss	No data
Cache stampede	Many misses at same time
Synchronization	Thread-level protection
Distributed lock	Cluster-level protection


⸻

7️⃣ Interview-Grade Answer

If asked:

“When cache expires and many requests come, do they all hit DB?”

Answer:

“Yes, unless cache stampede protection is implemented. Without coordination, concurrent cache misses can cause multiple identical DB queries. This is typically solved using request coalescing, locks, or stale-while-revalidate strategies.”

⸻

8️⃣ Final Mental Model

Cache is a book shelf.
When the book is missing:
•	Everyone runs to the library
•	Unless one person is assigned to fetch it

⸻

9️⃣ Key Takeaway (Memorize)

Caching improves average latency, not worst-case concurrency.
Stampede protection is mandatory at scale.

⸻

If you want next, I can:
•	Show Java code with CompletableFuture
•	Explain how Caffeine solves this internally
•	Explain Redis-based locking
•	Connect this to YouTube view count problem

Just tell me where to go next.