# SOLID Principles (Core of Object-Oriented Design)

### Single Responsibility Principle (SRP)
  * A class should have only one reason to change.
  * Promotes high cohesion and easier maintenance.
  * `Example:` Separate UserService and UserRepository.
  * `Example:` In a banking application, separate classes for handling account management and transaction processing.

### Open/Closed Principle (OCP)
  * Software entities should be open for extension but closed for modification.
  * Use interfaces and abstract classes to allow new functionality without changing existing code.
  * `Example:` Adding new payment methods without modifying existing payment processing code.
  * `Example:` In a notification system, adding new notification channels (like SMS, Email) without changing the existing notification logic.

### Liskov Substitution Principle (LSP)
  * Subtypes must be substitutable for their base types.
  * Derived classes should enhance, not weaken, the behavior of base classes.
  * `Example:` A Square class should not violate the behavior of a Rectangle class if it inherits from it.
  * `Example:` In a shape hierarchy, a Circle class should not violate the behavior of a Shape class if it inherits from it.

### Interface Segregation Principle (ISP)
  * Clients should not be forced to depend on interfaces they do not use.
  * Create smaller, specific interfaces rather than large, general ones.
  * `Example:` Different type of account - withdrawable and non-withdrawable accounts. Savings account should not implement withdraw method. So, segregate the interface into smaller ones.
  * `Example:` Separate interfaces for different user roles (Admin, User, Guest).
  * `Example:` In a media player application, separate interfaces for audio playback and video playback.

### Dependency Inversion Principle (DIP)
  * High-level modules should not depend on low-level modules; both should depend on abstractions.
  * Use dependency injection to achieve this.
  * `Example:` Notification Interface for sending notifications, implemented by EmailNotifier and SMSNotifier.
  * `Example:` A service class should depend on a repository interface, not a concrete implementation.
  * `Example:` In a reporting system, a ReportGenerator class should depend on a DataSource

---

# DRY – Don’t Repeat Yourself
* Avoid duplication in logic, configuration, and behavior.
* Encourages modular code with reusability.

⸻

# KISS – Keep It Simple, Stupid
* Code should be as simple as possible.
* Avoid over-engineering and unnecessary complexity.

⸻

# YAGNI – You Aren’t Gonna Need It
* Don’t add functionality unless it is necessary.
* Builds leaner and maintainable code.

⸻

# Separation of Concerns (SoC)
* Different parts of a system should handle different responsibilities.
* Promotes layered architecture: Controller → Service → Repository

⸻

# 6. Law of Demeter (LoD) – Principle of Least Knowledge
* A class should only talk to its direct friends, not strangers.
* Reduces tight coupling and increases modularity.

⸻

# 7. High Cohesion, Low Coupling
* High Cohesion: Each class should focus on a single task.
* Low Coupling: Classes should have minimal dependencies on one another.

⸻

# 8. Composition Over Inheritance
* Prefer composing objects over extending classes.
* Increases flexibility and testability.

⸻

# 9. Favor Immutability
* Use immutable objects to reduce side-effects, especially in concurrent code.
* Promotes thread-safety and predictability.

⸻

# 10. Design for Interfaces, Not Implementations
* Depend on abstractions (interfaces) to decouple modules.
* Enables flexible and testable code.

⸻

# 11. Encapsulation
* Keep internal state private and expose behavior through public methods.
* Prevents external interference and misuse.

⸻

# 12. Fail Fast Principle
* Fail early and loudly when invalid data is passed.
* Helps in catching bugs sooner.

⸻

# 13. Single Level of Abstraction
* A method should operate at a single level of abstraction.
* Improves readability and maintainability.

⸻

# 14. Command–Query Separation (CQS)
* A method should either change state (command) or return data (query), but not both.

⸻

# 15. Don’t Call Us, We’ll Call You (Hollywood Principle)
* Frameworks call your code (e.g., Spring), not the other way around.
* Enables inversion of control and plugin-style design.

⸻

# 16. Dependency Injection (DI) Principle
* Inject dependencies instead of hard-coding them.
* Reduces coupling and improves testability.

⸻

# 17. Design by Contract
* Methods should clearly define their expectations (preconditions, postconditions, invariants).
* Promotes correct usage of code.

⸻

# 18. TDA – Tell, Don’t Ask
* Don’t query an object’s state and make decisions; instead, tell the object what to do.
* Encourages behavior-rich domain objects.

⸻

# 19. Principle of Least Astonishment
* Code should behave in a way that least surprises users.
* Increases developer confidence and predictability.

⸻

# 20. Object Calisthenics (Advanced Design Discipline)
* Write classes with fewer than 50 lines
* Use only one level of indentation
* Wrap primitives and strings
* Use first-class collections, etc.